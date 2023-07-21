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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public final class JSONFileCreator {

    private JSONFileCreator() {
    }

    private static final String JSON = ".json";
    private static final String SB3 = ".sb3";
    private static final String MBLOCK = ".mblock";

    public static void writeJsonFromProgram(Program program, String postfix) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + postfix + JSON)) {
            out.println(jsonString);
        }
    }

    public static void writeJsonFromProgram(Program program, Path output, String postfix) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        Path outPath = output.resolve(program.getIdent().getName() + postfix + JSON);
        try (PrintWriter out = new PrintWriter(outPath.toString())) {
            out.println(jsonString);
        }
    }

    public static void writeSb3FromProgram(Program program, Path outputPath, File sourceSB3File, String postfix) throws IOException {
        writeBinary(program, outputPath, sourceSB3File, postfix, SB3);
    }

    private static void writeBinary(Program program, Path outputPath, File sourceFile, String postfix, String fileExtension) throws IOException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        Path destinationPath = outputPath.resolve(program.getIdent().getName() + postfix + fileExtension);
        Path tmp = Files.createTempDirectory("litterbox_");

        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + postfix + JSON)) {
            out.println(jsonString);
        }

        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            zipFile.extractAll(String.valueOf(tmp));

            Path tempProj = tmp.resolve("project.json");
            Path annotatedJson = Path.of(program.getIdent().getName() + postfix + JSON);

            Files.copy(annotatedJson, tempProj, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(annotatedJson);

            File[] files = tmp.toFile().listFiles();
            if (files != null) {
                try (ZipFile zip = new ZipFile(destinationPath.toFile())) {
                    zip.addFiles(Arrays.asList(files));
                }
            }

            FileUtils.deleteDirectory(tmp.toFile());
        }
    }

    public static void writeMBlockFromProgram(Program program, Path outputPath, File sourceSB3File, String postfix) throws IOException {
        writeBinary(program, outputPath, sourceSB3File, postfix, MBLOCK);
    }
}
