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

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Collections;

/**
 * Groups various small helper functions for the converters that are not worth having their own class.
 */
final class ConverterUtilities {
    private ConverterUtilities() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Qualified variableInfoToIdentifier(
            final VariableInfo variableInfo, final RawBlockId id, final String variableName
    ) {
        final DataBlockMetadata metadata = new DataBlockMetadata(id.id(), null, 0, 0);
        final Variable variable = new Variable(new StrId(variableName), metadata);
        return new Qualified(new StrId(variableInfo.actor()), variable);
    }

    static Qualified variableInfoToIdentifier(final VariableInfo variableInfo, final RawBlock.RawVariable variable) {
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(variable.id(), variable);
        final Variable v = new Variable(new StrId(variable.name()), metadata);

        return new Qualified(new StrId(variableInfo.actor()), v);
    }

    static Qualified listInfoToIdentifier(
            final ExpressionListInfo listInfo, final RawBlockId id, final String listName
    ) {
        final DataBlockMetadata metadata = new DataBlockMetadata(id.id(), null, 0.0, 0.0);
        final ScratchList list = new ScratchList(new StrId(listName), metadata);
        return new Qualified(new StrId(listInfo.actor()), list);
    }

    static Qualified listInfoToIdentifier(final ExpressionListInfo listInfo, final RawBlock.RawList list) {
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(list.id(), list);
        final ScratchList l =  new ScratchList(new StrId(list.name()), metadata);

        return new Qualified(new StrId(listInfo.actor()), l);
    }

    static Qualified getListField(final ProgramParserState state, final RawBlock.RawRegularBlock block) {
        final RawField listField = block.getField(KnownFields.LIST);
        final RawBlockId listId = listField.id()
                .orElseThrow(() -> new InternalParsingException("Referenced list is missing an identifier."));
        final String listName = listField.value().toString();

        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                listId.id(), listName, state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );

        return listInfoToIdentifier(listInfo, listId, listName);
    }

    static MutationMetadata convertMutation(final RawMutation mutation) {
        return new ProcedureMutationMetadata(mutation.warp());
    }

    static Touchable convertTouchable(final ProgramParserState state, final RawBlock.RawRegularBlock block) {
        final String opcode = block.opcode();

        if (BoolExprOpcode.sensing_touchingobject.name().equals(opcode)) {
            final RawInput touched = block.getInput(KnownInputs.TOUCHINGOBJECTMENU);

            if (ShadowType.SHADOW == touched.shadowType()) {
                return convertTouchableMenuOption(state.getCurrentTarget(), touched);
            } else {
                final Expression expr = ExprConverter.convertExpr(state, block, touched);
                return new AsTouchable(expr);
            }
        } else if (BoolExprOpcode.sensing_touchingcolor.name().equals(opcode)) {
            return convertColor(state, block, block.getInput(KnownInputs.COLOR));
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

        final String touchedObjectName = touchedTarget.getFieldValueAsString(KnownFields.TOUCHINGOBJECTMENU);
        return switch (touchedObjectName) {
            case Constants.MOUSE -> new MousePointer(metadata);
            case Constants.TOUCHING_EDGE -> new Edge(metadata);
            default -> new SpriteTouchable(new StringLiteral(touchedObjectName), metadata);
        };
    }

    static Color convertColor(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingBlock, final RawInput block
    ) {
        if (block.input() instanceof BlockRef.Block dataBlock
                && dataBlock.block() instanceof RawBlock.RawColorLiteral color
                && color.color().startsWith("#")
                && color.color().length() == 7
        ) {
            return ColorLiteral.tryFromRgbHexString(color.color());
        } else {
            final NumExpr numExpr = NumExprConverter.convertNumExpr(state, containingBlock, block);
            return new FromNumber(numExpr);
        }
    }

    static ElementChoice convertElementChoice(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingStmt
    ) {
        final RawInput elementChoiceInput = containingStmt.getInput(KnownInputs.BACKDROP);

        if (
                ShadowType.SHADOW.equals(elementChoiceInput.shadowType())
                && elementChoiceInput.input() instanceof BlockRef.IdRef blockIdRef
                && state.getBlock(blockIdRef.id()) instanceof RawBlock.RawRegularBlock menuBlock
        ) {
            return convertElementChoiceFromMenu(blockIdRef.id(), menuBlock);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, containingStmt, elementChoiceInput);
            return new WithExpr(expr, new NoBlockMetadata());
        }
    }

    private static ElementChoice convertElementChoiceFromMenu(
            final RawBlockId menuBlockId, final RawBlock.RawRegularBlock menuBlock
    ) {
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(menuBlockId, menuBlock);
        final String choiceName = menuBlock.getFieldValueAsString(KnownFields.BACKDROP);

        return switch (choiceName) {
            case "next backdrop" -> new Next(metadata);
            case "previous backdrop" -> new Prev(metadata);
            case "random backdrop" -> new Random(metadata);
            default -> new WithExpr(new StrId(choiceName), metadata);
        };
    }
}
