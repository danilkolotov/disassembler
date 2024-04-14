package disassembler.riscv;

import disassembler.util.IntUtils;

import java.util.List;
import java.util.function.Function;

import static disassembler.util.IntUtils.extract;

public final class BType extends Instruction {
    public BType(List<Byte> bytes, int address) {
        super(bytes, address);
    }

    @Override
    protected String parseName() {
        int funct3 = extract(representation, 12, 15);
        return switch (funct3) {
            case 0b000 -> "beq";
            case 0b001 -> "bne";
            case 0b100 -> "blt";
            case 0b101 -> "bge";
            case 0b110 -> "bltu";
            case 0b111 -> "bgeu";
            default -> throw new IllegalArgumentException("Incorrect funct3 for B-type instruction: " + funct3);
        };
    }

    @Override
    protected Integer parseImmediate() {
        return new IntUtils.BitBuilder()
                .place(0, 0)
                .fill(1, 5, extract(representation, 8, 12))
                .fill(5, 11, extract(representation, 25, 31))
                .place(11, extract(representation, 7))
                .repeat(12, 32, extract(representation, 31))
                .build();
    }

    @Override
    protected Integer parseJumpAddress() {
        return parseImmediate() + address;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(extract(representation, 15, 20), extract(representation, 20, 25));
    }


}
