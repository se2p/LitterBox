/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Collections;

final class DataExprConverter extends ExprConverter {

    private DataExprConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static boolean parseableAsDataExpr(final RawTarget target, final RawInput exprBlock) {
        if (!(exprBlock.input() instanceof BlockRef.IdRef(RawBlockId inputId))) {
            // If not an IdRef, we technically would have to look up the identifier stored in the Block in the
            // symbol table. However, some JSON files contain references to IDs which are not present in the lookup
            // tables, and we want to keep these without exception.
            return true;
        }

        final RawBlock inputBlock = target.blocks().get(inputId);
        if (inputBlock == null) {
            return true;
        }

        if (inputBlock instanceof RawBlock.RawRegularBlock regularInputBlock) {
            return isParameter(regularInputBlock.opcode());
        } else {
            return false;
        }
    }

    static Expression convertDataExpr(final ProgramParserState state, final RawInput exprBlock) {
        switch (exprBlock.input()) {
            case BlockRef.IdRef(RawBlockId exprIdRef) -> {
                final RawBlock referencedBlock = state.getBlock(exprIdRef);
                if (!(referencedBlock instanceof RawBlock.RawRegularBlock referencedRegularBlock)) {
                    // should not happen if the parseableAsDataExpr check works as intended
                    throw new InternalParsingException("Unknown format for data expressions.");
                }

                if (isParameter(referencedRegularBlock.opcode())) {
                    return convertParameter(exprIdRef, referencedRegularBlock);
                }
            }
            case BlockRef.Block(RawBlock.ArrayBlock exprArrayBlock) -> {
                if (exprArrayBlock instanceof RawBlock.RawVariable rawVariable) {
                    return convertVariable(state, rawVariable);
                } else if (exprArrayBlock instanceof RawBlock.RawList rawList) {
                    return convertList(state, rawList);
                }
            }
        }

        // should not happen if the parseableAsDataExpr check works as intended
        throw new InternalParsingException("Unknown format for DataExpr.");
    }

    private static Expression convertParameter(
            final RawBlockId blockId, final RawBlock.RawRegularBlock parameterBlock
    ) {
        final String name = parameterBlock.getFieldValueAsString(KnownFields.VALUE);
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, parameterBlock);

        final Type type;
        if (ProcedureOpcode.argument_reporter_boolean.name().equals(parameterBlock.opcode())) {
            type = new BooleanType();
        } else {
            type = new StringType();
        }

        return new Parameter(new StrId(name), type, metadata);
    }

    private static Qualified convertVariable(final ProgramParserState state, final RawBlock.RawVariable variable) {
        final VariableInfo variableInfo = state.getSymbolTable().getOrAddVariable(
                variable.id().id(), variable.name(), state.getCurrentActor().getName(),
                StringType::new, true, "Stage"
        );
        return ConverterUtilities.variableInfoToIdentifier(variableInfo, variable.name());
    }

    private static Qualified convertList(final ProgramParserState state, final RawBlock.RawList list) {
        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                list.id().id(), list.name(), state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );
        return ConverterUtilities.listInfoToIdentifier(listInfo, list.name());
    }

    private static boolean isParameter(final String opcode) {
        return ProcedureOpcode.argument_reporter_boolean.name().equals(opcode)
                || ProcedureOpcode.argument_reporter_string_number.name().equals(opcode);
    }
}
