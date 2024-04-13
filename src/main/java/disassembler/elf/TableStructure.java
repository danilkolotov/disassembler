package disassembler.elf;

import disassembler.util.ByteIterator;
import disassembler.util.Pair;

import java.util.*;
import java.util.function.Function;

public class TableStructure<Entry> {
    private final Function<List<Byte>, Entry> maker;
    private final List<Pair<Integer, String>> fieldsInfo;

    public TableStructure(Function<List<Byte>, Entry> maker) {
        this.maker = maker;
        this.fieldsInfo = new ArrayList<>();
    }

    public TableStructure<Entry> entry(int length) {
        return entry(length, null);
    }

    public TableStructure<Entry> entry(int length, String name) {
        fieldsInfo.add(new Pair<>(length, name));
        return this;
    }

    public TableStructure<Entry> entry(int length, int count) {
        for (int i = 0; i < count; i++) {
            this.entry(length);
        }
        return this;
    }

    public int length() {
        return fieldsInfo.stream().mapToInt(Pair::first).sum();
    }


    public Table<Entry> build(ByteIterator iterator) {
        List<Entry> fields = new ArrayList<>();
        Map<String, Integer> names = new HashMap<>();
        for (Pair<Integer, String> field : fieldsInfo) {
            try {
                fields.add(maker.apply(iterator.next(field.first())));
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough data can be obtained from provided iterator");
            }
            String name = field.second();
            if (name != null) {
                if (names.containsKey(name)) {
                    throw new IllegalArgumentException("More than one field with name " + name + " were provided");
                }
                names.put(name, fields.size() - 1);
            }
        }
        return new Table<>(fields, names);
    }
}
