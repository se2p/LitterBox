package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;

import java.util.Set;

public abstract class Transformer {

    protected String opcode;
    protected boolean topLevel;
    protected boolean shadow; //Returns the opcode(s)/id(s) this transformer works for.

    abstract Set<String> getIdentifiers();

    abstract BasicBlock transform(JsonNode node, Ast ast);

    protected void extractStandardValues(JsonNode node) {
        opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
        topLevel = node.get("topLevel").asBoolean();
        shadow = node.get("shadow").asBoolean();
    }
}
