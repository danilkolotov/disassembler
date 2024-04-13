package disassembler.riscv;

import java.util.List;

import static disassembler.util.IntUtils.extract;

public final class Fence extends Instruction {
    public Fence(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName() {
        return "fence";
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
        return List.of(extract(representation, 24, 28), extract(representation, 24, 28));
    }
}
