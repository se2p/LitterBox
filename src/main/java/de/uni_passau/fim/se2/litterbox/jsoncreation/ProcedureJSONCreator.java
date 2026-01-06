/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;

public class ProcedureJSONCreator {

    /**
     * Updates type placeholders in the procedure signature to match actual parameter types.
     *
     * <p>
     * Scratch proccode strings contain type placeholders: %b (boolean), %n (number), %s (string/other).
     * This method ensures the placeholders match the actual parameter types in the definition.
     *
     * <p>
     * If there are fewer parameters than placeholders, extra placeholders are preserved
     * to support OrphanedParameter bug injection.
     *
     * @param proccode the original procedure signature with type placeholders
     * @param params   the current list of parameter definitions
     * @return the updated proccode with synchronized type placeholders
     */
    private static String syncTypePlaceholders(String proccode, List<ParameterDefinition> params) {
        StringBuilder result = new StringBuilder();
        // Split by placeholders while keeping them: "move %n steps" -> ["move ", "%n", " steps"]
        String[] segments = proccode.split("(?=%[sbn])|(?<=%[sbn])");
        int paramIndex = 0;

        for (String segment : segments) {
            if (segment.matches("%[sbn]")) {
                if (paramIndex < params.size()) {
                    result.append(getTypePlaceholder(params.get(paramIndex)));
                    paramIndex++;
                } else {
                    // Preserve placeholder for OrphanedParameter bugs
                    result.append(segment);
                }
            } else {
                result.append(segment);
            }
        }
        return result.toString();
    }

    /**
     * Returns the appropriate Scratch type placeholder for a parameter.
     */
    private static String getTypePlaceholder(ParameterDefinition param) {
        if (param.getType() instanceof BooleanType) {
            return "%b";
        } else if (param.getType() instanceof NumberType) {
            return "%n";
        } else {
            return "%s";
        }
    }

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
            for (Stmt stmt : stmtList.getStmts()) {
                nextId = vis.getBlockId(stmt);
                if (nextId != null) {
                    break;
                }
            }
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
            // Always serialize parameters - generate synthetic block ID if needed
            parameterInfos.add(createParameters(jsonString, protoId, parameterDefinition));
            String argumentId = procInfo.getName().replace(" ", "_") + "_argument_" + i;
            argumentIds.add(argumentId);
            if (!parameterInfos.isEmpty()) {
                jsonString.append(",");
            }
            inputs.add(createReferenceInput(argumentId,
                    INPUT_SAME_BLOCK_SHADOW, parameterInfos.get(i).getId(), false));
            i++;
        }

        // Sync type placeholders in proccode with actual parameter types
        String proccode = syncTypePlaceholders(procInfo.getName(), parameterDefinitions);

        String mutationString = createPrototypeMetadata(proccode,
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
        NonDataBlockMetadata metadata;
        if (parameterDefinition.getMetadata() instanceof NoBlockMetadata) {
            metadata = NonDataBlockMetadata.emptyNonBlockMetadata();
        } else {
            metadata = (NonDataBlockMetadata) parameterDefinition.getMetadata();
        }

        jsonString.append(createBlockWithoutMutationString(metadata,
                null,
                prototypeId, EMPTY_VALUE, createFields(VALUE_KEY,
                        parameterDefinition.getIdent().getName(), null), parameterDefinition.getOpcode()));
        return new ParameterInfo(parameterDefinition.getIdent().getName(), metadata.getBlockId(),
                parameterDefinition.getType());
    }
}


