package disassembler.riscv;

import java.util.List;

public final class EType extends Instruction {
    public EType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName() {
        return "ecall";
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
