package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.model.procedure.Parameter;
import scratch.newast.model.procedure.ProcedureDeclaration;
import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.ProcedureOpcode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

public class ProcDeclParser {
    private final static String CUSTOM_BLOCK = "custom_block";
    private final static String MUTATION = "mutation";
    private final static String PROCCODE = "proccode";

    public static ProcedureDeclarationList parse(JsonNode blocks) {
        Preconditions.checkNotNull(blocks);
        Iterator<JsonNode> iter = blocks.elements();
        List<JsonNode> defBlock = new ArrayList<>();
        List<JsonNode> protoBlock =new ArrayList<>();
        while (iter.hasNext()){
            JsonNode current = iter.next();
            String opcodeString = current.get(OPCODE_KEY).textValue();
            if(opcodeString.equals(ProcedureOpcode.procedures_definition.name())){
                defBlock.add(current);
            }else if(opcodeString.equals(ProcedureOpcode.procedures_prototype.name())){
                protoBlock.add(current);
            }
        }
        //each definition needs the prototype block because it holds the name of the procedure
        Preconditions.checkArgument(protoBlock.size()==defBlock.size());
        if(defBlock.size()==0){
            //TODO what to return if no custom blocks are found
            return null;
        }
        for (JsonNode jsonNode : defBlock) {

            parseProcDecl(jsonNode, blocks);
        }
        throw new RuntimeException("Not implemented");
    }

    private static ProcedureDeclaration parseProcDecl(JsonNode def, JsonNode blocks){
        JsonNode input = def.get(Constants.INPUTS_KEY).get(CUSTOM_BLOCK);
        Preconditions.checkArgument(input.isArray());
        ArrayNode inputArray = (ArrayNode) input;
        String protoReference=inputArray.get(2).textValue();
        JsonNode proto = blocks.get(protoReference);
        Iterator<Map.Entry<String, JsonNode>> iter = proto.get(INPUTS_KEY).fields();
        ArrayList<Parameter> inputs = new ArrayList<>();
        while (iter.hasNext()){
            JsonNode currentInput = iter.next().getValue();
            Preconditions.checkArgument(currentInput.isArray());
            ArrayNode current = (ArrayNode) currentInput;
            inputs.add(parseParameter(blocks, current.get(2).textValue()));
        }
        Identifier ident = new Identifier(proto.get(MUTATION).get(PROCCODE).textValue());
        throw new RuntimeException("Not Implemented");
    }

    private static Parameter parseParameter(JsonNode blocks, String textValue) {
        JsonNode param = blocks.get(textValue);
        //TODO what is the identifier of a parameter? opcode already defined?
        throw new RuntimeException("Not Implemented");
    }
}
