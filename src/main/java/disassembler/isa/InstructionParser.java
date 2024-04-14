package disassembler.isa;

import disassembler.util.ByteIterator;

import java.util.List;

public interface InstructionParser {
    List<Instruction> parse(ByteIterator iterator, int address);
}
