package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LLMAnalyzer extends FileAnalyzer<String> {

    public LLMAnalyzer(Path output, boolean delete, String query, String spriteName, String detectors, boolean ignoreLooseBlocks, boolean fix) {
        super(new ProgramLLMAnalyzer(query, spriteName, detectors, ignoreLooseBlocks, fix), output, delete);
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            // TODO Error handling
            return;
        }

        // TODO: Result may not be a string if we aim to fix a program, may need to refactor in two different analyzers?
        final String result = analyzer.analyze(program);
        if (output == null) {
            System.out.println(result);
        } else {
            writeResultToFile(file.toPath(), program, result);
        }
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, String scratchBlocks) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(output)) {
            bw.write(scratchBlocks);
        }
    }
}
