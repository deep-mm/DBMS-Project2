Authors : Deep Mehta (dmmehta2), Anmol Lunavat (alunavat)

The following updates have been made to SimpleDB server for Project 2:

1. Introduced a id for each buffer with get & set methods.
2. Added get and set methods for lsn in buffer class.
3. Replaces the Buffer[] bufferpool in bufferManager class with Map<BlockId,Buffer> bufferPoolMap & PriorityQueue<Buffer> bufferpool.
4. The bufferPoolMap is a mapping of blockIds and the buffers they are allocated to.
5. The bufferPool priority queues ensures that the unpinned buffer with lease LSN is at the top and can be selected when searching for buffers.

---

Manual run of bufferManagerTest:

Initial State (After pinning 5 blocks)

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           5           -1
buff2        2           2           -1
buff3        3           3           -1
buff1        4           1           -1

Step 1: Modify buff 5,2,3 with LSN 1,2,3 respectively

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           5           1
buff2        2           2           2
buff3        3           3           3
buff1        4           1           -1

Step 2: Unpin buff 3,5

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1                       1
buff2        2           2           2
buff3        3                       3
buff1        4           1           -1

Step 3: Pin Buffer with BlockID 6 - Since buff5 has least LSN and is unpinned, 6 will be assigned to that

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           6           -1
buff2        2           2           2
buff3        3                       3
buff1        4           1           -1

Step 4: Modify buff 1,5 with LSN 4,5 respectively

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           6           5
buff2        2           2           2
buff3        3                       3
buff1        4           1           4

Step 5: Pin buffer with Block ID 3 - Since only one unpinned buffer is there with LSN not equal to -1, we pin it to buff3

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           6           5
buff2        2           2           2
buff3        3           3           -1
buff1        4           1           4

Step6: Modify buff3 with LSN 6

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           6           5
buff2        2           2           2
buff3        3           3           6
buff1        4           1           4

Step 7: Unpin buff 3,5

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1                       5
buff2        2           2           2
buff3        3                       6
buff1        4           1           4

Step 8: Pin buffer with Block ID 7 - Since buff5 has the min LSN and is unpinned, Block 7 is pinned to it

Buff         BufferID    BlockID     LSN
buff4        0           4           -1
buff5        1           7           -1
buff2        2           2           2
buff3        3                       6
buff1        4           1           4

Step 9: Unpin buff4

Buff         BufferID    BlockID     LSN
buff4        0                       -1
buff5        1           7           -1
buff2        2           2           2
buff3        3                       6
buff1        4           1           4

Step 10: Pin buffer with Block ID 5 - Since buff3 is the only unpinned buffer with LSN not equal to -1, Block 5 is pinned to it

Buff         BufferID    BlockID     LSN
buff4        0                       -1
buff5        1           7           -1
buff2        2           2           2
buff3        3           5           -1
buff1        4           1           4

* Note: Unpinned buffers are denoted with empty block id's

---

Code Output Snippet:

Pin block 1
Pin block 2
Pin block 3
Pin block 4
Pin block 5
Unpin block [file test, block 3]
Unpin block [file test, block 5]

Allocated Buffers:
Buffer 1: [file test, block 5] status : unpinned lsn 1
Buffer 0: [file test, block 4] status : pinned lsn -1
Buffer 4: [file test, block 1] status : pinned lsn -1
Buffer 3: [file test, block 3] status : unpinned lsn 3
Buffer 2: [file test, block 2] status : pinned lsn 2

Unpinned Buffers in LRM order: 
Buffer : 1 (lsn : 1 ) 
Buffer : 3 (lsn : 3 ) 

Pin block 6

Allocated Buffers:
Buffer 0: [file test, block 4] status : pinned lsn -1
Buffer 1: [file test, block 6] status : pinned lsn -1
Buffer 4: [file test, block 1] status : pinned lsn -1
Buffer 3: [file test, block 3] status : unpinned lsn 3
Buffer 2: [file test, block 2] status : pinned lsn 2

Unpinned Buffers in LRM order: 
Buffer : 3 (lsn : 3 ) 

Pin block 3
Unpin block [file test, block 3]
Unpin block [file test, block 6]
Pin block 7

Allocated Buffers:
Buffer 0: [file test, block 4] status : pinned lsn -1
Buffer 1: [file test, block 7] status : pinned lsn -1
Buffer 4: [file test, block 1] status : pinned lsn 4
Buffer 3: [file test, block 3] status : unpinned lsn 6
Buffer 2: [file test, block 2] status : pinned lsn 2

Unpinned Buffers in LRM order: 
Buffer : 3 (lsn : 6 ) 

Unpin block [file test, block 4]
Pin block 5

Allocated Buffers:
Buffer 3: [file test, block 5] status : pinned lsn -1
Buffer 0: [file test, block 4] status : unpinned lsn -1
Buffer 1: [file test, block 7] status : pinned lsn -1
Buffer 4: [file test, block 1] status : pinned lsn 4
Buffer 2: [file test, block 2] status : pinned lsn 2

Unpinned Buffers in LRM order: 
Buffer : 0 (lsn : -1 ) 

For key [file test, block 5], the values are Buffer 3: [file test, block 5] status : pinned lsn -1
For key [file test, block 4], the values are Buffer 0: [file test, block 4] status : unpinned lsn -1
For key [file test, block 7], the values are Buffer 1: [file test, block 7] status : pinned lsn -1
For key [file test, block 1], the values are Buffer 4: [file test, block 1] status : pinned lsn 4
For key [file test, block 2], the values are Buffer 2: [file test, block 2] status : pinned lsn 2