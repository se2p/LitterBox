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

import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.KnownFields;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.KnownInputs;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;

final class ActorLookStmtConverter extends StmtConverter<ActorLookStmt> {
    ActorLookStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    ActorLookStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock stmtBlock) {
        final ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(stmtBlock.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, stmtBlock);

        return switch (opcode) {
            case sensing_askandwait -> {
                final StringExpr question = StringExprConverter.convertStringExpr(
                        state, stmtBlock, KnownInputs.QUESTION
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
                final Identifier variable = getOrCreateReferencedVariable(stmtBlock);
                yield new ShowVariable(variable, metadata);
            }
            case data_hidevariable -> {
                final Identifier variable = getOrCreateReferencedVariable(stmtBlock);
                yield new HideVariable(variable, metadata);
            }
            case data_showlist -> {
                final Identifier variable = getOrCreateReferencedList(stmtBlock);
                yield new ShowList(variable, metadata);
            }
            case data_hidelist -> {
                final Identifier variable = getOrCreateReferencedList(stmtBlock);
                yield new HideList(variable, metadata);
            }
            case looks_changeeffectby -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, stmtBlock, KnownInputs.CHANGE);
                final String effectName = stmtBlock.getFieldValueAsString(KnownFields.EFFECT);
                yield new ChangeGraphicEffectBy(new GraphicEffect(effectName), numExpr, metadata);
            }
            case looks_seteffectto -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, stmtBlock, KnownInputs.VALUE);
                final String effectName = stmtBlock.getFieldValueAsString(KnownFields.EFFECT);
                final GraphicEffect effect = new GraphicEffect(effectName);
                yield new SetGraphicEffectTo(effect, numExpr, metadata);
            }
        };
    }
}
