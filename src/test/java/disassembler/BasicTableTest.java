package disassembler;

import disassembler.elf.Table;
import disassembler.elf.TableStructure;
import disassembler.util.ByteIterator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTableTest {
    @Test
    public void testSingleUnnamed() {
        List<Byte> testList = List.of((byte) 1);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> list.get(0))
                .entry(1)
                .build(iterator);
        assertEquals(testList.get(0), table.get(0));
    }

    @Test
    public void testSingleNames() {
        List<Byte> testList = List.of((byte) 1);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> list.get(0))
                .entry(1, "name")
                .build(iterator);
        assertEquals(testList.get(0), table.get(0));
        assertEquals(testList.get(0), table.get("name"));
    }

    @Test
    public void testLength() {
        List<Byte> testList = List.of((byte) 0, (byte) 1, (byte) 2);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> list.get(0))
                .entry(1, "name")
                .entry(1)
                .entry(1)
                .build(iterator);
        assertEquals(3, table.size());
    }

    @Test
    public void testLengthMultiple() {
        List<Byte> testList = List.of((byte) 0, (byte) 1, (byte) 2);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> list.get(0))
                .entry(1, "name")
                .entry(1, 2)
                .build(iterator);
        assertEquals(3, table.size());
    }

    @Test
    public void testContents() {
        List<Byte> testList = List.of((byte) 0, (byte) 1, (byte) 2);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> (byte) (list.get(0) + 1))
                .entry(1, "name")
                .entry(1, 2)
                .build(iterator);
        assertEquals((byte) 1, table.get(0));
        assertEquals((byte) 2, table.get(1));
        assertEquals((byte) 3, table.get(2));
        assertEquals((byte) 1, table.get("name"));
    }

    @Test
    public void testContains() {
        List<Byte> testList = Collections.nCopies(5, (byte) 1);
        ByteIterator iterator = new ByteIterator(testList, 0);
        Table<Byte> table = new TableStructure<>(list -> list.get(0))
                .entry(1, "word")
                .entry(1, "another")
                .entry(1, 2)
                .entry(1, "NAME")
                .build(iterator);
        for (int i = 0; i < 5; i++) {
            assertTrue(table.contains(i));
        }
        for (int i = 5; i < 10; i++) {
            assertFalse(table.contains(i));
        }
        for (String name : List.of("word", "another", "NAME")) {
            assertTrue(table.contains(name));
        }
        assertFalse(table.contains("no-such-name"));
    }

    @Test
    public void testData() {
        List<Byte> testList = Collections.nCopies(5, (byte) 1);
        ByteIterator iterator = new ByteIterator(testList, 0);
        TableStructure<Byte> builder = new TableStructure<>(list -> list.get(0))
                .entry(1, 62);
        assertThrows(IllegalArgumentException.class, () -> builder.build(iterator));
    }

    @Test
    public void testName() {
        List<Byte> testList = Collections.nCopies(5, (byte) 1);
        ByteIterator iterator = new ByteIterator(testList, 0);
        TableStructure<Byte> builder = new TableStructure<>(list -> list.get(0))
                .entry(1, "name")
                .entry(1, "name");
        assertThrows(IllegalArgumentException.class, () -> builder.build(iterator));
    }
}
