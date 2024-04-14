package disassembler.riscv;

import disassembler.isa.Instruction;

import java.util.List;

public final class EType extends Instruction {
    public EType(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName(int representation) {
        return "ecall";
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
