package disassembler;

import disassembler.riscv.*;
import disassembler.util.ByteIterator;
import disassembler.util.IntUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static disassembler.util.IntUtils.getBits;
import static org.junit.jupiter.api.Assertions.*;

// only one test for each instruction
// more tests in ParserTest
public class InstructionParserTest {
    private Instruction parse(int code, int address) {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            bytes.add((byte) IntUtils.getBits(code, i * 8, i * 8 + 8));
        }
        return InstructionParser.parse(new ByteIterator(bytes, 0), address).get(0);
    }

    @Test
    public void bTypeTest() {
        int code = 0x00050863;
        int address = 0x100b0;
        Instruction parsed = parse(code, address);
        assertTrue(parsed instanceof BType);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("beq", parsed.getName());
        assertIterableEquals(List.of(10, 0), parsed.getRegisters());
        assertEquals(16, parsed.getImmediate());
        assertEquals(0x100c0, parsed.getJumpAddress());
    }

    @Test
    public void iTypeTest() {
        int code = 0x00000793;
        //10010011
        int address = 0x100f0;
        Instruction parsed = parse(code, address);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("addi", parsed.getName());
        assertIterableEquals(List.of(15, 0), parsed.getRegisters());
        assertEquals(0, parsed.getImmediate());
        assertEquals(null, parsed.getJumpAddress());
    }

    @Test
    public void jTypeTest() {
        int code = 0xfddff0ef;
        int address = 0x10098;
        Instruction parsed = parse(code, address);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("jal", parsed.getName());
        assertIterableEquals(List.of(1), parsed.getRegisters());
        assertEquals(0x10074, parsed.getImmediate());
        assertEquals(0x10074, parsed.getJumpAddress());
    }

    @Test
    public void invalidTest() {
        assertInstanceOf(Invalid.class, parse(0, 0));
    }

    @Test
    public void rTypeTest() {
        int code = 0x00f707b3;
        int address = 0x10174;
        Instruction parsed = parse(code, address);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("add", parsed.getName());
        assertIterableEquals(List.of(15, 14, 15), parsed.getRegisters());
        assertEquals(null, parsed.getImmediate());
        assertEquals(null, parsed.getJumpAddress());
    }

    @Test
    public void sTypeTest() {
        int code = 0xc2f18a23;
        int address = 0x10110;
        Instruction parsed = parse(code, address);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("sb", parsed.getName());
        assertIterableEquals(List.of(3, 15), parsed.getRegisters());
        assertEquals(-972, parsed.getImmediate());
        assertEquals(null, parsed.getJumpAddress());
    }

    @Test
    public void uTypeTest() {
        int code = 0x00011537;
        int address = 0x1012c;
        Instruction parsed = parse(code, address);
        assertEquals(address, parsed.getAddress());
        assertEquals(code, parsed.getCode());
        assertEquals("lui", parsed.getName());
        assertIterableEquals(List.of(10), parsed.getRegisters());
        assertEquals(0x11, parsed.getImmediate());
        assertEquals(null, parsed.getJumpAddress());
    }

}
