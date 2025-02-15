/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LLMQueryAnalyzer extends FileAnalyzer<String> {

    public LLMQueryAnalyzer(
            Path output,
            boolean delete,
            LlmQuery query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        super(new LLMProgramQueryAnalyzer(query, target, ignoreLooseBlocks), output, delete);
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            throw new IllegalArgumentException("Could not extract program from file: " + file);
        }

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
