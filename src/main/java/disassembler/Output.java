package disassembler;

import disassembler.elf.Table;
import disassembler.isa.Instruction;
import disassembler.riscv.rv32i.*;
import disassembler.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static disassembler.util.IntUtils.getBits;

public class Output {
    public static String output(List<Instruction> instructions, List<Pair<String, Table<Integer>>> symbols, Map<Integer, String> labels) {
        StringBuilder result = new StringBuilder();

        result.append(".text\n");
        for (Instruction instruction : instructions) {
            if (labels.containsKey(instruction.getAddress())) {
                result.append(String.format("\n%08x \t<%s>:\n", instruction.getAddress(), labels.get(instruction.getAddress())));
            }
            result.append(instructionToString(instruction, labels)).append("\n");
        }

        result.append("\n\n.symtab\n\n");
        result.append(symbolTableOutput(symbols));
        return result.toString();
    }

    private static String instructionToString(Instruction instruction, Map<Integer, String> labels) {
        String name = instruction.getName();
        int address = instruction.getAddress();
        int code = instruction.getCode();

        List<String> load = List.of("lb", "lh", "lw", "lbu", "lhu", "jalr");

        return String.format("   %05x:\t%08x\t", address, code) + switch (instruction) {
            case BType b -> String.format(
                    "%7s\t%s, %s, 0x%x, <%s>",
                    name,
                    registerOutput(b.getRegisters().get(0)),
                    registerOutput(b.getRegisters().get(1)),
                    b.getJumpAddress(),
                    labels.get(b.getJumpAddress())
                );
            case Invalid inv -> String.format("%-7s", "invalid_instruction");
            case IType i -> (!load.contains(name) ? String.format(
                    "%7s\t%s, %s, %s",
                    name,
                    registerOutput(i.getRegisters().get(0)),
                    registerOutput(i.getRegisters().get(1)),
                    i.getImmediate()
            ) : lsOutput(name, i.getRegisters().get(0), i.getRegisters().get(1), i.getImmediate()));
            case JType j -> String.format(
                    "%7s\t%s, 0x%x <%s>",
                    name,
                    registerOutput(j.getRegisters().get(0)),
                    j.getJumpAddress(),
                    labels.get(j.getJumpAddress())
            );
            case RType r  -> String.format(
                    "%7s\t%s, %s, %s",
                    name,
                    registerOutput(r.getRegisters().get(0)),
                    registerOutput(r.getRegisters().get(1)),
                    registerOutput(r.getRegisters().get(2))
            );
            case SType s -> lsOutput(name, s.getRegisters().get(1), s.getRegisters().get(0), s.getImmediate());
            case UType u -> String.format(
                    "%7s\t%s, 0x%s",
                    name,
                    registerOutput(u.getRegisters().get(0)),
                    Integer.toHexString(u.getImmediate())
            );
            case EType e -> String.format("%7s", name);
            case Fence f -> String.format(
                    "%7s\t%s, %s",
                    name,
                    fenceOutput(f.getRegisters().get(0)),
                    fenceOutput(f.getRegisters().get(1))
            );
            default -> throw new IllegalStateException("Unexpected value: " + instruction);
        };
    }

    private static String lsOutput(String name, int r1, int r2, int immediate) {
        return String.format(
                "%7s\t%s, %d(%s)",
                name,
                registerOutput(r1),
                immediate,
                registerOutput(r2)
        );
    }

    private static String symbolTableOutput(List<Pair<String, Table<Integer>>> table) {
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
            String name = table.get(i).first();
            Table<Integer> current = table.get(i).second();
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
                            sections.getOrDefault(current.get("st_shndx"), Integer.toString(current.get("st_shndx"))),
                            name
                    )
            );
        }
        return result.toString();
    }

    private static String fenceOutput(int i) {
        StringBuilder res = new StringBuilder();
        if (getBits(i, 0) == 1) res.append("i");
        if (getBits(i, 1) == 1) res.append("o");
        if (getBits(i, 2) == 1) res.append("r");
        if (getBits(i, 3) == 1) res.append("w");
        return res.toString();
    }

    private static String registerOutput(int n){
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
}
