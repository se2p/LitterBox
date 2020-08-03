/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PrototypeMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.*;

public class ProcedureJSONCreator {
    public static String createProcedureJSONString(ProcedureDefinition definition,
                                                   String actorName, SymbolTable symbol,
                                                   ProcedureDefinitionNameMapping procDefNameMapping) {
        StringBuilder jsonString = new StringBuilder();
        TopNonDataBlockMetadata defMetadata = (TopNonDataBlockMetadata) definition.getMetadata().getDefinition();
        NonDataBlockMetadata protoMetadata = (NonDataBlockMetadata) definition.getMetadata().getPrototype();
        String protoId = protoMetadata.getBlockId();
        StmtList stmtList = definition.getStmtList();
        String nextId = null;

        if (stmtList.getStmts().size() > 0) {
            IdVisitor vis = new IdVisitor();
            nextId = vis.getBlockId(stmtList.getStmts().get(0));
        }

        StrId id = (StrId) definition.getIdent();
        ProcedureInfo procInfo = procDefNameMapping.getProcedures().get(actorName).get(id);

        //create definition block
        List<String> inputs = new ArrayList<>();
        inputs.add(createReferenceInput(CUSTOM_BLOCK_KEY, INPUT_SAME_BLOCK_SHADOW, protoMetadata.getBlockId(), false));
        jsonString.append(createBlockWithoutMutationString(defMetadata, nextId,
                null, createInputs(inputs), EMPTY_VALUE)).append(",");

        //create parameters
        List<ParameterDefinition> parameterDefinitions =
                definition.getParameterDefinitionList().getParameterDefinitions();
        List<ParameterInfo> parameterInfos = new ArrayList<>();
        for (ParameterDefinition parameterDefinition : parameterDefinitions) {
            parameterInfos.add(createParameters(jsonString, protoId, parameterDefinition));
            jsonString.append(",");
        }

        //create prototype
        PrototypeMutationMetadata protoMutationMeta = (PrototypeMutationMetadata) protoMetadata.getMutation();
        List<String> argumentIds = protoMutationMeta.getArgumentIds();
        Preconditions.checkArgument(argumentIds.size() >= parameterInfos.size(), "Number of parameters is not equal "
                + "to the number of argument ids");
        inputs = new ArrayList<>();
        for (int i = 0; i < argumentIds.size(); i++) {
            inputs.add(createReferenceInput(argumentIds.get(i), INPUT_SAME_BLOCK_SHADOW, parameterInfos.get(i).getId(),
                    false));
        }

        String mutationString = createPrototypeMetadata(protoMutationMeta.getTagName(), procInfo.getName(),
                argumentIds, parameterInfos, protoMutationMeta.isWarp());
        jsonString.append(createBlockWithMutationString(protoMetadata, null, defMetadata.getBlockId(),
                createInputs(inputs), EMPTY_VALUE, mutationString));

        if (stmtList.getStmts().size() > 0) {
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
                prototypeId, EMPTY_VALUE, createFields(metadata.getFields().getList().get(0).getFieldsName(),
                        parameterDefinition.getIdent().getName(), null)));
        return new ParameterInfo(parameterDefinition.getIdent().getName(), metadata.getBlockId(),
                parameterDefinition.getType());
    }
}


