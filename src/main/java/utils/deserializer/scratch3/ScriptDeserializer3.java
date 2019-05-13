package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.ScBlock;
import scratch.data.Script;
import utils.Identifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ScriptDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Script> with Script objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Script> with Script objects
     */
    static List<Script> deserialize(JsonNode rootNode) {
        JsonNode globalScripts = rootNode.path("blocks");
        Iterator<String> elements = globalScripts.fieldNames();
        List<Script> scripts = new ArrayList<>();
        while (elements.hasNext()) {
            String id = elements.next();
            JsonNode n = globalScripts.get(id);
            if (n.get("topLevel").asBoolean()) {
                List<ScBlock> blocks = parseBlocks(globalScripts, id);
                double[] pos = {n.get("x").asDouble(), n.get("y").asDouble()};
                Script script = new Script();
                script.setPosition(pos);
                script.setBlocks(blocks);
                scripts.add(script);
            }
        }
        return scripts;
    }

    /**
     * Parsing a script with its blocks and nested blocks
     *
     * @param globalBlocks the JsonNode to deserialize
     * @param id           internal block id
     * @return a List<ScBlock> with ScBlock objects
     */
    private static List<ScBlock> parseBlocks(JsonNode globalBlocks, String id) {
        JsonNode startingBlock = globalBlocks.get(id);
        List<ScBlock> blocks = new ArrayList<>();
        ScBlock block = new ScBlock();
        block.setId(id);
        block.setContent(startingBlock.get("opcode").asText());
        handleFields(globalBlocks, id, block);
        handleInputs(globalBlocks, id, block);
        handleCustom(globalBlocks, id, block);
        blocks.add(block);
        if (!startingBlock.get("next").asText().equals("null")) {
            parseBlock(blocks, globalBlocks, startingBlock.get("next").asText());
        }
        return blocks;
    }

    /**
     * Parsing a block with its nested blocks
     *
     * @param blocks       the script to add the block
     * @param globalBlocks the JsonNode to deserialize
     * @param id           internal block id
     */

    private static void parseBlock(List<ScBlock> blocks, JsonNode globalBlocks, String id) {
        JsonNode nextBlock = globalBlocks.get(id);
        ScBlock block = new ScBlock();
        block.setId(id);
        if (nextBlock == null) {
            return;
        }
        block.setContent(nextBlock.get("opcode").asText());
        handleInputs(globalBlocks, id, block);
        handleFields(globalBlocks, id, block);
        handleCustom(globalBlocks, id, block);
        blocks.add(block);
        if (!nextBlock.get("next").asText().equals("null")) {
            parseBlock(blocks, globalBlocks, nextBlock.get("next").asText());
        }
    }

    private static void handleInputs(JsonNode globalBlocks, String id, ScBlock block) {
        JsonNode inputs = globalBlocks.get(id).get("inputs");
        Iterator<String> elements = inputs.fieldNames();
        while (elements.hasNext()) {
            String inputId = elements.next();
            List<String> ins = new ArrayList<>();
            if (inputs.get(inputId).isArray()) {
                for (final JsonNode objNode : inputs.get(inputId)) {
                    if (objNode.isArray()) {
                        for (final JsonNode n : objNode) {
                            ins.add(n.asText());
                        }
                    } else {
                        ins.add(objNode.asText());
                    }
                }
            }
            block.getInputs().put(inputId, ins);
            switch (inputId) {
                case "SUBSTACK": {
                    String subId = ins.get(1);
                    List<ScBlock> subblocks = new ArrayList<>();
                    parseBlock(subblocks, globalBlocks, subId);
                    block.setNestedBlocks(subblocks);
                    break;
                }
                case "SUBSTACK2": {
                    String subId = ins.get(1);
                    List<ScBlock> subblocks = new ArrayList<>();
                    parseBlock(subblocks, globalBlocks, subId);
                    block.setElseBlocks(subblocks);
                    break;
                }
                case "CLONE_OPTION": {
                    String subId = ins.get(1);
                    JsonNode clone = globalBlocks.get(subId);
                    JsonNode n = clone.get("fields").get(Identifier.CLONE_OPTION.getValue()).get(0);
                    block.setCreatedClone(n.asText());
                    break;
                }
                case "CONDITION": {
                    String keyOption = Identifier.KEY_OPTION.getValue();
                    String touchingObject = Identifier.TOUCHING_OBJECT.getValue();
                    String operator = "operator_gt";
                    String subId = ins.get(1);
                    String condition = globalBlocks.get(subId).get("opcode").asText();
                    if (globalBlocks.get(subId).get("inputs").get(keyOption) != null) {
                        String subsubId = globalBlocks.get(subId).get("inputs").get(keyOption).get(1).asText();
                        JsonNode subsub = globalBlocks.get(subsubId);
                        condition = condition + "," + subsub.get("opcode").asText();
                        if (subsub.get("fields").get(keyOption) != null) {
                            condition = condition + "," + subsub.get("fields").get(keyOption).get(0).asText();
                        }
                    } else if (globalBlocks.get(subId).get("inputs").get(touchingObject) != null) {
                        String subsubId = globalBlocks.get(subId).get("inputs").get(touchingObject).get(1).asText();
                        JsonNode subsub = globalBlocks.get(subsubId);
                        condition = condition + "," + subsub.get("opcode").asText();
                        if (subsub.get("fields").get(touchingObject) != null) {
                            condition = condition + "," + subsub.get("fields").get(touchingObject).get(0).asText();
                        }
                    } else if (globalBlocks.get(subId).get("opcode").asText().equals(operator)) {
                        condition = getCondition(globalBlocks, subId, condition, "OPERAND1");
                        if (condition == null) continue;
                    } else {
                        condition = condition + "," + globalBlocks.get(subId).get("inputs").toString();
                    }
                    block.setCondition(condition);
                    break;
                }
            }
        }
    }

    private static void handleCustom(JsonNode globalBlocks, String id, ScBlock block) {
        if (globalBlocks.get(id).get("opcode").asText().equals("procedures_call")) {
            block.setProcode(globalBlocks.get(id).get("mutation").get("proccode").asText());
        } else if (globalBlocks.get(id).get("opcode").asText().equals("procedures_definition")) {
            String sub = globalBlocks.get(id).get("inputs").get("custom_block").get(1).asText();
            block.setProcode(globalBlocks.get(sub).get("mutation").get("proccode").asText());
        }
    }

    private static String getCondition(JsonNode globalBlocks, String subId, String condition, String operand) {
        String subsubId = globalBlocks.get(subId).get("inputs").get(operand).get(1).asText();
        JsonNode subsub = globalBlocks.get(subsubId);
        if (subsub == null) {
            condition = condition + "," + globalBlocks.get(subId).get("inputs").get("OPERAND1");
            condition = getCondition2(globalBlocks, subId, condition, "OPERAND2");
            return condition;
        }
        condition = condition + "," + subsub.get("opcode").asText();
        if (subsub.get("opcode").asText().equals("sensing_of")) {
            condition = condition + "," + subsub.get("fields").get("PROPERTY").get(0);
            JsonNode obj = globalBlocks.get(subsub.get("inputs").get("OBJECT").get(1).asText());
            condition = condition + "," + obj.get("opcode").asText();
            condition = condition + "," + obj.get("fields").get("OBJECT").get(0);
        }
        return condition;
    }

    private static String getCondition2(JsonNode globalBlocks, String subId, String condition, String operand) {
        String subsubId = globalBlocks.get(subId).get("inputs").get(operand).get(1).asText();
        JsonNode subsub = globalBlocks.get(subsubId);
        if (subsub == null) {
            condition = condition + "," + globalBlocks.get(subId).get("inputs").get("OPERAND2");
            return condition;
        }
        condition = condition + "," + subsub.get("opcode").asText();
        if (subsub.get("opcode").asText().equals("sensing_of")) {
            condition = condition + "," + subsub.get("fields").get("PROPERTY").get(0);
            JsonNode obj = globalBlocks.get(subsub.get("inputs").get("OBJECT").get(1).asText());
            condition = condition + "," + obj.get("opcode").asText();
            condition = condition + "," + obj.get("fields").get("OBJECT").get(0);
        }
        return condition;
    }

    private static void handleFields(JsonNode globalBlocks, String id, ScBlock block) {
        JsonNode fields = globalBlocks.get(id).get("fields");
        //System.out.println(fields);
        Iterator<String> elements = fields.fieldNames();
        while (elements.hasNext()) {
            String fieldId = elements.next();
            List<String> fld = new ArrayList<>();
            if (fields.get(fieldId).isArray()) {
                for (final JsonNode objNode : fields.get(fieldId)) {
                    if (objNode.isArray()) {
                        for (final JsonNode n : objNode) {
                            fld.add(n.asText());
                        }
                    } else {
                        fld.add(objNode.asText());
                    }
                }
            }
            block.getFields().put(fieldId, fld);
        }
    }

}
