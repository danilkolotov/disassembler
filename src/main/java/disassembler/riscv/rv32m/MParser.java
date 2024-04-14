package disassembler.riscv.rv32m;

import disassembler.isa.Instruction;
import disassembler.isa.InstructionParser;
import disassembler.riscv.rv32i.Invalid;
import disassembler.riscv.rv32m.RType;
import disassembler.util.ByteIterator;

import java.util.ArrayList;
import java.util.List;

import static disassembler.util.IntUtils.LEToInt;
import static disassembler.util.IntUtils.getBits;

public class MParser implements InstructionParser {
    public Instruction parse(List<Byte> bytes, int address) {
        int code = LEToInt(bytes);
        int opcode = getBits(code, 0, 7);
        if (opcode == 0b0110011) {
            return new RType(bytes, address);
        } else {
            throw new IllegalArgumentException("Invalid instruction: " + bytes);
        }
    }
}
