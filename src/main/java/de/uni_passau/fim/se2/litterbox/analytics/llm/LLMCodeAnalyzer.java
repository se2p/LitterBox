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

package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.analytics.FileAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import org.apache.commons.io.file.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LLMCodeAnalyzer extends FileAnalyzer<Program> {

    public LLMCodeAnalyzer(LLMProgramModificationAnalyzer analyzer, Path output, boolean delete) {
        super(analyzer, output, delete);
    }

    public LLMCodeAnalyzer(LLMProgramCompletionAnalyzer analyzer, Path output, boolean delete) {
        super(analyzer, output, delete);
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            throw new IllegalArgumentException("Could not extract program from file: " + file);
        }

        final Program modifiedProgram = analyzer.analyze(program);
        writeResultToFile(file.toPath(), program, modifiedProgram);
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Program modifiedProgram) throws IOException {
        final String extension = PathUtils.getExtension(projectFile);

        if ("json".equals(extension)) {
            JSONFileCreator.writeJsonFromProgram(modifiedProgram, output, "_llm");
        } else if ("sb3".equals(extension)) {
            JSONFileCreator.writeSb3FromProgram(modifiedProgram, output, projectFile.toFile(), "_llm");
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + projectFile);
        }
    }
}
