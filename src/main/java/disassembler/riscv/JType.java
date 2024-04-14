package disassembler.riscv;

import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class JType extends Instruction {

    public JType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName() {
        return "jal";
    }

    @Override
    protected Integer parseImmediate() {
        return new IntUtils.BitBuilder()
                .place(0, 0)
                .place(1, 11, IntUtils.getBits(representation, 21, 31))
                .place(11, getBits(representation, 20))
                .place(12, 20, IntUtils.getBits(representation, 12, 20))
                .fill(20, 32, IntUtils.getBits(representation, 31, 32))
                .build() + address;
    }

    @Override
    protected Integer parseJumpAddress() {
        return parseImmediate();
    }

    @Override
    protected List<Integer> parseRegisters() {
        return List.of(IntUtils.getBits(representation, 7, 12));
    }
}
