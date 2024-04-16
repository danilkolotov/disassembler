package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class BType extends Instruction {
    public BType(List<Byte> bytes, int address) {
        super(bytes, address);
    }

    @Override
    protected String parseName(int representation) {
        int funct3 = IntUtils.getBits(this.representation, 12, 15);
        return switch (funct3) {
            case 0b000 -> "beq";
            case 0b001 -> "bne";
            case 0b100 -> "blt";
            case 0b101 -> "bge";
            case 0b110 -> "bltu";
            case 0b111 -> "bgeu";
            default -> throw error();
        };
    }

    @Override
    protected Integer parseImmediate(int representation) {
        return new IntUtils.BitBuilder()
                .place(0, 0)
                .place(1, 5, IntUtils.getBits(representation, 8, 12))
                .place(5, 11, IntUtils.getBits(representation, 25, 31))
                .place(11, getBits(representation, 7))
                .fill(12, 32, getBits(representation, 31))
                .build();
    }

    @Override
    protected Integer parseJumpAddress(int representation) {
        return parseImmediate(representation) + address;
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(getBits(representation, 15, 20), getBits(representation, 20, 25));
    }


}
