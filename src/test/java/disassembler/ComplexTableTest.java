package disassembler;

import disassembler.elf.Table;
import disassembler.elf.TableStructure;
import disassembler.util.ByteIterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComplexTableTest {
    private static List<Byte> testList;

    @BeforeAll
    public static void setTestList() {
        testList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            testList.add((byte) i);
        }
    }

    @Test
    public void table2DTest() {
        TableStructure<Integer> entry =
                new TableStructure<>(list -> list.stream().mapToInt(Byte::intValue).sum())
                        .entry(2, "first")
                        .entry(1, "second");
        Table<Table<Integer>> table =
                new TableStructure<>(list -> entry.build(new ByteIterator(list, 0)))
                        .entry(entry.length(), 4)
                        .build(new ByteIterator(testList, 0));
        for (int i = 0; i < 4; i++) {
            int first = (int) testList.get(i * 3) + testList.get(i * 3 + 1);
            int second = testList.get(i * 3 + 2);
            assertEquals(first, table.get(i).get("first"));
            assertEquals(second, table.get(i).get("second"));
        }
    }

    @Test
    public void makeTest() {
        TableStructure<Integer> entry =
                new TableStructure<>(list -> list.stream().mapToInt(Byte::intValue).sum())
                        .entry(2, "first")
                        .entry(1, "second");
        Table<Table<Integer>> table = Table.make(entry, 4, new ByteIterator(testList, 0));
        for (int i = 0; i < 4; i++) {
            int first = (int) testList.get(i * 3) + testList.get(i * 3 + 1);
            int second = testList.get(i * 3 + 2);
            assertEquals(first, table.get(i).get("first"));
            assertEquals(second, table.get(i).get("second"));
        }
    }
}
