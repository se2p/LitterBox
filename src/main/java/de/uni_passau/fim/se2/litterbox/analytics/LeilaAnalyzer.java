/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.LeilaVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class LeilaAnalyzer extends Analyzer {

    private static final String INTERMEDIATE_EXTENSION = ".sc";
    private static final Logger log = Logger.getLogger(MetricAnalyzer.class.getName());
    private final boolean nonDet;
    private final boolean onNever;

    /**
     * Constructor for the leila analyzer.
     *
     * @param input path to folder or file that should be analyzed
     * @param output Path to file or folder for the resulting .sc file(s);
     *               has to be a folder if multiple projects are analysed
     *               (file will be created if not existing yet, path has to exist
     * @param nonDet flag whether attributes in intermediate language should be
     *               non deterministic (i.e. not initialized)
     */
    public LeilaAnalyzer(String input, String output, boolean nonDet, boolean onNever) {
        super(input, output);
        this.nonDet = nonDet;
        this.onNever = onNever;

    }

    @Override
    void check(File fileEntry, String out) {
        if (!Paths.get(out).toFile().isDirectory()) {
            log.warning("Output path must be a folder");
            return;
        }

        PrintStream stream;
        String outName = getIntermediateFileName(fileEntry.getName());

        try {
            Path outPath = Paths.get(out, outName);
            stream = new PrintStream(outPath.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("Creation of output stream not possible with output file " + outName);
            return;
        }
        log.info("Starting to print " + fileEntry.getName() + " to file " + out);
        LeilaVisitor visitor = new LeilaVisitor(stream, nonDet, onNever);
        Program program = extractProgram(fileEntry);
        visitor.visit(program);
        stream.close();
        log.info("Finished printing.");
    }

    private String getIntermediateFileName(String name) {
        String programName = name.substring(0, name.lastIndexOf("."));
        return programName + INTERMEDIATE_EXTENSION;
    }
}
