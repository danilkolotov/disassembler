package disassembler.elf;

import disassembler.util.ByteIterator;

import java.util.*;

public class Table<Entry> implements Iterable<Entry> {
    private final List<Entry> fields;
    private final Map<String, Integer> names;

    public Table(List<Entry> fields, Map<String, Integer> names) {
        this.fields = new ArrayList<>(fields);
        this.names = new HashMap<>(names);
    }

    public static <T> Table<Table<T>> make(TableStructure<T> structure, int count, ByteIterator iterator) {
        return new TableStructure<>(list -> structure.build(new ByteIterator(list, 0)))
                .entry(structure.length(), count)
                .build(iterator);
    }

    public Entry get(int index) {
        return fields.get(index);
    }

    public Entry get(String name) {
        return get(names.get(name));
    }

    public boolean contains(int index) {
        return fields.size() > index;
    }

    public boolean contains(String name) {
        return names.containsKey(name);
    }

    public int size() {
        return fields.size();
    }

    @Override
    public Iterator<Entry> iterator() {
        return fields.listIterator();
    }
}
