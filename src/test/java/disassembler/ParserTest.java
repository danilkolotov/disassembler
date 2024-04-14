package disassembler;

import disassembler.riscv.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static disassembler.util.IntUtils.extract;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    private static final Path resources = Paths.get("src", "test", "resources");


    @Test
    public void testSamples() {
        int samplesNumber = 3;
        for (int i = 3; i <= samplesNumber; i++) {
            Path samplePath = resources.resolve(Paths.get(Integer.toString(i), "test.elf"));
            Path correctPath = resources.resolve(Paths.get(Integer.toString(i), "answer.txt"));
            try {
                String toCheck = new Parser(Files.readAllBytes(samplePath)).parse();
                assertEquals(String.join("\n" , Files.readAllLines(correctPath)), toCheck.trim(), "Sample number " + i + " failed.");
            } catch (IOException e) {
                throw new IllegalStateException("Can't read from test files, sample " + i, e);
            }
        }
    }
}
