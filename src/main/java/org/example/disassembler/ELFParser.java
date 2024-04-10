package org.example.disassembler;

import org.example.disassembler.elf.Table;
import org.example.disassembler.elf.TableStructure;
import org.example.disassembler.util.ByteIterator;

import java.util.*;
import java.util.function.Function;

public class ELFParser {
    private final byte[] bytes;
    private Table<Integer> header;
//    private SectionHeaderTable sectionHeaderTable;
    private Table<Table<Integer>> sectionHeaderTable;
    private SymbolTable symbolTable;
    private List<Instruction> instructions;
    private int nameOffset;
    private Map<Integer, String> addressToName;

    private static int LEToInt(List<Byte> a) {
        int res = 0;

        for (int i = a.size() - 1; i >= 0; i--) {
            res = res * 16 * 16 + Byte.toUnsignedInt(a.get(i));
        }
        return res;
    }


    public ELFParser(byte[] bytes) {
        this.addressToName = new HashMap<>();
        this.bytes = bytes;
    }

    public String parse() {
        StringBuilder result = new StringBuilder();

        header = new TableStructure<>(ELFParser::LEToInt)
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

//        sh_name = LEToInt(iterator.next(4));
//        sh_type = LEToInt(iterator.next(4));
//        sh_flags = LEToInt(iterator.next(4));
//        sh_addr = LEToInt(iterator.next(4));
//        sh_offset = LEToInt(iterator.next(4));
//        sh_size = LEToInt(iterator.next(4));
//        sh_link = LEToInt(iterator.next(4));
//        sh_info = LEToInt(iterator.next(4));
//        sh_addralign = LEToInt(iterator.next(4));
//        sh_entsize = LEToInt(iterator.next(4));


        TableStructure<Integer> sectionHeaderStructure = new TableStructure<>(ELFParser::LEToInt)
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

        sectionHeaderTable = new TableStructure<>(list -> sectionHeaderStructure.build(new ByteIterator(list, 0)))
                .entry(sectionHeaderStructure.length(), header.get("e_shnum"))
                .build(new ByteIterator(bytes, header.get("e_shoff")));

//        sectionHeaderTable = new SectionHeaderTable(header.get("e_shoff"), header.get("e_shnum"));

//        int nameSectionOffset = sectionHeaderTable.getDataOffset(header.get("e_shstrndx"));
        int nameSectionOffset = sectionHeaderTable.get(header.get("e_shstrndx")).get("sh_offset");

        List<String> sectionNames = new ArrayList<>();
        for (int i = 0; i < header.get("e_shnum"); i++) {
//            int nameOffset = sectionHeaderTable.getNameOffset(i);
            int nameOffset = sectionHeaderTable.get(i).get("sh_name");
            sectionNames.add(parseName(nameSectionOffset + nameOffset));
        }

        for (int i = 0; i < sectionNames.size(); i++) {
            if (sectionNames.get(i).equals(".strtab")) {
//                nameOffset = sectionHeaderTable.getDataOffset(i);
                nameOffset = sectionHeaderTable.get(i).get("sh_offset");
            }
        }


        for (int i = 0; i < sectionNames.size(); i++) {
            if (sectionNames.get(i).equals(".symtab")) {
//                int entryNumber = sectionHeaderTable.getSize(i) / sectionHeaderTable.getEntrySize(i);
                int entryNumber = sectionHeaderTable.get(i).get("sh_size") / sectionHeaderTable.get(i).get("sh_entsize");
//                symbolTable = new SymbolTable(sectionHeaderTable.getDataOffset(i), entryNumber);
                symbolTable = new SymbolTable(sectionHeaderTable.get(i).get("sh_offset"), entryNumber);
            }
        }

        for (int i = 0; i < sectionNames.size(); i++) {
            if (sectionNames.get(i).equals(".text")) {
//                parseText(sectionHeaderTable.getDataOffset(i), sectionHeaderTable.getDataLength(i), sectionHeaderTable.getAddress(i));
                parseText(sectionHeaderTable.get(i).get("sh_offset"), sectionHeaderTable.get(i).get("sh_size"), sectionHeaderTable.get(i).get("sh_addr"));
            }
        }

        symbolTable.addInfo();

        int labelCount = 0;

        for (Instruction instruction : instructions) {
            if (instruction.label && !addressToName.containsKey(instruction.jump)) {
                addressToName.put(instruction.jump, "L" + labelCount);
                labelCount++;
            }
        }


        result.append(".text\n");
        for (Instruction instruction : instructions) {
            if (addressToName.containsKey(instruction.address)) {
                result.append(String.format("\n%08x \t<%s>:\n", instruction.address, addressToName.get(instruction.address)));
            }
            result.append(String.format("   %05x:\t%08x\t", instruction.address, instruction.code));
            String add = "";
            if (instruction.label) {
                if (instruction.bType) {
                    add = ", <" + addressToName.get(instruction.jump) + ">";
                } else {
                    add = " <" + addressToName.get(instruction.jump) + ">";
                }
            }
            result.append(instruction).append(add).append("\n");
        }

        result.append("\n\n.symtab\n\n");
        result.append(symbolTable);
        return result.toString();
    }

