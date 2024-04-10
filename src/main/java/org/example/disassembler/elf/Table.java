package org.example.disassembler.elf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table<Entry> {
    private final List<Entry> fields;
    private final Map<String, Integer> names;

    public Table(List<Entry> fields, Map<String, Integer> names) {
        this.fields = new ArrayList<>(fields);
        this.names = new HashMap<>(names);
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
}
