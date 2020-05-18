package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class JSONFileCreator {

    public static void writeJsonFromProgram(Program program){
        String jsonString = JSONStringCreator.createProgramJSONString(program);
        try (PrintWriter out = new PrintWriter(program.getIdent().getName()+"_restored.json")) {
            out.println(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
