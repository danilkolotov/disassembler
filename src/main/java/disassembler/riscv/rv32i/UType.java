package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class UType extends Instruction {

    public UType(List<Byte> representation, int code) {
        super(representation, code);
    }

    @Override
    protected String parseName(int representation) {
        int opcode = IntUtils.getBits(representation, 0, 7);
        return switch (opcode) {
            case 0b0110111 -> "lui";
            case 0b0010111 -> "auipc";
            default -> throw new IllegalArgumentException("Illegal funct3, code: 0x" + Integer.toHexString(representation));
        };
    }

    @Override
    protected Integer parseImmediate(int representation) {
//            return new IntUtils.BitBuilder()
//                    .place(0, 10, getBits(representation, 12, 32))
//                    .fill(20, 32, getBits(representation, 31, 32))
//                    .build();
        return new IntUtils.BitBuilder()
                .fill(0, 12, 0)
                .place(12, 32, getBits(representation, 12, 32))
                .build() >> 12;
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(getBits(representation, 7, 12));
    }
}
