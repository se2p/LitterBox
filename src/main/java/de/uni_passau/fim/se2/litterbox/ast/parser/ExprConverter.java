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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

abstract class ExprConverter {

    protected ExprConverter() {
    }

    static Expression convertExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final KnownInputs exprInput
    ) {
        final RawInput input = containingBlock.getInput(exprInput);
        return convertExpr(state, containingBlock, input);
    }

    static Expression convertExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (NumExprConverter.parseableAsNumExpr(state.getCurrentTarget(), exprBlock)) {
            return NumExprConverter.convertNumExpr(state, containingBlock, exprBlock);
        } else if (StringExprConverter.parseableAsStringExpr(state.getCurrentTarget(), exprBlock)) {
            return StringExprConverter.convertStringExpr(state, containingBlock, exprBlock);
        } else if (BoolExprConverter.parseableAsBoolExpr(state.getCurrentTarget(), exprBlock)) {
            return BoolExprConverter.convertBoolExpr(state, containingBlock, exprBlock);
        } else if (DataExprConverter.parseableAsDataExpr(state.getCurrentTarget(), exprBlock)) {
            return DataExprConverter.convertDataExpr(state, exprBlock);
        } else {
            return new UnspecifiedExpression();
        }
    }

    static ExpressionStmt convertExprStmt(
            final ProgramParserState state,
            final RawBlockId blockId,
            final RawBlock exprBlock
    ) {
        final SymbolTable symbolTable = state.getSymbolTable();

        switch (exprBlock) {
            case RawBlock.RawRegularBlock regularExprBlock -> {
                final Expression expr = convertExprBlock(state, blockId, regularExprBlock);
                return new ExpressionStmt(expr);
            }
            case RawBlock.RawVariable variable -> {
                final VariableInfo varInfo = symbolTable.getVariable(
                        variable.id().id(), variable.name(), state.getCurrentActor().getName()
                ).orElseThrow(() -> new InternalParsingException(
                        "Program contains unknown variable '" + variable.name() + "' in actor '"
                                + state.getCurrentActor().getName() + "'."
                ));
                final Qualified varId = ConverterUtilities.topLevelVariableInfoToIdentifier(varInfo, variable);

                return new ExpressionStmt(varId);
            }
            case RawBlock.RawList list -> {
                final ExpressionListInfo listInfo = symbolTable.getList(
                        list.id().id(), list.name(), state.getCurrentActor().getName()
                ).orElseThrow(() -> new InternalParsingException(
                        "Program contains unknown list '" + list.name() + "' in actor '"
                                + state.getCurrentActor().getName() + "'."
                ));
                final Qualified listId = ConverterUtilities.topLevelListInfoToIdentifier(listInfo, list);

                return new ExpressionStmt(listId);
            }
            default -> throw new InternalParsingException("Unknown format for expression statement.");
        }
    }

    private static Expression convertExprBlock(
            final ProgramParserState state, final RawBlockId blockId, final RawBlock.RawRegularBlock block
    ) {
        if (NumExprOpcode.contains(block.opcode())) {
            return NumExprConverter.convertNumExpr(state, blockId, block);
        } else if (StringExprOpcode.contains(block.opcode())) {
            return StringExprConverter.convertStringExpr(state, blockId, block);
        } else if (BoolExprOpcode.contains(block.opcode())) {
            return BoolExprConverter.convertBoolExpr(state, blockId, block);
        } else {
            throw new InternalParsingException("Unknown opcode for expression: " + block.opcode());
        }
    }

    protected static boolean hasCorrectShadow(final RawInput exprBlock) {
        return exprBlock.shadowType() == ShadowType.SHADOW || (
                exprBlock.shadowType() == ShadowType.NO_SHADOW && !(exprBlock.input() instanceof BlockRef.IdRef)
                );
    }
}
