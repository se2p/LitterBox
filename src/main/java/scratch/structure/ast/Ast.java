package scratch.structure.ast;

import com.fasterxml.jackson.databind.JsonNode;
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
        for (Map.Entry<String, BasicBlock> block : nodesIdMap.entrySet()) {
            if (block.getValue().getParent() == null) {
                JsonNode parentNode = blocksNode.at("/" + block.getKey()).get("parent");
                if (parentNode != null) {
                    String parent = parentNode.toString();
                    parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string
                    block.getValue().setParent(nodesIdMap.get(parent));

                }
            }

            if (block.getValue().getNext() == null && blocksNode.at("/" + block.getKey()).get("next") != null) {
                String next = blocksNode.at("/" + block.getKey()).get("next").toString();
                next = next.replaceAll("^\"|\"$", ""); //remove quotes around string
                block.getValue().setNext((Stackable) nodesIdMap.get(next));
            }
        }
        return tree;
    }

    public BasicBlock getRoot() {
        return root;
    }
}
