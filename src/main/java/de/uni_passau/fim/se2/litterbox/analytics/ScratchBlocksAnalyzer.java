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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScratchBlocksAnalyzer extends FileAnalyzer<String> {

    private BufferedWriter writer;

    public ScratchBlocksAnalyzer(Path output, boolean delete) {
        super(new ProgramScratchBlocksAnalyzer(), output, delete);
    }

    @Override
    protected void beginAnalysis() throws IOException {
        if (output != null) {
            writer = Files.newBufferedWriter(output);
        }
    }

    @Override
    protected void endAnalysis() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, String scratchBlocks) throws IOException {
        if (output == null) {
            System.out.println(scratchBlocks);
        } else {
            writer.write(scratchBlocks);
            writer.newLine();
        }
    }
}
