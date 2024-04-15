package disassembler.isa;

import java.util.List;
import java.util.Optional;

import static disassembler.util.IntUtils.LEToInt;

public abstract class Instruction {
    protected int representation, address;
    protected String name;
    protected Integer immediate;
    protected List<Integer> registers;
    protected Integer jumpAddress;

    public Instruction(List<Byte> bytes, int address) {
        this.representation = LEToInt(bytes);
        this.address = address;
        this.name = parseName(representation);
        this.immediate = parseImmediate(representation);
        this.registers = parseRegisters(representation);
        this.jumpAddress = parseJumpAddress(representation);
    }

    protected abstract String parseName(int representation);

    protected Integer parseImmediate(int representation) {
        return null;
    }

    protected Integer parseJumpAddress(int representation) {
        return null;
    }

    protected List<Integer> parseRegisters(int representation) {
        return null;
    }

    public final String getName() {
        return name;
    }

    public final List<Integer> getRegisters() {
        return check(registers, "registers");
    }

    public final Integer getImmediate() {
        return check(immediate, "immediate");
    }

    public final Integer getJumpAddress() {
        return check(jumpAddress, "jump address");
    }

    public final int getAddress() {
        return address;
    }

    public final int getCode() {
        return representation;
    }

    private <T> T check(T field, String fieldName) {
        if (field == null) {
            throw new UnsupportedOperationException("Can't get " + fieldName + " from instruction " + name);
        }
        return field;
    }
}
