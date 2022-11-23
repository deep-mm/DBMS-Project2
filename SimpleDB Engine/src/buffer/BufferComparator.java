package src.buffer;

import java.util.Comparator;

public class BufferComparator implements Comparator<Buffer> {

    @Override
    public int compare(Buffer b1, Buffer b2) {
        return b1.getLsn() > b2.getLsn() || b1.isPinned() ? 1 : -1;
    }
}
