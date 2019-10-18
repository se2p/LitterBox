package scratch.structure.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import scratch.structure.ast.inputs.Literal;
import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.stack.SingleIntInputBlock;
import scratch.structure.ast.transformers.Dispatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static scratch.structure.ast.transformers.Transformer.*;

public class Ast {

    ScratchBlock root = null;

    public Map<String, ScratchBlock> getNodesIdMap() {
        return nodesIdMap;
    }

    Map<String, ScratchBlock> nodesIdMap = new HashMap();

    public Ast parseScript(JsonNode blocksNode) {
        Ast tree = new Ast();
        buildBasicBlocks(blocksNode);
        connectAndFillSlots(blocksNode);

        return tree;
    }

    private void buildBasicBlocks(JsonNode blocksNode) {
        Iterator<String> it = blocksNode.fieldNames();

        while (it.hasNext()) {
            String nextId = it.next();
            JsonNode node = blocksNode.get(nextId);
            String opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
            ScratchBlock block = Dispatcher.dispatcher.transform(opcode, node, this);

            nodesIdMap.put(nextId, block);

            String parent = node.get("parent").toString();
            parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string
            if (parent.equals("null")) {
                root = block;
            }
        }
    }

    /**
     * Sets all parent and next blocks and fills the slots of blocks with inputs.
     * Currently filling the slots is only implemented for SingleIntInputBlocks.
     *
     * @param blocksNode The JsonNode containing all block information.
     */
    private void connectAndFillSlots(JsonNode blocksNode) {
        //Connect the blocks
        for (Map.Entry<String, ScratchBlock> blockIdAndBlock : nodesIdMap.entrySet()) {
            ScratchBlock block = blockIdAndBlock.getValue();
            // FIXME remove null check as soon as all blocks of the slottest fixture are implemented
            if (block != null && block.getParent() == null) {
                JsonNode parentNode = blocksNode.get(blockIdAndBlock.getKey()).get("parent");
                if (parentNode != null && !(parentNode instanceof NullNode)) {
                    String parent = parentNode.toString();
                    parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string
                    blockIdAndBlock.getValue().setParent((Extendable) nodesIdMap.get(parent));
                }
            }

            JsonNode nextNode = blocksNode.get(blockIdAndBlock.getKey()).get("next");
            // FIXME remove null check as soon as all blocks of the slottest fixture are implemented
            if (block != null && block.getNext() == null && nextNode != null && !(nextNode instanceof NullNode)) {
                String next = nextNode.toString();
                next = next.replaceAll("^\"|\"$", ""); //remove quotes around string
                blockIdAndBlock.getValue().setNext((Stackable) nodesIdMap.get(next));
            }
            if (block instanceof SingleIntInputBlock) {
                parseSingleIntInputs(blocksNode.get(blockIdAndBlock.getKey()).get("inputs"), (SingleIntInputBlock) block);
            }
        }
    }

    /**
     * Adds and fills in the slot of a SingleIntInputBlocks.
     *
     * @param inputs          The JsonNode containing the inputs of the block.
     * @param block           The SingleIntInputBlock of which the slot is filled in.
     */
    private void parseSingleIntInputs(JsonNode inputs, SingleIntInputBlock block) {
        Map.Entry slotEntry = inputs.fields().next();
        String slotName = (String) slotEntry.getKey();
        ArrayNode inputArray = (ArrayNode) slotEntry.getValue();
        int shadowIndicator = inputArray.get(POS_INPUT_SHADOW).asInt();

        if (shadowIndicator == INPUT_DIFF_BLOCK_SHADOW) {
            String blockID;
            if (inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID) instanceof ArrayNode) {
                // It is either a variable or a list primary input
                blockID = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).toString().replaceAll("^\"|\"$", "");
            } else {
                // It is a block primary input
                blockID = inputArray.get(POS_DATA_ARRAY).asText(); //TODO add an own constant instead of using one with the same value
            }
            Input primary = (Input) nodesIdMap.get(blockID);
            int shadowType = inputArray.get(POS_SHADOW_ARRAY).get(POS_INPUT_TYPE).asInt();
            String shadowValue = inputArray.get(POS_SHADOW_ARRAY).get(POS_INPUT_VALUE).asText();
            Literal shadow = new Literal(shadowType, shadowValue);
            Slot slot = new Slot(slotName, shadowIndicator, primary, shadow);
            block.setSlot(slot);

        } else if (shadowIndicator == INPUT_BLOCK_NO_SHADOW || shadowIndicator == INPUT_SAME_BLOCK_SHADOW) {
            //TODO find out what the difference between the meaning of these constants really is
            //There is no shadow, so create a new Literal and add it as primary.
            int type = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_TYPE).asInt();
            String value = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asText();
            Literal primary = new Literal(type, value);
            Slot slot = new Slot(slotName, shadowIndicator, primary);
            block.setSlot(slot);
        }
    }

    public ScratchBlock getRoot() {
        return root;
    }
}
