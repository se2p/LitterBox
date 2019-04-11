package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import utils.deserializer.scratch2.SpriteDeserializer;
import utils.deserializer.scratch2.StageDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Util class for parsing the JSON files
 */
public class JsonParser {

    /**
     * A method for parsing the Scratch Project out of a zipped Scratch project file
     *
     * @param fileName the file name
     * @param path     the file path
     * @return the parsed Scratch Project
     */
    public static Project parse(String fileName, String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Project project = new Project();
            project.setName(fileName);
            project.setFilenameExtension(".sb2");
            project.setPath(path);
            JsonNode rootNode = mapper.readTree(ZipReader.getJson(path));
            Iterator<JsonNode> script = rootNode.path("scripts").elements();
            project.setStage(StageDeserializer.deserialize(rootNode));
            project.setSprites(SpriteDeserializer.deserialize(rootNode));
            return project;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A method for parsing the Scratch Project out of a raw JSON file
     *
     * @param file the Scratch project JSON file
     * @return the parsed Scratch Project
     */
    public static Project parseRaw(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Project project = new Project();
            project.setName(file.getName());
            project.setFilenameExtension(".sb");
            project.setPath(file.getPath());
            JsonNode rootNode = mapper.readTree(file);
            project.setStage(StageDeserializer.deserialize(rootNode));
            project.setSprites(SpriteDeserializer.deserialize(rootNode));
            return project;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A method to make scripts readable in the console
     *
     * @param scripts list opf scripts
     * @return a String for pretty console output
     */
    public static String prettyPrintScript(List<Script> scripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Scripts:");
        int i = 0;
        for (Script sc : scripts) {
            sb.append("\n");
            sb.append("*****Script_").append(i).append("*****\nPosition: x ").append(sc.getPosition()[0]).append(", y ").append(sc.getPosition()[1]);
            sb.append("\nBlocks:\n");
            for (ScBlock b : sc.getBlocks()) {
                prettyBlocks(sb, b, 0);
            }
            sb.append("*****Script_").append(i).append("*****");
            i++;
        }
        return sb.toString();
    }

    /**
     * A recursive method appending a pretty String version of the block and its nested blocks to a given StringBuilder
     *
     * @param sb the StringBuilder to append the given block in pretty form
     * @param b  the block to append
     * @param x  the recursion level to simulate the tree structure with readable indents in the console output
     */
    private static void prettyBlocks(StringBuilder sb, ScBlock b, int x) {
        for (int y = 0; y < x; y++) {
            sb.append("~~~~");
        }
        sb.append("    [ScBlock{" + "content='").append(b.getContent()).append('\'').append("}]\n");
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            x = x + 1;
        }
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            for (ScBlock nb : b.getNestedBlocks()) {
                prettyBlocks(sb, nb, x);
            }
        }
        if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
            for (int y = 0; y < x; y++) {
                sb.append("~~~~");
            }
            sb.append("    ELSE\n");
            for (ScBlock nb : b.getElseBlocks()) {
                prettyBlocks(sb, nb, x);
            }
        }
    }
}
