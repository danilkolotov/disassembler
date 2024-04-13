package disassembler.riscv;

import java.util.List;

import static disassembler.util.IntUtils.LEToInt;

public abstract sealed class Instruction permits BType, IType, Invalid, JType, RType, SType, UType {
    protected int representation, address;
    protected String name;
    protected Integer immediate;
    protected List<Integer> registers;
    protected Integer jumpAddress;

    public Instruction(List<Byte> bytes, int address) {
        this.representation = LEToInt(bytes);
        this.address = address;
        this.name = parseName();
        this.immediate = parseImmediate();
        this.registers = parseRegisters();
        this.jumpAddress = parseJumpAddress();
    }

    protected abstract String parseName();

    protected abstract Integer parseImmediate();

    protected abstract Integer parseJumpAddress();

    protected abstract List<Integer> parseRegisters();

    public final String getName() {
        return name;
    }

    public final List<Integer> getRegisters() {
        return registers;
    }

    public final Integer getImmediate() {
        return immediate;
    }

    public final Integer getJumpAddress() {
        return jumpAddress;
    }

    public final int getAddress() {
        return address;
    }

    public final int getCode() {
        return representation;
    }
}
