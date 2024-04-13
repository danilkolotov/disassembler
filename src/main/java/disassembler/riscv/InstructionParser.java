package disassembler.riscv;

import disassembler.util.ByteIterator;

import java.util.ArrayList;
import java.util.List;

import static disassembler.util.IntUtils.LEToInt;
import static disassembler.util.IntUtils.extract;

public class InstructionParser {
    public static List<Instruction> parse(ByteIterator iterator, int address) {
        List<Instruction> result = new ArrayList<>();
        while (iterator.hasNext(4)) {
            List<Byte> current = iterator.next(4);
            int code = LEToInt(current);
            int opcode = extract(code, 0, 7);
            try {
                result.add(switch (opcode) {
                    case 0b0110011 -> new RType(current, address);
                    case 0b0010011, 0b0000011, 0b1100111, 0b1110011 -> new IType(current, address);
                    case 0b0100011 -> new SType(current, address);
                    case 0b1100011 -> new BType(current, address);
                    case 0b1101111 -> new JType(current, address);
                    case 0b0110111, 0b0010111 -> new UType(current, address);
                    //                case 0b0001111 -> new Fence(current, address);
//                    case 0b1110011 -> new EType(current, address);
                    default -> new Invalid(current, address);
                });
            } catch (IllegalArgumentException e) {
                result.add(new Invalid(current, address));
            }
            address += 4;
        }
        return result;
    }
}
