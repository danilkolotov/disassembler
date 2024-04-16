package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public class RType extends Instruction {

    public RType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName(int representation) {
        int funct3 = getBits(representation, 12, 15);
        int funct7 = getBits(representation, 25, 32);
        if (funct7 != 0b0000000 && funct7 != 0b0100000) {
            throw error();
        }
        return switch (funct3) {
            case 0b000 -> switch (funct7) {
                case 0b0000000 -> "add";
                case 0b0100000 -> "sub";
                default -> throw error();
            };
            case 0b001 -> "sll";
            case 0b010 -> "slt";
            case 0b011 -> "sltu";
            case 0b100 -> "xor";
            case 0b101 -> switch (funct7) {
                case 0b0000000 -> "srl";
                case 0b0100000 -> "sra";
                default -> throw error();
            };
            case 0b110 -> "or";
            case 0b111 -> "and";
            default -> throw error();
        };
    }
    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(
            IntUtils.getBits(representation, 7, 12),
            IntUtils.getBits(representation, 15, 20),
            IntUtils.getBits(representation, 20, 25)
        );
    }
}
