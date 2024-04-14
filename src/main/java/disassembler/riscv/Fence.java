package disassembler.riscv;

import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

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
        return List.of(IntUtils.getBits(representation, 24, 28), IntUtils.getBits(representation, 24, 28));
    }
}
