package disassembler.riscv.rv32i;


import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class SType extends Instruction {

    public SType(List<Byte> representation, int code) {
        super(representation, code);
    }

    @Override
    protected String parseName(int representation) {
        int funct3 = IntUtils.getBits(representation, 12, 15);
        return switch (funct3) {
            case 0b000 -> "sb";
            case 0b001 -> "sh";
            case 0b010 -> "sw";
            default -> throw error();
        };
    }

    @Override
    protected Integer parseImmediate(int representation) {
        return new IntUtils.BitBuilder()
                .place(0, 5, getBits(representation, 7, 12))
                .place(5, 11, getBits(representation, 25, 31))
                .fill(11, 32, getBits(representation, 31))
                .build();
    }
    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(getBits(representation, 15, 20), getBits(representation, 20, 25));
    }
}
