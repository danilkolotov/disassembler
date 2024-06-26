package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class JType extends Instruction {

    public JType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName(int representation) {
        return "jal";
    }

    @Override
    protected Integer parseImmediate(int representation) {
        return new IntUtils.BitBuilder()
                .place(0, 0)
                .place(1, 11, getBits(representation, 21, 31))
                .place(11, getBits(representation, 20))
                .place(12, 20, getBits(representation, 12, 20))
                .fill(20, 32, getBits(representation, 31, 32))
                .build() + address;
    }

    @Override
    protected Integer parseJumpAddress(int representation) {
        return parseImmediate(representation);
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(getBits(representation, 7, 12));
    }
}
