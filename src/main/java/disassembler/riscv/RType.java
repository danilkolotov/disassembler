package disassembler.riscv;

import java.util.List;

import static disassembler.util.IntUtils.extract;

public final class RType extends Instruction {

    public RType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName() {
        int funct3 = extract(representation, 12, 15);
        int funct7 = extract(representation, 25, 32);
        return switch (funct3) {
            case 0b000 -> switch (funct7) {
                case 0b0000000 -> "add";
                case 0b0100000 -> "sub";
                default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
            };
            case 0b001 -> "sll";
            case 0b010 -> "slt";
            case 0b011 -> "sltu";
            case 0b100 -> "xor";
            case 0b101 -> switch (funct7) {
                case 0b0000000 -> "srl";
                case 0b0100000 -> "sra";
                default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
            };
            case 0b110 -> "or";
            case 0b111 -> "and";
            default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
        };
    }

    @Override
    protected Integer parseImmediate() {
        return null;
    }

    @Override
    protected Integer parseJumpAddress() {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(
            extract(representation, 7, 12),
            extract(representation, 15, 20),
            extract(representation, 20, 25)
        );
    }
}
