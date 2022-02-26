/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;

public class ProcedureJSONCreator {

    /**
     * This method creates a JSON String of a {@link ProcedureDefinition} that can be used in a Scratch JSON.
     *
     * @param definition         the {@link ProcedureDefinition} that should be converted to a JSON String
     * @param actorName          the name of the {@link ActorDefinition} which holds the {@link ProcedureDefinition}
     * @param symbol             the {@link SymbolTable} of the {@link Program}
     * @param procDefNameMapping the {@link ProcedureDefinitionNameMapping} of the {@link Program}
     * @return The complete JSON String that corresponds to the given {@link ProcedureDefinition}
     */
    public static String createProcedureJSONString(ProcedureDefinition definition,
                                                   String actorName, SymbolTable symbol,
                                                   ProcedureDefinitionNameMapping procDefNameMapping) {
        StringBuilder jsonString = new StringBuilder();
        TopNonDataBlockMetadata defMetadata = (TopNonDataBlockMetadata) definition.getMetadata().getDefinition();
        NonDataBlockMetadata protoMetadata = (NonDataBlockMetadata) definition.getMetadata().getPrototype();
        String protoId = protoMetadata.getBlockId();
        StmtList stmtList = definition.getStmtList();
        String nextId = null;

        if (!stmtList.getStmts().isEmpty()) {
            IdVisitor vis = new IdVisitor();
            nextId = vis.getBlockId(stmtList.getStmts().get(0));
        }

        StrId id = (StrId) definition.getIdent();
        ProcedureInfo procInfo = procDefNameMapping.getProcedures().get(actorName).get(id);

        //create definition block
        List<String> inputs = new ArrayList<>();
        inputs.add(createReferenceInput(CUSTOM_BLOCK_KEY, INPUT_SAME_BLOCK_SHADOW, protoMetadata.getBlockId(), false));
        jsonString.append(createBlockWithoutMutationString(defMetadata, nextId,
                null, createInputs(inputs), EMPTY_VALUE, definition.getOpcode())).append(",");

        //create prototype
        ProcedureMutationMetadata protoMutationMeta = (ProcedureMutationMetadata) protoMetadata.getMutation();
        List<String> argumentIds = new ArrayList<>();
        inputs = new ArrayList<>();

        //create parameters
        List<ParameterDefinition> parameterDefinitions =
                definition.getParameterDefinitionList().getParameterDefinitions();
        List<ParameterInfo> parameterInfos = new ArrayList<>();
        int i = 0;
        for (ParameterDefinition parameterDefinition : parameterDefinitions) {
            if (!(parameterDefinition.getMetadata() instanceof NoBlockMetadata)) {
                parameterInfos.add(createParameters(jsonString, protoId, parameterDefinition));
                String argumentId = procInfo.getName().replace(" ","_") + "_argument_" + i;
                argumentIds.add(argumentId);
                jsonString.append(",");
                inputs.add(createReferenceInput(argumentId,
                        INPUT_SAME_BLOCK_SHADOW, parameterInfos.get(i).getId(), false));
            }
            i++;
        }

        String mutationString = createPrototypeMetadata(procInfo.getName(),
                argumentIds, parameterInfos, protoMutationMeta.isWarp());
        jsonString.append(createBlockWithMutationString(protoMetadata, null, defMetadata.getBlockId(),
                createInputs(inputs), EMPTY_VALUE, mutationString, definition.getPrototypeOpcode()));

        if (!stmtList.getStmts().isEmpty()) {
            StmtListJSONCreator stmtListJSONCreator =
                    new StmtListJSONCreator(defMetadata.getBlockId(), definition.getStmtList(), symbol);
            jsonString.append(",");
            jsonString.append(stmtListJSONCreator.createStmtListJSONString());
        }
        return jsonString.toString();
    }

    private static ParameterInfo createParameters(StringBuilder jsonString, String prototypeId,
                                                  ParameterDefinition parameterDefinition) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) parameterDefinition.getMetadata();
        jsonString.append(createBlockWithoutMutationString(metadata,
                null,
                prototypeId, EMPTY_VALUE, createFields(VALUE_KEY,
                        parameterDefinition.getIdent().getName(), null), parameterDefinition.getOpcode()));
        return new ParameterInfo(parameterDefinition.getIdent().getName(), metadata.getBlockId(),
                parameterDefinition.getType());
    }
}


