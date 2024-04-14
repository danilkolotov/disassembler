package disassembler.riscv;


import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class SType extends Instruction {

    public SType(List<Byte> representation, int code) {
        super(representation, code);
    }

    @Override
    protected String parseName() {
        int funct3 = IntUtils.getBits(representation, 12, 15);
        return switch (funct3) {
            case 0b000 -> "sb";
            case 0b001 -> "sh";
            case 0b010 -> "sw";
            default -> throw new IllegalArgumentException("Illegal funct3, code: 0x" + Integer.toHexString(representation));
        };
    }

    @Override
    protected Integer parseImmediate() {
        return new IntUtils.BitBuilder()
                .place(0, 5, IntUtils.getBits(representation, 7, 12))
                .place(5, 11, IntUtils.getBits(representation, 25, 31))
                .fill(11, 32, getBits(representation, 31))
                .build();
    }

    @Override
    protected Integer parseJumpAddress() {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(IntUtils.getBits(representation, 15, 20), IntUtils.getBits(representation, 20, 25));
    }
}
