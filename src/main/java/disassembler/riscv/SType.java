package disassembler.riscv;


import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.extract;

public final class SType extends Instruction {

    public SType(List<Byte> representation, int code) {
        super(representation, code);
    }

    @Override
    protected String parseName() {
        int funct3 = extract(representation, 12, 15);
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
                .fill(0, 5, extract(representation, 7, 12))
                .fill(5, 11, extract(representation, 25, 31))
                .repeat(11, 32, extract(representation, 31))
                .build();
    }

    @Override
    protected Integer parseJumpAddress() {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(extract(representation, 15, 20), extract(representation, 20, 25));
    }
}
