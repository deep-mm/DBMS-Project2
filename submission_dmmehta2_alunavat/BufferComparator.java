package src.buffer;

import java.util.Comparator;

public class BufferComparator implements Comparator<Buffer> {

    @Override
    public int compare(Buffer b1, Buffer b2) {
        if((b1.isPinned() && b2.isPinned()) || (!b1.isPinned() && !b2.isPinned())){
            if ((b1.getLsn() == -1 && b2.getLsn() == -1) || (b1.getLsn() != -1 && b2.getLsn() != -1)){
                return b1.getLsn() > b2.getLsn() ? 1 : -1;
            }
            else if (b1.getLsn() == -1){
                return 1;
            }
            else{
                return -1;
            }
        }
        else if(b1.isPinned())
            return 1;
        return -1;
    }
}
