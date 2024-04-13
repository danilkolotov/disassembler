package disassembler.riscv;

import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.extract;

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
                .fill(1, 11, extract(representation, 21, 31))
                .place(11, extract(representation, 20))
                .fill(12, 20, extract(representation, 20, 21))
                .repeat(20, 32, extract(representation, 31, 32))
                .build();
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
