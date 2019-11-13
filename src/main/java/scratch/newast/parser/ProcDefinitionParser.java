package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.NEXT_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.PARENT_KEY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.StmtList;
import scratch.newast.model.procedure.Parameter;
import scratch.newast.model.procedure.ParameterList;
import scratch.newast.model.procedure.ParameterListPlain;
import scratch.newast.model.procedure.ProcedureDefinition;
import scratch.newast.model.procedure.ProcedureDefinitionList;
import scratch.newast.model.type.BooleanType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.ProcedureOpcode;

public class ProcDefinitionParser {
    private final static String CUSTOM_BLOCK_KEY = "custom_block";
    private final static String MUTATION_KEY = "mutation";
    private final static String PROCCODE_KEY = "proccode";
    private final static String ARGUMENTNAMES_KEY = "argumentnames";
    private final static String VALUE_KEY = "VALUE";
    private final static int PROTOTYPE_REFERENCE_POS = 1;
    private final static int PARAMETER_REFERENCE_POS = 1;

    public static ProcedureDefinitionList parse(JsonNode blocks) throws ParsingException {
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
            return new ProcedureDefinitionList(new ArrayList<>());
        }
        List<ProcedureDefinition> procdecls = new ArrayList<>();
        for (JsonNode jsonNode : defBlock) {

            procdecls.add(parseProcDecl(jsonNode, blocks));
        }
        return new ProcedureDefinitionList(procdecls);
    }

    private static ProcedureDefinition parseProcDecl(JsonNode def, JsonNode blocks) throws ParsingException {
        JsonNode input = def.get(Constants.INPUTS_KEY).get(CUSTOM_BLOCK_KEY);
        Preconditions.checkArgument(input.isArray());
        ArrayNode inputArray = (ArrayNode) input;
        String protoReference = inputArray.get(PROTOTYPE_REFERENCE_POS).textValue();
        JsonNode proto = blocks.get(protoReference);
        Iterator<Map.Entry<String, JsonNode>> iter = proto.get(INPUTS_KEY).fields();
        ArrayList<Parameter> inputs = new ArrayList<>();
        while (iter.hasNext()) {
            String inputRef = iter.next().getKey();
            JsonNode currentInput = iter.next().getValue();
            Preconditions.checkArgument(currentInput.isArray());
            ArrayNode current = (ArrayNode) currentInput;
            inputs.add(parseParameter(blocks, inputRef, current.get(PARAMETER_REFERENCE_POS).textValue()));
        }
        ParameterListPlain parameterListPlain = new ParameterListPlain(inputs);
        ParameterList parameterList = new ParameterList(parameterListPlain);
        String methodName = proto.get(MUTATION_KEY).get(PROCCODE_KEY).textValue();
        Identifier ident = new Identifier(proto.get(PARENT_KEY).textValue());
        JsonNode argumentNamesNode = proto.get(MUTATION_KEY).get(ARGUMENTNAMES_KEY);
        ObjectMapper mapper = new ObjectMapper();
       // mapper.readTree(argumentNamesNode.textValue());
        //TODO get array out of string
        //ProgramParser.procDefMap.addProcedure(ident,methodName,);
        StmtList stmtList = ScriptParser.parseStmtList(def.get(NEXT_KEY).textValue(), blocks);
        return new ProcedureDefinition(ident, parameterList, stmtList);
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
