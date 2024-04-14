package disassembler;

import disassembler.elf.Table;
import disassembler.elf.TableStructure;
import disassembler.riscv.Instruction;
import disassembler.riscv.InstructionParser;
import disassembler.util.ByteIterator;
import disassembler.util.IntUtils;

import java.util.*;
import java.util.function.Function;
public class Parser {
    private final byte[] bytes;
    private Table<Integer> header;
    private Table<Table<Integer>> sectionHeaderTable;
    private Table<Table<Integer>> symbolTable;
    private List<Instruction> instructions;
    private int nameOffset;
    private Map<Integer, String> addressToName;

    public Parser(byte[] bytes) {
        this.addressToName = new HashMap<>();
        this.bytes = bytes;
    }

//    public String parse(Function<Instruction, String> outputStrategy) {
    public String parse() {
        StringBuilder result = new StringBuilder();

        header = new TableStructure<>(IntUtils::LEToInt)
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

        sectionHeaderTable = Table.make(
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
        symbolTable = Table.make(
                symbolStructure,
                symbolTableHeader.get("sh_size") / symbolTableHeader.get("sh_entsize"),
                new ByteIterator(bytes, symbolTableHeader.get("sh_offset"))
        );

        Table<Integer> textHeader = sectionHeaderTable.get(sectionNames.indexOf(".text"));
        parseText(textHeader.get("sh_offset"), textHeader.get("sh_size"), textHeader.get("sh_addr"));

        for (Table<Integer> symbolTableEntry : symbolTable) {
            addressToName.put(symbolTableEntry.get("st_value"), getName(symbolTableEntry.get("st_name")));
        }

        int labelCount = 0;
        System.err.println(addressToName);
        for (Instruction instruction : instructions) {
            Integer jump = instruction.getJumpAddress();
            if (jump != null && !addressToName.containsKey(jump)) {
                addressToName.put(jump, "L" + labelCount++);
            }
        }

        result.append(".text\n");
        for (Instruction instruction : instructions) {
            if (addressToName.containsKey(instruction.getAddress())) {
                result.append(String.format("\n%08x \t<%s>:\n", instruction.getAddress(), addressToName.get(instruction.getAddress())));
            }
//            result.append(String.format("   %05x:\t%08x\t", instruction.getAddress(), instruction.getCode()));
            String add = "";
            if (instruction.getJumpAddress() != null) {
                if (!instruction.getName().equals("jal")) {
                    add = ", <" + addressToName.get(instruction.getJumpAddress()) + ">";
                } else {
                    add = " <" + addressToName.get(instruction.getJumpAddress()) + ">";
                }
            }
//            result.append(outputStrategy.apply(instruction)).append(add).append("\n");
                result.append(instruction).append(add).append("\n");
        }

        result.append("\n\n.symtab\n\n");
        result.append(temporary(symbolTable));
        return result.toString();
    }

    private String temporary(Table<Table<Integer>> table) {
        Map<Integer, String> types = new HashMap<>();
        types.put(0, "NOTYPE");
        types.put(1, "OBJECT");
        types.put(2, "FUNC");
        types.put(3, "SECTION");
        types.put(4, "FILE");
        types.put(5, "COMMON");
        types.put(6, "TLS");
        types.put(10, "LOOS");
        types.put(12, "HIOS");
        types.put(13, "LOPROC");
        types.put(15, "HIPROC");
        Map<Integer, String> binds = new HashMap<>();
        binds.put(0, "LOCAL");
        binds.put(1, "GLOBAL");
        binds.put(2, "WEAK");
        binds.put(10, "LOOS");
        binds.put(12, "HIOS");
        binds.put(13, "LOPROC");
        binds.put(15, "HIPROC");
        Map<Integer, String> visibilities = new HashMap<>();
        visibilities.put(0, "DEFAULT");
        visibilities.put(1, "INTERNAL");
        visibilities.put(2, "HIDDEN");
        visibilities.put(3, "PROTECTED");
        Map<Integer, String> sections = new HashMap<>();
        sections.put(0x0000, "UNDEF");
        sections.put(0xff00, "LOPROC");
        sections.put(0xff1f, "HIPROC");
        sections.put(0xff20, "LOOS");
        sections.put(0xff3f, "HIOS");
        sections.put(0xfff1, "ABS");
        sections.put(0xfff2, "COMMON");
        sections.put(0xffff, "XINDEX");

        StringBuilder result = new StringBuilder();
        result.append("Symbol Value              Size Type     Bind     Vis       Index Name\n");
        for (int i = 0; i < table.size(); i++) {
            Table<Integer> current = table.get(i);
            int st_type = current.get("st_info") % 16;
            int st_bind = current.get("st_info") / 16;
            int st_visibility = current.get("st_other") & 0x3;
            result.append(
                String.format(
                    "[%4d] 0x%-15X %5d %-8s %-8s %-8s %6s %s\n",
                    i,
                    current.get("st_value"),
                    current.get("st_size"),
                    types.get(st_type),
                    binds.get(st_bind),
                    visibilities.get(st_visibility),
                    sections.getOrDefault(current.get("st_shndx"),
                    Integer.toString(current.get("st_shndx"))),
                    getName(current.get("st_name"))
                )
            );
        }
        return result.toString();
    }

    private void parseText(int dataOffset, int length, int address) {
        instructions = new ArrayList<>();
        ByteIterator iterator = new ByteIterator(bytes, dataOffset, length);
        instructions = InstructionParser.parse(iterator, address);
    }

    private String parseName(int offset) {
        ByteIterator iterator = new ByteIterator(bytes, offset);
        StringBuilder result = new StringBuilder();
        char cc = (char) (byte) iterator.next(1).get(0);
        while (cc != 0) {
            result.append(cc);
            cc = (char) (byte) iterator.next(1).get(0);
        }
        return result.toString();
    }

    private String getName(int offset) {
        return parseName(nameOffset + offset);
    }
}