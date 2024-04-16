package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class IType extends Instruction {

    public IType(List<Byte> bytes, int address) {
        super(bytes, address);
    }

    @Override
    protected String parseName(int representation) {
        int opcode = IntUtils.getBits(representation, 0, 7);
        int funct3 = IntUtils.getBits(representation, 12, 15);
        int funct7 = IntUtils.getBits(representation, 25, 32);
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
                    default -> throw error();
                };
                default -> throw error();
            };
        }
        if (opcode == 0b0000011) {
            return switch (funct3) {
                case 0b000 -> "lb";
                case 0b001 -> "lh";
                case 0b010 -> "lw";
                case 0b100 -> "lbu";
                case 0b101 -> "lhu";
                default -> throw error();
            };
        }
        if (opcode == 0b1100111) {
            if (funct3 == 0b000) {
                return "jalr";
            }
        }
        throw error();
    }

    @Override
    protected Integer parseImmediate(int representation) {
        int funct3 = getBits(representation, 12, 15);
        if (funct3 == 0b101) {
            return getBits(representation, 20, 25);
        }
        if (funct3 == 0b001) {
            return parseImmediateImpl(representation << getBits(parseImmediateImpl(representation), 20, 25));
        }
        return parseImmediateImpl(representation);
    }

    private int parseImmediateImpl(int code){
        return new IntUtils.BitBuilder()
                .place(0, 12, IntUtils.getBits(code, 20, 32))
                .fill(12, 32, getBits(code, 31))
                .build();
    }

    @Override
    protected Integer parseJumpAddress(int representation) {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(IntUtils.getBits(representation, 7, 12), IntUtils.getBits(representation, 15, 20));
    }

}
