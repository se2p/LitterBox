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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;

public class JSONFileCreator {

    public static void writeJsonFromProgram(Program program) {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeJsonFromProgram(Program program, String output) {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        if (output.charAt(output.length() - 1) != '/') {
            output = output + "/";
        }
        try (PrintWriter out = new PrintWriter(output + program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeSb3FromProgram(Program program, String output, File file) throws IOException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        if (output.charAt(output.length() - 1) != '/') {
            output = output + "/";
        }
        String destinationPath = output + program.getIdent().getName() + "_annotated.sb3";


        FileUtils.copyFile(file, new File(destinationPath));


        try (PrintWriter out = new PrintWriter(program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        File annotatedJson = new File(program.getIdent().getName() + "_annotated.json");

        Path zipFilePath = Paths.get(destinationPath);
        FileSystem fs = FileSystems.newFileSystem(zipFilePath, null);
        Path source = fs.getPath("/project.json");
        Files.delete(source);
        Files.copy(annotatedJson.toPath(), source);
        Files.delete(annotatedJson.toPath());


    }
}
