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

import de.uni_passau.fim.se2.litterbox.analytics.code2vec.GeneratePathTask;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Code2VecAnalyzer extends Analyzer {
    private static final Logger log = Logger.getLogger(Code2VecAnalyzer.class.getName());
    private final int maxPathLength;
    private final boolean includeStage;
    private final boolean wholeProgram;

    public Code2VecAnalyzer(String input, String output, int maxPathLength, boolean includeStage, boolean wholeProgram, boolean delete) {
        super(input, output, delete);
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            log.warning("Program was null. File name was '" + fileEntry.getName() + "'");
            return;
        }

        GeneratePathTask generatePathTask = new GeneratePathTask(program, maxPathLength, includeStage, wholeProgram);
        String code2VecInput = generatePathTask.createContextForCode2Vec();

        if (code2VecInput.length() > 0) {
            if (outputPath.equals("CONSOLE")) {
                System.out.println(code2VecInput);
            } else {
                writeToFile(fileEntry.getName(), outputPath, code2VecInput);
            }
        }
    }

    private void writeToFile(String fileName, String outputPath, String code2VecInput) {
        if (!Paths.get(outputPath).toFile().isDirectory()) {
            log.warning("Output path must be a folder");
            return;
        }

        PrintStream stream;
        String outName = fileName.substring(0, fileName.lastIndexOf("."));

        try {
            Path outPath = Paths.get(outputPath, outName);
            stream = new PrintStream(outPath.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("Creation of output stream not possible with output file " + outName);
            return;
        }
        log.info("Starting to print " + fileName + " to file " + outputPath);
        stream.print(code2VecInput);
        stream.close();
        log.info("Finished printing.");
    }
}
