package org.example.disassembler.util;

import java.util.ArrayList;
import java.util.List;

public class ByteIterator {
    private final List<Byte> source;
    private int position;
    public ByteIterator(List<Byte> source, int start) {
        this.source = source;
        this.position = start;
    }

    public ByteIterator(byte[] source, int start) {
        this.source = new ArrayList<>();
        for (byte b : source) {
            this.source.add(b);
        }
        this.position = start;
    }

    public List<Byte> next(int length) {
        List<Byte> result = source.subList(position, position + length);
        position += length;
        return result;
    }
}
