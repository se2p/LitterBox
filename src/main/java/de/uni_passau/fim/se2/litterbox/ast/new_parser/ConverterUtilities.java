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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.BlockRef;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawMutation;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

/**
 * Groups various small helper functions for the converters that are not worth having their own class.
 */
final class ConverterUtilities {
    private ConverterUtilities() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Qualified variableInfoToIdentifier(final VariableInfo variableInfo, final String variableName) {
        return new Qualified(new StrId(variableInfo.getActor()), new Variable(new StrId(variableName)));
    }

    static Qualified listInfoToIdentifier(final ExpressionListInfo listInfo, final String listName) {
        return new Qualified(new StrId(listInfo.getActor()), new ScratchList(new StrId(listName)));
    }

    static MutationMetadata convertMutation(final RawMutation mutation) {
        return new ProcedureMutationMetadata(mutation.warp());
    }

    static Touchable convertTouchable(final ProgramParserState state, final RawBlock.RawRegularBlock block) {
        final String opcode = block.opcode();

        if (BoolExprOpcode.sensing_touchingobject.name().equals(opcode)) {
            final RawInput touched = block.inputs().get(Constants.TOUCHINGOBJECTMENU);

            if (ShadowType.SHADOW == touched.shadowType()) {
                return convertTouchableMenuOption(state.getCurrentTarget(), touched);
            } else {
                final Expression expr = ExpressionConverter.convertExpr(state, block, touched);
                return new AsTouchable(expr);
            }
        } else if (BoolExprOpcode.sensing_touchingcolor.name().equals(opcode)) {
            return convertColor(state, block, block.inputs().get(Constants.COLOR_KEY));
        } else {
            throw new InternalParsingException("Unknown touchable type. Missing parser implementation.");
        }
    }

    private static Touchable convertTouchableMenuOption(final RawTarget target, final RawInput touched) {
        if (!(touched.input() instanceof BlockRef.IdRef touchedId)) {
            throw new InternalParsingException("Unknown format for touched object.");
        }

        final RawBlock obj = target.blocks().get(touchedId.id());
        if (!(obj instanceof RawBlock.RawRegularBlock touchedTarget)) {
            throw new InternalParsingException("Unknown format for touched object.");
        }

        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(touchedId.id(), touchedTarget);

        final String touchedObjectName = touchedTarget.fields().get(Constants.TOUCHINGOBJECTMENU).value().toString();
        return switch (touchedObjectName) {
            case Constants.MOUSE -> new MousePointer(metadata);
            case Constants.TOUCHING_EDGE -> new Edge(metadata);
            default -> new SpriteTouchable(new StringLiteral(touchedObjectName), metadata);
        };
    }

    static Color convertColor(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingBlock, final RawInput block
    ) {
        // FIXME parse inputs that are not a text color as a "FromNumber" color

        if (block.input() instanceof BlockRef.Block dataBlock
                && dataBlock.block() instanceof RawBlock.RawColorLiteral color
                && color.color().startsWith("#")) {
            final long r = Long.parseLong(color.color().substring(1, 3), 16);
            final long g = Long.parseLong(color.color().substring(3, 5), 16);
            final long b = Long.parseLong(color.color().substring(5, 7), 16);

            return new ColorLiteral(r, g, b);
        } else {
            final NumExpr numExpr = NumExprConverter.convertNumExpr(state, containingBlock, block);
            return new FromNumber(numExpr);
        }
    }

    static ElementChoice convertElementChoice(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingStmt
    ) {
        throw new UnsupportedOperationException("todo: element choice");
    }
}
