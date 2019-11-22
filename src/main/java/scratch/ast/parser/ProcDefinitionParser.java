/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package scratch.ast.parser;

import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.INPUTS_KEY;
import static scratch.ast.Constants.NEXT_KEY;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.Constants.PARENT_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.StmtList;
import scratch.ast.model.procedure.Parameter;
import scratch.ast.model.procedure.ParameterList;
import scratch.ast.model.procedure.ParameterListPlain;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.procedure.ProcedureDefinitionList;
import scratch.ast.model.type.BooleanType;
import scratch.ast.model.type.StringType;
import scratch.ast.model.type.Type;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.ProcedureOpcode;
import scratch.utils.Preconditions;

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
        ArrayList<Parameter> inputs = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> iter = proto.get(INPUTS_KEY).fields();
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
        Identifier ident = new StrId(proto.get(PARENT_KEY).textValue());
        JsonNode argumentNamesNode = proto.get(MUTATION_KEY).get(ARGUMENTNAMES_KEY);
        ObjectMapper mapper = new ObjectMapper();

        final JsonNode argumentsNode;
        try {
            argumentsNode = mapper.readTree(argumentNamesNode.textValue());
        } catch (IOException e) {
            throw new ParsingException("Could not read argument names of a procedure");
        }

        Preconditions.checkArgument(argumentsNode.isArray());
        ArrayNode argumentsArray = (ArrayNode) argumentsNode;

        String[] arguments = new String[argumentsArray.size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = argumentsArray.get(i).textValue();
        }

        ProgramParser.procDefMap.addProcedure(ident, methodName, arguments, getTypes(inputs));
        StmtList stmtList = ScriptParser.parseStmtList(def.get(NEXT_KEY).textValue(), blocks);
        return new ProcedureDefinition(ident, parameterList, stmtList);
    }

    private static Type[] getTypes(ArrayList<Parameter> inputs) {
        Type[] types = new Type[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            types[i] = inputs.get(i).getType();
        }

        return types;
    }

    private static Parameter parseParameter(JsonNode blocks, String reference, String textValue) {
        JsonNode param = blocks.get(textValue);
        final String opcodeString = param.get(OPCODE_KEY).textValue();

        final Parameter result;
        if (opcodeString.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            result = new Parameter(new StrId(reference), new BooleanType());
        } else {
            result = new Parameter(new StrId(reference), new StringType());
        }

/*        JsonNode values = param.get(FIELDS_KEY).get(VALUE_KEY);
        Preconditions.checkArgument(values.isArray());
        ArrayNode arrayNode = (ArrayNode) values;
        String name = arrayNode.get(0).textValue();
        result.setValue(name);
        */
        // FIXME!
        throw new IllegalArgumentException("The above (uncommented) code does not make sense! Fix this.");

    }
}
