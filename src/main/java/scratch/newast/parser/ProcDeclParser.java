package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.StmtList;
import scratch.newast.model.procedure.*;
import scratch.newast.model.type.BooleanType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.ProcedureOpcode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static scratch.newast.Constants.*;

public class ProcDeclParser {
    private final static String CUSTOM_BLOCK = "custom_block";
    private final static String MUTATION = "mutation";
    private final static String PROCCODE = "proccode";
    private final static String VALUE_KEY = "VALUE";

    public static ProcedureDeclarationList parse(JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blocks);
        Iterator<JsonNode> iter = blocks.elements();
        List<JsonNode> defBlock = new ArrayList<>();
        List<JsonNode> protoBlock = new ArrayList<>();
        while (iter.hasNext()) {
            JsonNode current = iter.next();
            String opcodeString = current.get(OPCODE_KEY).textValue();
            if (opcodeString.equals(ProcedureOpcode.procedures_definition.name())) {
                defBlock.add(current);
            } else if (opcodeString.equals(ProcedureOpcode.procedures_prototype.name())) {
                protoBlock.add(current);
            }
        }
        //each definition needs the prototype block because it holds the name of the procedure
        Preconditions.checkArgument(protoBlock.size() == defBlock.size());
        if (defBlock.size() == 0) {
            return new ProcedureDeclarationList(new ArrayList<>());
        }
        List<ProcedureDeclaration> procdecls = new ArrayList<>();
        for (JsonNode jsonNode : defBlock) {

            procdecls.add(parseProcDecl(jsonNode, blocks));
        }
        return new ProcedureDeclarationList(procdecls);
    }

    private static ProcedureDeclaration parseProcDecl(JsonNode def, JsonNode blocks) throws ParsingException {
        JsonNode input = def.get(Constants.INPUTS_KEY).get(CUSTOM_BLOCK);
        Preconditions.checkArgument(input.isArray());
        ArrayNode inputArray = (ArrayNode) input;
        String protoReference = inputArray.get(2).textValue();
        JsonNode proto = blocks.get(protoReference);
        Iterator<Map.Entry<String, JsonNode>> iter = proto.get(INPUTS_KEY).fields();
        ArrayList<Parameter> inputs = new ArrayList<>();
        while (iter.hasNext()) {
            String inputRef = iter.next().getKey();
            JsonNode currentInput = iter.next().getValue();
            Preconditions.checkArgument(currentInput.isArray());
            ArrayNode current = (ArrayNode) currentInput;
            inputs.add(parseParameter(blocks, inputRef, current.get(2).textValue()));
        }
        ParameterListPlain parameterListPlain = new ParameterListPlain(inputs);
        ParameterList parameterList = new ParameterList(parameterListPlain);
        String methodName = proto.get(MUTATION).get(PROCCODE).textValue();
        //TODO add name in ProcNameMapping
        Identifier ident = new Identifier(proto.get(PARENT_KEY).textValue());
        StmtList stmtList = ScriptParser.parseStmtList(def.get(NEXT_KEY).textValue(), blocks);
        return new ProcedureDeclaration(ident, parameterList, stmtList);
    }

    private static Parameter parseParameter(JsonNode blocks, String reference, String textValue) {
        JsonNode param = blocks.get(textValue);
        String opcodeString = param.get(OPCODE_KEY).textValue();
        Parameter returnParam;
        if (opcodeString.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            returnParam = new Parameter(new Identifier(reference), new BooleanType());
        } else {
            returnParam = new Parameter(new Identifier(reference), new StringType());
        }
        JsonNode values = param.get(FIELDS_KEY).get(VALUE_KEY);
        Preconditions.checkArgument(values.isArray());
        ArrayNode arrayNode = (ArrayNode) values;
        String name = arrayNode.get(0).textValue();
        returnParam.setValue(name);
        return returnParam;
    }
}
