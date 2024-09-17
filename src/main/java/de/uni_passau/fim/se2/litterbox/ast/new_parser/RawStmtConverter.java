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

    private final Map<String, ConverterChoice> converterChoices = buildParserSelector();

    RawStmtConverter(final ProgramParserState state) {
        this.state = state;
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
            return convertStmt(converterChoices.get(opcode), blockId, stmtBlock);
        } else {
            if (PropertyLoader.getSystemBooleanProperty("parser.log_unknown_opcode")) {
                log.warning("Block with ID '" + blockId + "' and unknown opcode '" + opcode + "'. ");
            }

            return new UnspecifiedStmt();
        }
    }

    private Stmt convertStmt(
            final ConverterChoice converterChoice, final RawBlockId blockId, final RawBlock.RawRegularBlock stmtBlock
    ) {
        return switch (converterChoice) {
            case Expression -> {
                final Expression expr = ExpressionConverter.convertExprStmt(state, blockId, stmtBlock);
                yield new ExpressionStmt(expr);
            }
            case ActorLook -> ActorLookStmtConverter.convertStmt(state, blockId, stmtBlock);
            case Control -> null;
            case Common -> null;
            case SpriteMotion -> null;
            case SpriteLook -> null;
            case ActorSound -> null;
            case Call -> null;
            case List -> null;
            case Set -> null;
            case Pen -> null;
            case TextToSpeech -> null;
            case Emotion -> null;
            case LedMatrix -> null;
            case Led -> null;
            case Speaker -> null;
            case RobotMove -> null;
            case Reset -> null;
            case Ir -> null;
            case Music -> null;
        };
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
    // ToDo: benchmark this compared to the original style lookup
    private static Map<String, ConverterChoice> buildParserSelector() {
        final Map<String, ConverterChoice> choices = new HashMap<>();

        addOpcodes(choices, BoolExprOpcode.values(), ConverterChoice.Expression);
        addOpcodes(choices, NumExprOpcode.values(), ConverterChoice.Expression);
        addOpcodes(choices, StringExprOpcode.values(), ConverterChoice.Expression);
        addOpcodes(choices, ActorLookStmtOpcode.values(), ConverterChoice.ActorLook);
        addOpcodes(choices, ControlStmtOpcode.values(), ConverterChoice.Control);
        addOpcodes(choices, CommonStmtOpcode.values(), ConverterChoice.Common);
        addOpcodes(choices, SpriteMotionStmtOpcode.values(), ConverterChoice.SpriteMotion);
        addOpcodes(choices, SpriteLookStmtOpcode.values(), ConverterChoice.SpriteLook);
        addOpcodes(choices, ActorSoundStmtOpcode.values(), ConverterChoice.ActorSound);
        addOpcodes(choices, CallStmtOpcode.values(), ConverterChoice.Call);
        addOpcodes(choices, ListStmtOpcode.values(), ConverterChoice.List);
        addOpcodes(choices, SetStmtOpcode.values(), ConverterChoice.Set);
        addOpcodes(choices, PenOpcode.values(), ConverterChoice.Pen);
        addOpcodes(choices, TextToSpeechOpcode.values(), ConverterChoice.TextToSpeech);
        addOpcodes(choices, EmotionStmtOpcode.values(), ConverterChoice.Emotion);
        addOpcodes(choices, LEDMatrixStmtOpcode.values(), ConverterChoice.LedMatrix);
        addOpcodes(choices, LEDStmtOpcode.values(), ConverterChoice.Led);
        addOpcodes(choices, SpeakerStmtOpcode.values(), ConverterChoice.Speaker);
        addOpcodes(choices, RobotMoveStmtOpcode.values(), ConverterChoice.RobotMove);
        addOpcodes(choices, ResetStmtOpcode.values(), ConverterChoice.Reset);
        addOpcodes(choices, IRStmtOpcode.values(), ConverterChoice.Ir);
        addOpcodes(choices, MusicOpcode.values(), ConverterChoice.Music);

        return choices;
    }

    private static void addOpcodes(
            final Map<String, ConverterChoice> choices, final Opcode[] opcodes, final ConverterChoice choice
    ) {
        Arrays.stream(opcodes).map(Opcode::getName).forEach(v -> choices.put(v, choice));
    }

    private enum ConverterChoice {
        Expression,
        ActorLook,
        Control,
        Common,
        SpriteMotion,
        SpriteLook,
        ActorSound,
        Call,
        List,
        Set,
        Pen,
        TextToSpeech,
        Emotion,
        LedMatrix,
        Led,
        Speaker,
        RobotMove,
        Reset,
        Ir,
        Music;
    }
}
