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

        try (PrintWriter out = new PrintWriter(output + program.getIdent().getName() + "_annotated.json")) {
            out.println(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeSb3FromProgram(Program program, String output, File file) throws IOException {
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        String destinationPath = output + program.getIdent().getName() + "_annotated.zip";


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
