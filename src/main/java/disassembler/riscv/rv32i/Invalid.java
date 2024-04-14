package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;

import java.util.List;

public final class Invalid extends Instruction {
    public Invalid(List<Byte> current, int address) {
        super(current, address);
    }
}
