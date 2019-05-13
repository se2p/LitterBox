package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.ScBlock;
import scratch.data.Script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class ScriptDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Script> with Script objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Script> with Script objects
     */
    static List<Script> deserialize(JsonNode rootNode) {
        JsonNode globalScripts = rootNode.path("scripts");
        Iterator<JsonNode> elements = globalScripts.elements();
        List<Script> scripts = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode c = elements.next();
            List<String> scrpt = new ArrayList<>();
            List<ScBlock> blocks = new ArrayList<>();
            if (c.isArray()) {
                for (final JsonNode objNode : c) {
                    if (objNode.isArray()) {
                        blocks = parseBlocks(objNode);
                    } else {
                        scrpt.add(objNode.asText());
                    }
                }
            }
            Script script = new Script();
            double[] pos = {Double.valueOf(scrpt.get(0)), Double.valueOf(scrpt.get(1))};
            script.setPosition(pos);
            script.setBlocks(blocks);
            scripts.add(script);
        }
        return scripts;
    }

    /**
     * Parsing a script with its blocks and nested blocks
     *
     * @param objectNode the JsonNode to deserialize
     * @return a List<ScBlock> with ScBlock objects
     */
    private static List<ScBlock> parseBlocks(JsonNode objectNode) {
        List<ScBlock> blocks = new ArrayList<>();
        if (objectNode.isArray()) {
            for (final JsonNode node : objectNode) {
                ScBlock bl = parseBlock(node);
                blocks.add(bl);
            }
        }
        return blocks;
    }

    /**
     * Parsing a block with its nested blocks
     * Pretty dirty because of the way Scratch saves their block structure in the Version 2.0 (https://en.scratch-wiki.info/wiki/Scratch_File_Format#Block_tuples)
     * TODO: Create new method for JSON parsing when using Scratch Version 3.0
     * In Version 3.0, you can directly access nested blocks and condition values. You do not need to check for JsonNode array content anymore.
     *
     * @param objectNode the JsonNode to deserialize
     * @return a ScBlock object with its nested blocks
     */

    private static ScBlock parseBlock(JsonNode objectNode) {
        ScBlock bl = new ScBlock();
        if (objectNode.size() == 1) {
            bl.setContent(objectNode.get(0).asText());
            return bl;
        } else if (objectNode.size() == 4) {
            //System.out.println(objectNode);
            bl.setContent(objectNode.get(0).asText());
            //System.out.println(objectNode);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < objectNode.size(); i++) {
                //System.out.println(objectNode.get(i));
                if (objectNode.get(i).isArray()) {
                    if (objectNode.get(i).get(0) != null && objectNode.get(i).get(0).isArray()) {
                        if (i == 2) {
                            bl.setNestedBlocks(parseBlocks(objectNode.get(i)));
                        } else {
                            bl.setElseBlocks(parseBlocks(objectNode.get(i)));
                        }
                    } else {
                        sb.append(objectNode.get(i));
                    }
                } else {
                    sb.append(objectNode.get(i));
                }
            }
            bl.setContent(sb.toString());
        } else if (objectNode.size() > 1) {
            bl.setContent(objectNode.get(0).asText());
            //System.out.println(objectNode);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < objectNode.size(); i++) {
                //System.out.println(objectNode.get(i));
                if (objectNode.get(i).isArray()) {
                    if (objectNode.get(i).get(0) != null && objectNode.get(i).get(0).isArray()) {
                        bl.setNestedBlocks(parseBlocks(objectNode.get(i)));
                    } else {
                        sb.append(objectNode.get(i));
                    }
                } else {
                    sb.append(objectNode.get(i));
                }
            }
            bl.setContent(sb.toString());
        }
        return bl;
    }

}
