package scratch.structure.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class Utils {
    public static JsonNode parseScript(String path) {
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
                sb.append(line);        //.append('\n');
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
