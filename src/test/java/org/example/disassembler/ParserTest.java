package org.example.disassembler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                String toCheck = new ELFParser(Files.readAllBytes(samplePath)).parse();
                assertEquals(String.join("\n" , Files.readAllLines(correctPath)), toCheck.trim(), "Sample number " + i + " failed.");
            } catch (IOException e) {
                throw new IllegalStateException("Can't read from test files, sample " + i, e);
            }
        }
    }
}
