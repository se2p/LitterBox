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
package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class JSONFileCreator {

    public static void writeJsonFromProgram(Program program) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        }
    }

    public static void writeJsonFromProgram(Program program, String output) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        Path outPath = Paths.get(output, program.getIdent().getName() + "_annotated.json");
        try (PrintWriter out = new PrintWriter(outPath.toString())) {
            out.println(jsonString);
        }
    }

    public static void writeSb3FromProgram(Program program, String output, File file) throws IOException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        String destinationPath = Paths.get(output, program.getIdent().getName() + "_annotated.sb3").toString();
        Path tmp = Files.createTempDirectory("litterbox_");

        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        }

        ZipFile zipFile = new ZipFile(file);
        zipFile.extractAll(String.valueOf(tmp));

        File tempProj = new File(tmp + "/project.json");
        File annotatedJson = new File(program.getIdent().getName() + "_annotated.json");

        Files.copy(annotatedJson.toPath(), tempProj.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(annotatedJson.toPath());
        File tempDir = new File(String.valueOf(tmp));

        File[] files = tempDir.listFiles();
        if (files != null) {
            ZipFile zip = new ZipFile(destinationPath);
            zip.addFiles(Arrays.asList(files));
        }

        FileUtils.deleteDirectory(tempDir);
    }
}
