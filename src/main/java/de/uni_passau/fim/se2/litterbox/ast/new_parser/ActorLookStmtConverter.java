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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawField;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Collections;

class ActorLookStmtConverter {
    private ActorLookStmtConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static ActorLookStmt convertStmt(
            final ProgramParserState state, final RawBlockId blockId, final RawBlock.RawRegularBlock stmtBlock
    ) {
        final ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(stmtBlock.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, stmtBlock);

        return switch (opcode) {
            case sensing_askandwait -> {
                final StringExpr question = StringExprConverter.convertStringExpr(
                        state, stmtBlock, stmtBlock.inputs().get(Constants.QUESTION_KEY)
                );
                yield new AskAndWait(question, metadata);
            }
            case looks_nextbackdrop -> new NextBackdrop(metadata);
            case looks_switchbackdroptoandwait -> {
                final ElementChoice elementChoice = ConverterUtilities.convertElementChoice(state, stmtBlock);
                yield new SwitchBackdropAndWait(elementChoice, metadata);
            }
            case looks_switchbackdropto -> {
                final ElementChoice elementChoice = ConverterUtilities.convertElementChoice(state, stmtBlock);
                yield new SwitchBackdrop(elementChoice, metadata);
            }
            case looks_cleargraphiceffects -> new ClearGraphicEffects(metadata);
            case data_showvariable -> {
                final Identifier variable = getOrCreateReferencedVariable(state, stmtBlock);
                yield new ShowVariable(variable, metadata);
            }
            case data_hidevariable -> {
                final Identifier variable = getOrCreateReferencedVariable(state, stmtBlock);
                yield new HideVariable(variable, metadata);
            }
            case data_showlist -> {
                final Identifier variable = getOrCreateReferencedList(state, stmtBlock);
                yield new ShowList(variable, metadata);
            }
            case data_hidelist -> {
                final Identifier variable = getOrCreateReferencedList(state, stmtBlock);
                yield new HideList(variable, metadata);
            }
            case looks_changeeffectby -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(
                        state, stmtBlock, stmtBlock.inputs().get(Constants.CHANGE_KEY)
                );
                final String effectName = stmtBlock.fields().get(Constants.EFFECT_KEY).value().toString();
                yield new ChangeGraphicEffectBy(new GraphicEffect(effectName), numExpr, metadata);
            }
            case looks_seteffectto -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(
                        state, stmtBlock, stmtBlock.inputs().get(Constants.VALUE_KEY)
                );
                final String effectName = stmtBlock.fields().get(Constants.EFFECT_KEY).value().toString();
                final GraphicEffect effect = new GraphicEffect(effectName);
                yield new SetGraphicEffectTo(effect, numExpr, metadata);
            }
        };
    }

    private static Qualified getOrCreateReferencedVariable(
            final ProgramParserState state, final RawBlock.RawRegularBlock stmtBlock
    ) {
        final RawField varField = stmtBlock.fields().get(Constants.VARIABLE_KEY);
        final String varName = varField.value().toString();
        final RawBlockId varId = varField.id()
                .orElseThrow(() -> new InternalParsingException("Missing variable id."));

        final VariableInfo varInfo = state.getSymbolTable().getOrAddVariable(
                varId.id(), varName, state.getCurrentActor().getName(),
                StringType::new, true, "Stage"
        );

        return new Qualified(new StrId(varInfo.getActor()), new Variable(new StrId(varName)));
    }

    private static Qualified getOrCreateReferencedList(
            final ProgramParserState state, final RawBlock.RawRegularBlock stmtBlock
    ) {
        final RawField listField = stmtBlock.fields().get(Constants.LIST_KEY);
        final String listName = listField.value().toString();
        final RawBlockId listId = listField.id()
                .orElseThrow(() -> new InternalParsingException("Missing variable id."));

        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                listId.id(), listName, state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );

        return new Qualified(new StrId(listInfo.getActor()), new ScratchList(new StrId(listName)));
    }
}
