package disassembler.riscv;

import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.extract;

public final class UType extends Instruction {

    public UType(List<Byte> representation, int code) {
        super(representation, code);
    }

    @Override
    protected String parseName() {
        int opcode = extract(representation, 0, 7);
        return switch (opcode) {
            case 0b0110111 -> "lui";
            case 0b0010111 -> "auipc";
            default -> throw new IllegalArgumentException("Illegal funct3, code: 0x" + Integer.toHexString(representation));
        };
    }

    @Override
    protected Integer parseImmediate() {
            return new IntUtils.BitBuilder()
                    .fill(0, 10, extract(representation, 12, 32))
                    .repeat(20, 32, extract(representation, 31, 32))
                    .build();
            // wrong formula?? TODO: check spec
    }

    @Override
    protected Integer parseJumpAddress() {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(extract(representation, 7, 12));
    }
}
