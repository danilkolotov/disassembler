package disassembler.riscv;

import java.util.List;

import static disassembler.util.IntUtils.extract;

public final class IType extends Instruction {

    public IType(List<Byte> bytes, int address) {
        super(bytes, address);
    }

    @Override
    protected String parseName() {
        int opcode = extract(representation, 0, 7);
        int funct3 = extract(representation, 12, 15);
        int funct7 = extract(representation, 25, 32);
        if (opcode == 0b0010011) {
            return switch (funct3) {
                case 0b000 -> "addi";
                case 0b010 -> "slti";
                case 0b011 -> "sltiu";
                case 0b100 -> "xori";
                case 0b110 -> "ori";
                case 0b111 -> "andi";
                case 0b001 -> "slli";
                case 0b101 -> switch (funct7){
                    case 0b0000000 -> "srli";
                    case 0b0100000 -> "srai";
                    default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
                };
                default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
            };
        }
        if (opcode == 0b0000011) {
            return switch (funct3) {
                case 0b000 -> "lb";
                case 0b001 -> "lh";
                case 0b010 -> "lw";
                case 0b100 -> "lbu";
                case 0b101 -> "lhu";
                default -> throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
            };
        }
        if (opcode == 0b1100111) {
            if (funct3 == 0b000) {
                return "jalr";
            }
        }
        throw new IllegalArgumentException("Illegal funct3 & funct7 combination, code: 0x" + Integer.toHexString(representation));
    }

    @Override
    protected Integer parseImmediate() {
        int funct3 = extract(representation, 12, 15);
        if (funct3 == 0b101) {
            return extract(representation, 20, 25);
        }
        if (funct3 == 0b001) {
            return parseImmediateImpl(representation << extract(parseImmediateImpl(representation), 20, 25));
        }
        return parseImmediateImpl(representation);
    }

    private int parseImmediateImpl(int code){
        int res = 0b11111_11111_11111_11111000_000_000_000 * extract(code, 31, 32);
        res += extract(code, 20, 32);
        return res;
    }

    @Override
    protected Integer parseJumpAddress() {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(extract(representation, 7, 12), extract(representation, 15, 20));
    }

}
