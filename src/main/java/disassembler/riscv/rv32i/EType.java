package disassembler.riscv.rv32i;

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
}
