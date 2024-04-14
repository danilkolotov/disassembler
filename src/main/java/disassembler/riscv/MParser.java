package disassembler.riscv;

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
    public List<Instruction> parse(ByteIterator iterator, int address) {
        List<Instruction> result = new ArrayList<>();
        while (iterator.hasNext(4)) {
            List<Byte> current = iterator.next(4);
            int code = LEToInt(current);
            int opcode = getBits(code, 0, 7);
            if (opcode == 0b0110011) {
                try {
                    result.add(new RType(current, address));
                } catch (IllegalArgumentException e) {
                    result.add(new Invalid(current, address));
                }
            } else {
                result.add(new Invalid(current, address));
            }
            address += 4;
        }
        return result;
    }
}
