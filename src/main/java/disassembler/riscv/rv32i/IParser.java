package disassembler.riscv.rv32i;

import disassembler.isa.Instruction;
import disassembler.isa.InstructionParser;

import java.util.List;

import static disassembler.util.IntUtils.LEToInt;
import static disassembler.util.IntUtils.getBits;

public class IParser implements InstructionParser {
    public Instruction parse(List<Byte> bytes, int address) {
            int code = LEToInt(bytes);
            int opcode = getBits(code, 0, 7);
            return switch (opcode) {
                case 0b0110011 -> new RType(bytes, address);
                case 0b0010011, 0b0000011, 0b1100111 -> new IType(bytes, address);
                case 0b0100011 -> new SType(bytes, address);
                case 0b1100011 -> new BType(bytes, address);
                case 0b1101111 -> new JType(bytes, address);
                case 0b0110111, 0b0010111 -> new UType(bytes, address);
                case 0b0001111 -> new Fence(bytes, address);
                case 0b1110011 -> new EType(bytes, address);
                default -> throw new IllegalArgumentException("Invalid instruction: " + bytes);
            };
    }
}
