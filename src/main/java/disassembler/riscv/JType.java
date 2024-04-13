package disassembler.riscv;

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
        int res = 0b11111111111100000000000000000000 * extract(representation, 31, 32);
        res += extract(representation, 12, 20) << 12;
        res += extract(representation, 20, 21) << 11;
        res += extract(representation, 21, 31) << 1;
        return res;
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
