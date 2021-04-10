/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.jsoncreation.enums.FilePostfix;
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

    private JSONFileCreator() {
    }

    private static final String JSON = ".json";
    private static final String SB3 = ".sb3";

    public static void writeJsonFromProgram(Program program, FilePostfix postfix) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + postfix + JSON)) {
            out.println(jsonString);
        }
    }

    public static void writeJsonFromProgram(Program program, String output, FilePostfix postfix) throws FileNotFoundException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        Path outPath = Paths.get(output, program.getIdent().getName() + postfix + JSON);
        try (PrintWriter out = new PrintWriter(outPath.toString())) {
            out.println(jsonString);
        }
    }

    public static void writeSb3FromProgram(Program program, String output, File file, FilePostfix postfix) throws IOException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);

        String destinationPath = Paths.get(output, program.getIdent().getName() + postfix + SB3).toString();
        Path tmp = Files.createTempDirectory("litterbox_");

        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + postfix + JSON)) {
            out.println(jsonString);
        }

        ZipFile zipFile = new ZipFile(file);
        zipFile.extractAll(String.valueOf(tmp));

        File tempProj = new File(tmp + "/project.json");
        File annotatedJson = new File(program.getIdent().getName() + postfix + JSON);

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
