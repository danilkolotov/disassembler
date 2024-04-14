package disassembler.isa;

import java.util.List;

@FunctionalInterface
public interface InstructionParser {
    Instruction parse(List<Byte> bytes, int address);
}
