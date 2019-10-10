package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Sprite;
import utils.deserializer.scratch2.SpriteDeserializer;
import utils.deserializer.scratch2.StageDeserializer;
import utils.deserializer.scratch3.SpriteDeserializer3;
import utils.deserializer.scratch3.StageDeserializer3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Util class for parsing the JSON files
 */
public class JsonParser {

    /**
     * SCRATCH 3.0
     * A method for parsing the Scratch Project out of a zipped Scratch project file
     *
     * @param fileName the file name
     * @param path     the file path
     * @return the parsed Scratch Project
     */
    public static Project parse3(String fileName, String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Project project = new Project();
            project.setName(fileName);
            project.setFilenameExtension(".sb3");
            project.setVersion(Version.SCRATCH3);
            project.setPath(path);
            JsonNode rootNode = mapper.readTree(ZipReader.getJson(path));
            List<Sprite> sprites = new ArrayList<>();
            if (!rootNode.has("targets")) {
                return null;
            }
            Iterator<JsonNode> elements = rootNode.get("targets").elements();
            while (elements.hasNext()) {
                JsonNode c = elements.next();
                if (c.has("isStage")) {
                    if (c.get("isStage").asBoolean()) {
                        project.setStage(StageDeserializer3.deserialize(c));
                    } else {
                        Sprite sprite3 = SpriteDeserializer3.deserialize(c);
                        sprites.add(sprite3);
                    }
                }
            }
            project.setSprites(sprites);
            return project;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * SCRATCH 2.0
     * A method for parsing the Scratch Project out of a zipped Scratch project file
     *
     * @param fileName the file name
     * @param path     the file path
     * @return the parsed Scratch Project
     */
    public static Project parse2(String fileName, String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Project project = new Project();
            project.setName(fileName);
            project.setFilenameExtension(".sb2");
            project.setVersion(Version.SCRATCH2);
            project.setPath(path);
            JsonNode rootNode = mapper.readTree(ZipReader.getJson(path));
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
            project.setFilenameExtension(".sb2");
            project.setVersion(Version.SCRATCH2);
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
     * SCRATCH 3.0
     * A method to make scripts readable in the console
     *
     * @param scripts list opf scripts
     * @return a String for pretty console output
     */
    public static String prettyPrintScript3(List<Script> scripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Scripts:");
        int i = 0;
        for (Script sc : scripts) {
            sb.append("\n");
            sb.append("*****Script_").append(i).append("*****\nPosition: x ").append(sc.getPosition()[0]).append(", y ").append(sc.getPosition()[1]);
            sb.append("\nBlocks:\n");
            for (ScBlock b : sc.getBlocks()) {
                prettyBlocks3(sb, b, 0);
            }
            sb.append("*****Script_").append(i).append("*****");
            i++;
        }
        return sb.toString();
    }

    /**
     * SCRATCH 3.0
     * A recursive method appending a pretty String version of the block and its nested blocks to a given StringBuilder
     *
     * @param sb the StringBuilder to append the given block in pretty form
     * @param b  the block to append
     * @param x  the recursion level to simulate the tree structure with readable indents in the console output
     */
    private static void prettyBlocks3(StringBuilder sb, ScBlock b, int x) {
        for (int y = 0; y < x; y++) {
            sb.append("~~~~");
        }
        sb.append("    [ScBlock{" + "content='").append(b.getContent()).append('\'').append("}]\n");
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            x = x + 1;
        }
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            for (ScBlock nb : b.getNestedBlocks()) {
                prettyBlocks3(sb, nb, x);
            }
        }
        if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
            for (int y = 0; y < x; y++) {
                sb.append("~~~~");
            }
            sb.append("    ELSE\n");
            for (ScBlock nb : b.getElseBlocks()) {
                prettyBlocks3(sb, nb, x);
            }
        }
    }

    /**
     * SCRATCH 2.0
     * A method to make scripts readable in the console
     *
     * @param scripts list opf scripts
     * @return a String for pretty console output
     */
    public static String prettyPrintScript2(List<Script> scripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Scripts:");
        int i = 0;
        for (Script sc : scripts) {
            sb.append("\n");
            sb.append("*****Script_").append(i).append("*****\nPosition: x ").append(sc.getPosition()[0]).append(", y ").append(sc.getPosition()[1]);
            sb.append("\nBlocks:\n");
            for (ScBlock b : sc.getBlocks()) {
                prettyBlocks2(sb, b, 0);
            }
            sb.append("*****Script_").append(i).append("*****");
            i++;
        }
        return sb.toString();
    }

    /**
     * SCRATCH 2.0
     * A recursive method appending a pretty String version of the block and its nested blocks to a given StringBuilder
     *
     * @param sb the StringBuilder to append the given block in pretty form
     * @param b  the block to append
     * @param x  the recursion level to simulate the tree structure with readable indents in the console output
     */
    private static void prettyBlocks2(StringBuilder sb, ScBlock b, int x) {
        for (int y = 0; y < x; y++) {
            sb.append("~~~~");
        }
        sb.append("    [ScBlock{" + "content='").append(b.getContent()).append('\'').append("}]\n");
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            x = x + 1;
        }
        if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
            for (ScBlock nb : b.getNestedBlocks()) {
                prettyBlocks2(sb, nb, x);
            }
        }
        if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
            for (int y = 0; y < x; y++) {
                sb.append("~~~~");
            }
            sb.append("    ELSE\n");
            for (ScBlock nb : b.getElseBlocks()) {
                prettyBlocks2(sb, nb, x);
            }
        }
    }

    public static JsonNode getBlocksNodeFromJSON(String path) {
        JsonNode script = null;
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        ObjectMapper mapper = new ObjectMapper();
        try {

            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JsonNode rootNode = mapper.readTree(sb.toString());

            Iterator<JsonNode> elements = rootNode.get("targets").elements();
            while (elements.hasNext()) {
                JsonNode c = elements.next();
                if (c.has("isStage") && !c.get("isStage").asBoolean() && c.has("blocks")) {
                    script = c.get("blocks");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return script;
    }
}
