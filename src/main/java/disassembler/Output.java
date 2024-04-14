package disassembler;

import disassembler.elf.Table;
import disassembler.riscv.*;
import disassembler.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static disassembler.util.IntUtils.extract;

public class Output {
    public static String output(List<Instruction> instructions, List<Pair<String, Table<Integer>>> symbols, Map<Integer, String> labels) {
        StringBuilder result = new StringBuilder();

        result.append(".text\n\n\n");
        for (Instruction instruction : instructions) {
            if (labels.containsKey(instruction.getAddress())) {
                result.append(String.format("\n%08x \t<%s>:\n", instruction.getAddress(), labels.get(instruction.getAddress())));
            }
//            result.append(String.format("   %05x:\t%08x\t", instruction.getAddress(), instruction.getCode()));
            String add = "";
            if (instruction.getJumpAddress() != null) {
                if (!instruction.getName().equals("jal")) {
                    add = ", <" + labels.get(instruction.getJumpAddress()) + ">";
                } else {
                    add = " <" + labels.get(instruction.getJumpAddress()) + ">";
                }
            }
            result.append(instructionToString(instruction)).append(add).append("\n");
        }

        result.append("\n\n.symtab\n\n");
        result.append(temporary(symbols));
        return result.toString();
    }

    private static String instructionToString(Instruction instruction) {
        String name = instruction.getName();
        List<String> registers = Optional.ofNullable(instruction.getRegisters()).map(l -> l.stream().map(Output::registerOutput).toList()).orElse(null);
        Integer immediate = instruction.getImmediate();
        int address = instruction.getAddress();
        int code = instruction.getCode();


        return String.format("   %05x:\t%08x\t", address, code) + switch (instruction) {
            case BType b -> "b";
            case Invalid inv -> "invalid_instruction";
            case IType i -> "i";
            case JType j -> "j";
            case RType r  -> "r";
            case SType s -> "s";
            case UType u -> "u";
            case EType e -> "";
            case Fence f ->
                    String.format("%7s\t%s, %s", f.getName(), fenceOutput(f.getRegisters().get(0)), fenceOutput(f.getRegisters().get(1)));
        };
    }

    private static String temporary(List<Pair<String, Table<Integer>>> table) {
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
        if (extract(i, 0) == 1) res.append("i");
        if (extract(i, 1) == 1) res.append("o");
        if (extract(i, 2) == 1) res.append("r");
        if (extract(i, 3) == 1) res.append("w");
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
