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

import static scratch.ast.Constants.*;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.StmtList;
import scratch.ast.model.procedure.*;
import scratch.ast.model.type.BooleanType;
import scratch.ast.model.type.StringType;
import scratch.ast.model.type.Type;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.ProcedureOpcode;
import utils.Preconditions;

public class ProcDefinitionParser {

    private final static String CUSTOM_BLOCK_KEY = "custom_block";
    private final static String ARGUMENTNAMES_KEY = "argumentnames";
    private final static int PROTOTYPE_REFERENCE_POS = 1;
    private final static int PARAMETER_REFERENCE_POS = 1;

    public static ProcedureDefinitionList parse(JsonNode blocks, String actorName) throws ParsingException {
        Preconditions.checkNotNull(blocks);
        Iterator<JsonNode> iter = blocks.elements();
        List<JsonNode> defBlock = new ArrayList<>();
        List<JsonNode> protoBlock = new ArrayList<>();

        while (iter.hasNext()) {
            JsonNode current = iter.next();
            String opcodeString = current.get(OPCODE_KEY).asText();
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

            procdecls.add(parseProcDecl(jsonNode, blocks, actorName));
        }

        return new ProcedureDefinitionList(procdecls);
    }

    private static ProcedureDefinition parseProcDecl(JsonNode def, JsonNode blocks, String actorName) throws ParsingException {
        JsonNode input = def.get(Constants.INPUTS_KEY).get(CUSTOM_BLOCK_KEY);
        Preconditions.checkArgument(input.isArray());
        ArrayNode inputArray = (ArrayNode) input;
        String protoReference = inputArray.get(PROTOTYPE_REFERENCE_POS).asText();
        JsonNode proto = blocks.get(protoReference);
        ArrayList<Parameter> inputs = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> iter = proto.get(INPUTS_KEY).fields();
        List<Type> paraTypes = new ArrayList<>();
        while (iter.hasNext()) {
            final Entry<String, JsonNode> current = iter.next();
            JsonNode currentInput = current.getValue();
            Preconditions.checkArgument(currentInput.isArray());
            ArrayNode currentAsArray = (ArrayNode) currentInput;

            //TODO is this right?
            if(!currentAsArray.get(PARAMETER_REFERENCE_POS).asText().equals("null")) {
                paraTypes = addType(blocks, paraTypes, currentAsArray.get(PARAMETER_REFERENCE_POS).asText());
            }
        }


        String methodName = proto.get(MUTATION_KEY).get(PROCCODE_KEY).asText();
        Identifier ident = new StrId(proto.get(PARENT_KEY).asText());
        JsonNode argumentNamesNode = proto.get(MUTATION_KEY).get(ARGUMENTNAMES_KEY);
        ObjectMapper mapper = new ObjectMapper();

        final JsonNode argumentsNode;
        try {
            argumentsNode = mapper.readTree(argumentNamesNode.asText());
        } catch (IOException e) {
            throw new ParsingException("Could not read argument names of a procedure");
        }

        Preconditions.checkArgument(argumentsNode.isArray());
        ArrayNode argumentsArray = (ArrayNode) argumentsNode;

        String[] arguments = new String[argumentsArray.size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = PARAMETER_ABBREVIATION + argumentsArray.get(i).asText();
        }
        Preconditions.checkArgument(arguments.length == paraTypes.size());
        Type[] typeArray = new Type[paraTypes.size()];
        ProgramParser.procDefMap.addProcedure(ident, actorName, methodName, arguments, paraTypes.toArray(typeArray));

        for (int i = 0; i < paraTypes.size(); i++) {
            inputs.add(new Parameter(new StrId(arguments[i]), paraTypes.get(i)));
        }
        ParameterListPlain parameterListPlain = new ParameterListPlain(inputs);
        ParameterList parameterList = new ParameterList(parameterListPlain);
        StmtList stmtList = ScriptParser.parseStmtList(def.get(NEXT_KEY).asText(), blocks);
        return new ProcedureDefinition(ident, parameterList, stmtList);
    }

    private static List<Type> addType(JsonNode blocks, List<Type> types, String textValue) {
        JsonNode param = blocks.get(textValue);
        final String opcodeString = param.get(OPCODE_KEY).asText();
        if (opcodeString.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            types.add(new BooleanType());

        } else {
            types.add(new StringType());
        }
        return types;
    }
}
