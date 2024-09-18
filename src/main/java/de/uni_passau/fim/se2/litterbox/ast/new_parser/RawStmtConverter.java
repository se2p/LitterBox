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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

final class RawStmtConverter {

    private static final Logger log = Logger.getLogger(RawStmtConverter.class.getName());

    private static final String STOP_OPTION = "STOP_OPTION";

    private final ProgramParserState state;

    private final Map<String, StmtConverter<?>> converterChoices;

    RawStmtConverter(final ProgramParserState state) {
        this.state = state;
        this.converterChoices = buildConverterSelector();
    }

    Stmt convertStmt(final RawBlockId blockId, final RawBlock stmtBlock) {
        if (stmtBlock instanceof RawBlock.ArrayBlock arrayBlock) {
            final Expression expr = ExpressionConverter.convertExprStmt(state, arrayBlock);
            return new ExpressionStmt(expr);
        } else if (stmtBlock instanceof RawBlock.RawRegularBlock regularBlock) {
            return converStmt(blockId, regularBlock);
        } else {
            // should never happen unless sealed interface RawBlock changes,
            // use pattern-matching switch when upgrading to Java 21
            throw new InternalParsingException("Unknown statement format.");
        }
    }

    private Stmt converStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock stmtBlock) {
        final String opcode = stmtBlock.opcode();

        if (isTerminationStmt(stmtBlock)) {
            return convertTerminationStmt(stmtBlock);
        } else if (
                ProcedureOpcode.argument_reporter_boolean.name().equals(opcode)
                        || ProcedureOpcode.argument_reporter_string_number.name().equals(opcode)
        ) {
            throw new UnsupportedOperationException("todo: dead parameter conversion");
        } else if (converterChoices.containsKey(opcode)) {
            return converterChoices.get(opcode).convertStmt(blockId, stmtBlock);
        } else {
            if (PropertyLoader.getSystemBooleanProperty("parser.log_unknown_opcode")) {
                log.warning("Block with ID '" + blockId + "' and unknown opcode '" + opcode + "'. ");
            }

            return new UnspecifiedStmt();
        }
    }

    private boolean isTerminationStmt(final RawBlock.RawRegularBlock stmt) {
        final boolean hasStopOption = stmt.fields().containsKey(STOP_OPTION);
        final boolean otherScriptsExist = hasStopOption && referencesStopOthers(stmt);

        return TerminationStmtOpcode.contains(stmt.opcode()) && !otherScriptsExist;
    }

    private boolean referencesStopOthers(final RawBlock.RawRegularBlock stmt) {
        final String stopOptionValue = stmt.fields().get(STOP_OPTION).value().toString();
        return "other scripts in sprite".equals(stopOptionValue) || "other scripts in stage".equals(stopOptionValue);
    }

    private TerminationStmt convertTerminationStmt(final RawBlock.RawRegularBlock stmt) {
        throw new UnsupportedOperationException("todo: termination");
    }

    /**
     * Builds a lookup table from opcode to the converter that can be used to convert this type of statement.
     *
     * <p>The usual {@code Opcode.contains()} check would in the worst case loop through all possible opcodes before
     * finding the one that is supported, since we would have to do an if-elseif-chain checking them one-by-one.
     *
     * @return A lookup table from opcode to suitable converter.
     */
    private Map<String, StmtConverter<?>> buildConverterSelector() {
        final Map<String, StmtConverter<?>> choices = new HashMap<>();

        addOpcodes(choices, BoolExprOpcode.values(), new ExprStmtConverter(state));
        addOpcodes(choices, NumExprOpcode.values(), new ExprStmtConverter(state));
        addOpcodes(choices, StringExprOpcode.values(), new ExprStmtConverter(state));
        addOpcodes(choices, ActorLookStmtOpcode.values(), new ActorLookStmtConverter(state));
        addOpcodes(choices, ControlStmtOpcode.values(), new ControlStmtConverter(state));
        addOpcodes(choices, CommonStmtOpcode.values(), null);
        addOpcodes(choices, SpriteMotionStmtOpcode.values(), new SpriteMotionStmtConverter(state));
        addOpcodes(choices, SpriteLookStmtOpcode.values(), new SpriteLookStmtConverter(state));
        addOpcodes(choices, ActorSoundStmtOpcode.values(), new ActorSoundStmtConverter(state));
        addOpcodes(choices, CallStmtOpcode.values(), new CallStmtConverter(state));
        addOpcodes(choices, ListStmtOpcode.values(), new ListStmtConverter(state));
        addOpcodes(choices, SetStmtOpcode.values(), null);
        addOpcodes(choices, PenOpcode.values(), null);
        addOpcodes(choices, TextToSpeechOpcode.values(), null);
        addOpcodes(choices, MusicOpcode.values(), null);
        // mBlock extension blocks
        addOpcodes(choices, EmotionStmtOpcode.values(), null);
        addOpcodes(choices, LEDMatrixStmtOpcode.values(), null);
        addOpcodes(choices, LEDStmtOpcode.values(), null);
        addOpcodes(choices, SpeakerStmtOpcode.values(), null);
        addOpcodes(choices, RobotMoveStmtOpcode.values(), null);
        addOpcodes(choices, ResetStmtOpcode.values(), null);
        addOpcodes(choices, IRStmtOpcode.values(), null);

        return choices;
    }

    private static void addOpcodes(
            final Map<String, StmtConverter<?>> choices, final Opcode[] opcodes, final StmtConverter<?> choice
    ) {
        Arrays.stream(opcodes).map(Opcode::getName).forEach(v -> choices.put(v, choice));
    }

    private static class ExprStmtConverter extends StmtConverter<ExpressionStmt> {

        ExprStmtConverter(final ProgramParserState state) {
            super(state);
        }

        @Override
        ExpressionStmt convertStmt(RawBlockId blockId, RawBlock.RawRegularBlock block) {
            final Expression expr = ExpressionConverter.convertExprStmt(state, blockId, block);
            return new ExpressionStmt(expr);
        }
    }
}
