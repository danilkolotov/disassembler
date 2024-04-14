package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.util.IntUtils;

import java.util.List;

import static disassembler.util.IntUtils.getBits;

public final class Fence extends Instruction {
    public Fence(List<Byte> representation, int address) {
        super(representation, address);
    }

    @Override
    protected String parseName(int representation) {
        return "fence";
    }

    @Override
    protected List<Integer> parseRegisters(int representation) {
        return List.of(getBits(representation, 24, 28), getBits(representation, 24, 28));
    }
}
