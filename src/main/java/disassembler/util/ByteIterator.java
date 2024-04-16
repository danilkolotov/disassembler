package disassembler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteIterator {
    private final byte[] source;
    private int position;
    public ByteIterator(List<Byte> source, int start) {
        this.source = new byte[source.size()];
        for (int i = 0; i < source.size(); i++) {
            this.source[i] = source.get(i);
        }
        this.position = start;
    }

    public ByteIterator(byte[] source, int start) {
        this.source = source;
        this.position = start;
    }

    public ByteIterator(byte[] bytes, int dataOffset, int length) {
        this(Arrays.copyOfRange(bytes, dataOffset, dataOffset + length), 0);
    }

    public List<Byte> next(int length) {
        List<Byte> result = new ArrayList<>();
        for (int i = position; i < position + length; i++) {
            result.add(source[i]);
        }
        position += length;
        return result;
    }

    public boolean hasNext(int length) {
        return position + length <= source.length;
    }
}
