package disassembler.riscv;

import java.util.List;

public final class Invalid extends Instruction {
    public Invalid(List<Byte> current, int address) {
        super(current, address);
    }

    @Override
    protected String parseName() {
        return null;
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
        return null;
    }
}
