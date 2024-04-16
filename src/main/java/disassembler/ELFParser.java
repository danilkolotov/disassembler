package disassembler;

import disassembler.elf.Table;
import disassembler.elf.TableStructure;
import disassembler.isa.Instruction;
import disassembler.riscv.RISCParser;
import disassembler.util.ByteIterator;
import disassembler.util.IntUtils;
import disassembler.util.Pair;

import java.util.*;

public class ELFParser {
    private final byte[] bytes;

    private int nameOffset;

    public ELFParser(byte[] bytes) {
        this.bytes = bytes;
    }

    public String parse() {
        Table<Integer> header = new TableStructure<>(IntUtils::LEToInt)
                .entry(16, "e_ident")
                .entry(2, "e_type")
                .entry(2, "e_machine")
                .entry(4, "e_version")
                .entry(4, "e_entry")
                .entry(4, "e_phoff")
                .entry(4, "e_shoff")
                .entry(4, "e_flags")
                .entry(2, "e_ehsize")
                .entry(2, "e_phentsize")
                .entry(2, "e_phnum")
                .entry(2, "e_shentsize")
                .entry(2, "e_shnum")
                .entry(2, "e_shstrndx")
                .build(new ByteIterator(bytes, 0));

        TableStructure<Integer> sectionHeaderStructure = new TableStructure<>(IntUtils::LEToInt)
                .entry(4, "sh_name")
                .entry(4, "sh_type")
                .entry(4, "sh_flags")
                .entry(4, "sh_addr")
                .entry(4, "sh_offset")
                .entry(4, "sh_size")
                .entry(4, "sh_link")
                .entry(4, "sh_info")
                .entry(4, "sh_addralign")
                .entry(4, "sh_entsize");

        Table<Table<Integer>> sectionHeaderTable = Table.make(
                sectionHeaderStructure,
                header.get("e_shnum"),
                new ByteIterator(bytes, header.get("e_shoff"))
        );
        int nameSectionOffset = sectionHeaderTable.get(header.get("e_shstrndx")).get("sh_offset");

        List<String> sectionNames = new ArrayList<>();
        for (int i = 0; i < header.get("e_shnum"); i++) {
            sectionNames.add(parseName(nameSectionOffset + sectionHeaderTable.get(i).get("sh_name")));
        }

        int stringTableIndex = sectionNames.indexOf(".strtab");
        nameOffset = sectionHeaderTable.get(stringTableIndex).get("sh_offset");

        Table<Integer> symbolTableHeader = sectionHeaderTable.get(sectionNames.indexOf(".symtab"));
        TableStructure<Integer> symbolStructure = new TableStructure<>(IntUtils::LEToInt)
                .entry(4, "st_name")
                .entry(4, "st_value")
                .entry(4, "st_size")
                .entry(1, "st_info")
                .entry(1, "st_other")
                .entry(2, "st_shndx");
        Table<Table<Integer>> symbolTable = Table.make(
                symbolStructure,
                symbolTableHeader.get("sh_size") / symbolTableHeader.get("sh_entsize"),
                new ByteIterator(bytes, symbolTableHeader.get("sh_offset"))
        );

        Table<Integer> textHeader = sectionHeaderTable.get(sectionNames.indexOf(".text"));

        List<Instruction> instructions = RISCParser.parse(
                new ByteIterator(bytes, textHeader.get("sh_offset"), textHeader.get("sh_size")),
                textHeader.get("sh_addr")
        );

        Map<Integer, String> addressToLabel = new HashMap<>();
        for (Table<Integer> symbolTableEntry : symbolTable) {
            addressToLabel.put(symbolTableEntry.get("st_value"), getName(symbolTableEntry.get("st_name")));
        }

        int labelCount = 0;
        for (Instruction instruction : instructions) {
            try {
                Integer jump = instruction.getJumpAddress();
                if (!addressToLabel.containsKey(jump)) {
                    addressToLabel.put(jump, "L" + labelCount++);
                }
            } catch (UnsupportedOperationException ignored) {}
        }

        List<Pair<String, Table<Integer>>> symbols = new ArrayList<>();
        for (Table<Integer> symbol : symbolTable) {
            symbols.add(new Pair<>(getName(symbol.get("st_name")), symbol));
        }

        return Output.output(instructions, symbols, addressToLabel);
    }

    private String parseName(int offset) {
        ByteIterator iterator = new ByteIterator(bytes, offset);
        StringBuilder result = new StringBuilder();
        Byte current;
        while ((current = iterator.next(1).get(0)) != 0) {
            result.append((char) (byte) current);
        }
        return result.toString();
    }

    private String getName(int offset) {
        return parseName(nameOffset + offset);
    }
}