/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.DotVisitor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class DotAnalyzer extends Analyzer {
    private static final Logger log = Logger.getLogger(DotAnalyzer.class.getName());

    public DotAnalyzer(String input, String output, boolean delete) {
        super(input, output, delete);
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            log.warning("Could not parse program in " + fileEntry.getPath());
            return;
        }

        try {
            createDotFile(program, outputPath);
        } catch (IOException e) {
            log.warning("Could not create dot File: " + outputPath);
        }
    }

    private void createDotFile(Program program, String outputPath) throws IOException {
        final Path outputFile = Path.of(outputPath);
        final String dotString = DotVisitor.buildDotGraph(program);

        try (BufferedWriter bw = Files.newBufferedWriter(outputFile)) {
            bw.write(dotString);
        }
    }
}
