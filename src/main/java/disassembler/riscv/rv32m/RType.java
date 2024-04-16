package disassembler.riscv.rv32m;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public class RType extends disassembler.riscv.rv32i.RType {
    public RType(List<Byte> current, int address) {
        super(current, address);
    }

    @Override
    protected String parseName(int representation) {
        int funct3 = getBits(representation, 12, 15);
        return switch (funct3) {
            case 0b000 -> "mul";
            case 0b001 -> "mulh";
            case 0b010 -> "mulhsu";
            case 0b011 -> "mulhu";
            case 0b100 -> "div";
            case 0b101 -> "divu";
            case 0b110 -> "rem";
            case 0b111 -> "remu";
            default -> throw error();
        };
    }
}
