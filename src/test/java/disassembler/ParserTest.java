package disassembler;

import disassembler.riscv.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    private static final Path resources = Paths.get("src", "test", "resources");


    @Test
    public void testSamples() {
        int samplesNumber = 3;
        for (int i = 1; i <= samplesNumber; i++) {
            Path samplePath = resources.resolve(Paths.get(Integer.toString(i), "test.elf"));
            Path correctPath = resources.resolve(Paths.get(Integer.toString(i), "answer.txt"));
            try {
                String toCheck = new Parser(Files.readAllBytes(samplePath)).parse(ParserTest::skkvStrategy);
                assertEquals(String.join("\n" , Files.readAllLines(correctPath)), toCheck.trim(), "Sample number " + i + " failed.");
            } catch (IOException e) {
                throw new IllegalStateException("Can't read from test files, sample " + i, e);
            }
        }
    }

    private static String skkvStrategy(Instruction instruction) {
        String name = instruction.getName();
        List<Integer> registers = instruction.getRegisters();
        Integer immediate = instruction.getImmediate();

        return switch (instruction) {
            case BType b -> "b";
            case Invalid inv -> "b";
            case IType i -> "b";
            case JType j -> "b";
            case RType r -> "b";
            case SType s -> "b";
            case UType u -> "b";
        };
    }
}
