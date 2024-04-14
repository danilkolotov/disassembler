package disassembler.riscv;

import disassembler.isa.Instruction;

import java.util.List;

public final class Invalid extends Instruction {
    public Invalid(List<Byte> current, int address) {
        super(current, address);
    }

    @Override
    protected String parseName(int representation) {
        return null;
    }

    @Override
    protected Integer parseImmediate(int representation) {
        return null;
    }

    @Override
    protected Integer parseJumpAddress(int representation) {
        return null;
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return null;
    }
}
