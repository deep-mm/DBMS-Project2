package src.buffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import src.file.*;
import src.log.LogMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
public class BufferMgr {
   // Replace array with map
   //private Buffer[] bufferpool;
   public Map<BlockId,Buffer> bufferPoolMap;
   private PriorityQueue<Buffer> bufferpool;
   private int numAvailable;
   private static final long MAX_TIME = 10000; // 10 seconds
   
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on a {@link FileMgr} and
    * {@link src.log.LogMgr LogMgr} object.
    * @param numbuffs the number of buffer slots to allocate
    */
   public BufferMgr(FileMgr fm, LogMgr lm, int numbuffs) {
      //bufferpool = new Buffer[numbuffs];
      bufferPoolMap = new HashMap<BlockId,Buffer>();
      numAvailable = numbuffs;
      bufferpool = new PriorityQueue<Buffer>(numbuffs, new BufferComparator());
      for (int i=0; i<numbuffs; i++)
         //bufferpool[i] = new Buffer(fm, lm);
         bufferpool.add(new Buffer(fm, lm, i));
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   public synchronized int available() {
      return numAvailable;
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   public synchronized void flushAll(int txnum) {
      //for (Buffer buff : bufferpool)
      for (Buffer buff : bufferPoolMap.values())
         if (buff.modifyingTx() == txnum)
            buff.flush();
   }
   
   
   /**
    * Unpins the specified data buffer. If its pin count
    * goes to zero, then notify any waiting threads.
    * @param buff the buffer to be unpinned
    */
   public synchronized void unpin(Buffer buff) {
      bufferpool.remove(buff);
      buff.unpin();
      bufferpool.add(buff);
      if (!buff.isPinned()) {
         numAvailable++;
         notifyAll();
      }
   }
   
   /**
    * Pins a buffer to the specified block, potentially
    * waiting until a buffer becomes available.
    * If no buffer becomes available within a fixed 
    * time period, then a {@link BufferAbortException} is thrown.
    * @param blk a reference to a disk block
    * @return the buffer pinned to that block
    */
   public synchronized Buffer pin(BlockId blk) {
      try {
         long timestamp = System.currentTimeMillis();
         Buffer buff = tryToPin(blk);
         while (buff == null && !waitingTooLong(timestamp)) {
            wait(MAX_TIME);
            buff = tryToPin(blk);
         }
         if (buff == null)
            throw new BufferAbortException();

         return buff;
      }
      catch(InterruptedException e) {
         throw new BufferAbortException();
      }
   }  
   
   private boolean waitingTooLong(long starttime) {
      return System.currentTimeMillis() - starttime > MAX_TIME;
   }
   
   /**
    * Tries to pin a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   private Buffer tryToPin(BlockId blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         buff.assignToBlock(blk);
         buff.setLsn(-1);
         bufferPoolMap.put(blk, buff);
      }
      if (!buff.isPinned())
         numAvailable--;
      bufferpool.remove(buff);
      buff.pin();
      bufferpool.add(buff);
      return buff;
   }
   
   private Buffer findExistingBuffer(BlockId blk) {
      //for (Buffer buff : bufferpool) {
      //   BlockId b = buff.block();
      //   if (b != null && b.equals(blk))
      //      return buff;
      //}

      //return null;

      // Replace array with map
      return bufferPoolMap.get(blk);
   }
   
   private Buffer chooseUnpinnedBuffer() {
      //for (Buffer buff : bufferpool)
      //   if (!buff.isPinned())
      //   return buff;
      Buffer buff = bufferpool.peek();
      if (buff.isPinned() || buff.getLsn() == -1)
         return null;
      else {
         bufferPoolMap.remove(buff.block());
         return buff;
      }
   }

   public void printStatus(){
      System.out.println("\nAllocated Buffers:");
      for (Buffer buff : bufferPoolMap.values())
         System.out.println(buff.toString());
      
      System.out.println("\nUnpinned Buffers in LRM order: ");
      Iterator bufferIterator = bufferpool.iterator();
      while (bufferIterator.hasNext()) {
         Buffer buff = (Buffer)bufferIterator.next();
         if (!buff.isPinned())
            System.out.println("Buffer : " + buff.getId() + " (lsn : " + buff.getLsn() + " ) ");
      }
      System.out.println("");
   }
}