    private void parseText(int dataOffset, int length, int address) {
        instructions = new ArrayList<>();
        ByteIterator iterator = new ByteIterator(bytes, dataOffset);
        for (int i = 0; i < length / 4; i++) {
            instructions.add(new Instruction(LEToInt(iterator.next(4)), address + i * 4));
        }
    }

    private static class Instruction {
        final String string;
        final int address, code;
        boolean label = false;
        int jump = -1;
        public boolean bType = false;

        public Instruction(int code, int address) {
            final String inv = String.format("%-7s", "invalid_instruction");

            this.address = address;
            this.code = code;
            int opcode = get(code, 0, 7);
            int funct3;
            String rd, rs1, rs2;
            funct3 = get(code, 12, 15);
            rd = getRegName(get(code, 7, 12));
            rs1 = getRegName(get(code, 15, 20));
            rs2 = getRegName(get(code, 20, 25));
            switch (opcode) {
                case (0b0110111):
                    string = String.format("%8s", "lui\t") + getRegName(get(code, 7, 12)) + ", " + toHex(getUImmediate(code));
                    break;
                case (0b0010111):
                    string = String.format("%8s", "auipc\t") + getRegName(get(code, 7, 12)) + ", " + toHex(getUImmediate(code));
                    break;
                case (0b0010011):
                    funct3 = get(code, 12, 15);
                    rd = getRegName(get(code, 7, 12));
                    rs1 = getRegName(get(code, 15, 20));
                    switch (funct3) {
                        case (0b000):
                            string = String.format("%8s", "addi\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b010):
                            string = String.format("%8s", "slti\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b011):
                            string = String.format("%8s", "sltiu\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b100):
                            string = String.format("%8s", "xori\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b110):
                            string = String.format("%8s", "ori\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b111):
                            string = String.format("%8s", "andi\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code));
                            break;
                        case (0b001):
                            string = String.format("%8s", "slli\t") + rd + ", " + rs1 + ", " + toDec(getIImmediate(code << get(getIImmediate(code), 20, 25)));
                            break;
                        case (0b101):
                            switch (get(code, 27, 32)) {
                                case (0b00000):
                                    string = String.format("%8s", "srli\t") + rd + ", " + rs1 + ", " + toDec(get(code, 20, 25));
                                    break;
                                case (0b01000):
                                    string = String.format("%8s", "srai\t") + rd + ", " + rs1 + ", " + toDec(get(code, 20, 25));
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        default:
                            string = inv;
                            break;
                    }
                    break;
                case (0b0110011):
                    switch (funct3) {
                        case (0b000):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "mul\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "add\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0100000):
                                    string = String.format("%8s", "sub\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b001):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "mulh\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "sll\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b010):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "mulhsu\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "slt\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b011):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "mulhu\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "sltu\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b100):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "div\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "xor\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b101):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "divu\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "srl\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0100000):
                                    string = String.format("%8s", "sra\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b110):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "rem\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "or\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        case (0b111):
                            switch (get(code, 25, 32)) {
                                case (0b0000001):
                                    string = String.format("%8s", "remu\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                case (0b0000000):
                                    string = String.format("%8s", "and\t") + rd + ", " + rs1 + ", " + rs2;
                                    break;
                                default:
                                    string = inv;
                                    break;
                            }
                            break;
                        default:
                            string = inv;
                            break;
                    }
                    break;
                case (0b0001111):
                    int pred = get(code, 24, 28);
                    int succ = get(code, 20, 24);
                    String predString = parseFence(pred);
                    String succString = parseFence(succ);
                    string = String.format("%8s", "fence\t") + predString + ", " + succString;
                    break;
                case (0b1110011):
                    string = switch (get(code, 20, 25)) {
                        case 0b00000 -> String.format("%7s", "ecall");
                        case 0b00001 -> String.format("%7s", "ebreak");
                        default -> inv;
                    };
                    break;
                case (0b0000011):
                    rd = getRegName(get(code, 7, 12));
                    rs1 = getRegName(get(code, 15, 20));
                    switch (funct3) {
                        case (0b000):
                            string = String.format("%8s", "lb\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b001):
                            string = String.format("%8s", "lh\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b010):
                            string = String.format("%8s", "lw\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b100):
                            string = String.format("%8s", "lbu\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b101):
                            string = String.format("%8s", "lhu\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                            break;
                        default:
                            string = inv;
                            break;
                    }
                    break;
                case (0b0100011):
                    switch (funct3) {
                        case (0b000):
                            string = String.format("%8s", "sb\t") + rs2 + ", " + getSImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b001):
                            string = String.format("%8s", "sh\t") + rs2 + ", " + getSImmediate(code) + "(" + rs1 + ")";
                            break;
                        case (0b010):
                            string = String.format("%8s", "sw\t") + rs2 + ", " + getSImmediate(code) + "(" + rs1 + ")";
                            break;
                        default:
                            string = inv;
                            break;
                    }
                    break;
                case (0b1100111):
                    string = String.format("%8s", "jalr\t") + rd + ", " + getIImmediate(code) + "(" + rs1 + ")";
                    break;
                case (0b1101111):
                    string = String.format("%8s", "jal\t") + rd + ", " + toHex(getJImmediate(code) + address);
                    label = true;
                    jump = getJImmediate(code) + address;
                    break;
                case (0b1100011):
                    jump = getBImmediate(code) + address;
                    label = true;
                    bType = true;
                    switch (funct3) {
                        case (0b000):
                            string = String.format("%8s", "beq\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        case (0b001):
                            string = String.format("%8s", "bne\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        case (0b100):
                            string = String.format("%8s", "blt\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        case (0b101):
                            string = String.format("%8s", "bge\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        case (0b110):
                            string = String.format("%8s", "bltu\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        case (0b111):
                            string = String.format("%8s", "bgeu\t") + rs1 + ", " + rs2 + ", " + toHex(getBImmediate(code) + address);
                            break;
                        default:
                            string = inv;
                            break;
                    }
                    break;
                default:
                    string = inv;
                    break;
            }
        }

        private int get(int x, int l, int r) {
            int mask = 0;
            for (int i = l; i < r; i++) mask += 1 << i;
            return (mask & x) >>> l;
        }

        private String getRegName(int n) {
            if (n == 0) return "zero";
            if (n == 1) return "ra";
            if (n == 2) return "sp";
            if (n == 3) return "gp";
            if (n == 4) return "tp";
            if (5 <= n && n <= 7) return "t" + (n - 5);
            if (8 <= n && n <= 9) return "s" + (n - 8);
            if (10 <= n && n <= 17) return "a" + (n - 10);
            if (18 <= n && n <= 27) return "s" + (n - 16);
            if (28 <= n && n <= 31) return "t" + (n - 25);
            throw new IllegalArgumentException("Incorrect register number");
        }

        private String toHex(int x) {
            return "0x" + Integer.toHexString(x);
        }

        private String toDec(int x) {
            return Integer.toString(x);
        }


        private int getUImmediate(int x) {
            return get(x, 12, 32) + 0b11111111111100000000000000000000 * get(x, 31, 32);
        }

        private int getIImmediate(int x) {
            int res = 0b11111_11111_11111_11111000_000_000_000 * get(x, 31, 32);
            return res + get(x, 20, 32);
        }

        private int getBImmediate(int x) {
            int res = 0b11111111111111111111000000000000 * get(x, 31, 32);
            res += get(x, 7, 8) << 11;
            res += get(x, 25, 31) << 5;
            res += get(x, 8, 12) << 1;
            return res;
        }

        private int getJImmediate(int x) {
            int res = 0b11111111111100000000000000000000 * get(x, 31, 32);
            res += get(x, 12, 20) << 12;
            res += get(x, 20, 21) << 11;
            res += get(x, 21, 31) << 1;
            return res;
        }

        private int getSImmediate(int x) {
            return (get(x, 25, 32) << 5) + get(x, 7, 12) + get(x, 31, 32) * 0b11111_11111_11111_11111000_000_000_000;
        }

        private String parseFence(int x) {
            StringBuilder result = new StringBuilder();
            if (get(x, 0, 1) == 0b1) result.append("i");
            if (get(x, 1, 2) == 0b1) result.append("o");
            if (get(x, 2, 3) == 0b1) result.append("r");
            if (get(x, 3, 4) == 0b1) result.append("w");
            return result.toString();
        }

        @Override
        public String toString() {
            return string;
        }
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


    private class SectionHeaderTable {
        final List<SectionHeaderTableEntry> table;

        public SectionHeaderTable(int offset, int n) {
            table = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                table.add(new SectionHeaderTableEntry(offset + i * 40));
            }
        }

        public int getNameOffset(int i) {
            return table.get(i).sh_name;
        }

        public int getDataOffset(int i) {
            return table.get(i).sh_offset;
        }

        public int getDataLength(int i) {
            return table.get(i).sh_size;
        }

        public int getSize(int i) {
            return table.get(i).sh_size;
        }

        public int getEntrySize(int i) {
            return table.get(i).sh_entsize;
        }

        public int getAddress(int i) {
            return table.get(i).sh_addr;
        }


        private class SectionHeaderTableEntry {
            final int sh_name;
            final int sh_type;
            final int sh_flags;
            final int sh_addr;
            final int sh_offset;
            final int sh_size;
            final int sh_link;
            final int sh_info;
            final int sh_addralign;
            final int sh_entsize;
            final String realName;

            public SectionHeaderTableEntry(int offset) {
                ByteIterator iterator = new ByteIterator(bytes, offset);
                sh_name = LEToInt(iterator.next(4));
                sh_type = LEToInt(iterator.next(4));
                sh_flags = LEToInt(iterator.next(4));
                sh_addr = LEToInt(iterator.next(4));
                sh_offset = LEToInt(iterator.next(4));
                sh_size = LEToInt(iterator.next(4));
                sh_link = LEToInt(iterator.next(4));
                sh_info = LEToInt(iterator.next(4));
                sh_addralign = LEToInt(iterator.next(4));
                sh_entsize = LEToInt(iterator.next(4));
                realName = getName(sh_name);
            }
        }
    }

    private class SymbolTable {
        final List<SymbolTableEntry> table;

        public SymbolTable(int offset, int n) {
            table = parseTable(new ByteIterator(bytes, offset), SymbolTableEntry::new, n, 16);

        }

        private class SymbolTableEntry {
            final int st_name;
            final int st_value;
            final int st_size;
            final int st_info;
            final int st_type;
            final int st_bind;
            final int st_other;
            final int st_shndx;
            final int st_visibility;
            final String real_name;

            public SymbolTableEntry(List<Byte> data) {
                this(new ByteIterator(data, 0));
            }

            public SymbolTableEntry(ByteIterator iterator) {
                st_name = LEToInt(iterator.next(4));
                st_value = LEToInt(iterator.next(4));
                st_size = LEToInt(iterator.next(4));
                st_info = LEToInt(iterator.next(1));
                st_type = st_info % 16;
                st_bind = st_info / 16;
                st_other = LEToInt(iterator.next(1));
                st_visibility = st_other & 0x3;
                st_shndx = LEToInt(iterator.next(2));
                real_name = getName(st_name);
            }
        }

        public void addInfo() {
            for (SymbolTableEntry symbolTableEntry : table) {
                addressToName.put(symbolTableEntry.st_value, symbolTableEntry.real_name);
            }
        }

        @Override
        public String toString() {
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


            StringBuilder result = new StringBuilder();
            result.append("Symbol Value              Size Type     Bind     Vis       Index Name\n");
            for (int i = 0; i < table.size(); i++) {
                SymbolTableEntry current = table.get(i);
                result.append(String.format("[%4d] 0x%-15X %5d %-8s %-8s %-8s %6s %s\n", i, current.st_value, current.st_size, types.get(current.st_type), binds.get(current.st_bind), visibilities.get(current.st_visibility), getSection(current.st_shndx), getName(current.st_name)));
            }
            return result.toString();
        }

        private String getSection(int index) {
            return switch (index) {
                case 0b0 -> "UNDEF";
                case 0xff00 -> "LOPROC";
                case 0xff1f -> "HIPROC";
                case 0xff20 -> "LOOS";
                case 0xff3f -> "HIOS";
                case 0xfff1 -> "ABS";
                case 0xfff2 -> "COMMON";
                case 0xffff -> "XINDEX";
                default -> Integer.toString(index);
            };
        }
    }

    private String getName(int offset) {
        StringBuilder result = new StringBuilder();
        ByteIterator iterator = new ByteIterator(bytes, nameOffset + offset);
        char current = (char) (byte) iterator.next(1).get(0);
        while (current != 0) {
            result.append(current);
            current = (char) (byte) iterator.next(1).get(0);
        }
        return result.toString();
    }


    private static <T> List<T> parseTable(ByteIterator iterator, Function<List<Byte>, T> maker, int count, int entrySize) {
        List<T> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(maker.apply(iterator.next(entrySize)));
        }
        return res;
    }
}