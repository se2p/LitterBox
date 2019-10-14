package scratch.structure.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import scratch.structure.ast.transformers.Dispatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Ast {

    BasicBlock root = null;

    public Map<String, BasicBlock> getNodesIdMap() {
        return nodesIdMap;
    }

    Map<String, BasicBlock> nodesIdMap = new HashMap();

    public Ast parseScript(JsonNode blocksNode) {
        Ast tree = new Ast();
        Iterator<String> it = blocksNode.fieldNames();

        //Build the basic blocks
        while (it.hasNext()) {
            String nextId = it.next();
            JsonNode node = blocksNode.get(nextId);
            String opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
            BasicBlock block = Dispatcher.dispatcher.transform(opcode, node, this);

            nodesIdMap.put(nextId, block);

            String parent = node.get("parent").toString();
            parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string
            if (parent.equals("null")) {
                root = block;
            }
        }

        //Connect the blocks
        for (Map.Entry<String, BasicBlock> blockIdAndBlock : nodesIdMap.entrySet()) {
            BasicBlock block = blockIdAndBlock.getValue();
            if (block.getParent() == null) {
                JsonNode parentNode = blocksNode.get(blockIdAndBlock.getKey()).get("parent");
                if (parentNode != null && !(parentNode instanceof NullNode)) {
                    String parent = parentNode.toString();
                    parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string
                    blockIdAndBlock.getValue().setParent((Extendable) nodesIdMap.get(parent));
                }
            }

            JsonNode nextNode = blocksNode.get(blockIdAndBlock.getKey()).get("next");
            if (block.getNext() == null && nextNode != null && !(nextNode instanceof NullNode)) {
                String next = nextNode.toString();
                next = next.replaceAll("^\"|\"$", ""); //remove quotes around string
                blockIdAndBlock.getValue().setNext((Stackable) nodesIdMap.get(next));
            }
        }
        return tree;
    }

    public BasicBlock getRoot() {
        return root;
    }
}
