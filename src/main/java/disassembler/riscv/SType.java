package disassembler.riscv;


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
        return (extract(representation, 25, 32) << 5) + extract(representation, 7, 12) + extract(representation, 31, 32) * 0b11111_11111_11111_11111000_000_000_000;
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
