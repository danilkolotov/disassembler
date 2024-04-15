package disassembler;

import disassembler.util.ByteIterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ByteIteratorTest {
    private static final List<Byte> testList;

    static {
        testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testList.add((byte) i);
        }
    }

    @Test
    public void testLength() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j <= 5 - i; j++) {
                assertEquals(j, new ByteIterator(testList, i).next(j).size());
            }
        }
    }

    @Test
    public void testErrors() {
        for (int i = 0; i < 5; i++) {
            for (int j = 5 - i + 1; j < 10; j++) {
                int finalI = i;
                int finalJ = j;
                assertThrows(IndexOutOfBoundsException.class, () -> new ByteIterator(testList, finalI).next(finalJ));
            }
        }
    }

    @Test
    public void testContents() {
        assertIterableEquals(new ByteIterator(testList, 0).next(1), List.of((byte) 0));
        assertIterableEquals(new ByteIterator(testList, 1).next(2), List.of((byte) 1, (byte) 2));
        assertIterableEquals(new ByteIterator(testList, 0).next(5), testList);
    }


}
