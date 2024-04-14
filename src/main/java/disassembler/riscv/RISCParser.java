package disassembler.riscv;

import disassembler.isa.Instruction;
import disassembler.isa.InstructionParser;
import disassembler.riscv.rv32i.IParser;
import disassembler.riscv.rv32i.Invalid;
import disassembler.riscv.rv32m.MParser;
import disassembler.util.ByteIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RISCParser {
    private static final List<InstructionParser> parsers = List.of(
        new IParser(),
        new MParser()
    );
    public static List<Instruction> parse(ByteIterator iterator, int address) {
        List<Instruction> result = new ArrayList<>();
        while (iterator.hasNext(4)) {
            List<Byte> current = iterator.next(4);
            Instruction toAdd = null;
            for (InstructionParser parser : parsers) {
                try {
                    toAdd = parser.parse(current,  address);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                break;
            }
            result.add(Optional.ofNullable(toAdd).orElse(new Invalid(current, address)));
            address += 4;
        }
        return result;
    }
}
