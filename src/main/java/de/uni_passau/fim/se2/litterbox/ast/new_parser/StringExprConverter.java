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

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.IRMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;

import java.util.Collections;
import java.util.Optional;

final class StringExprConverter extends ExprConverter {

    private StringExprConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static StringExpr convertStringExpr(
            final ProgramParserState state,
            final RawTarget target,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (!parseableAsStringExpr(target, exprBlock)) {
            final Expression expr = ExpressionConverter.convertExpr(state, target, containingBlock, exprBlock);
            return new AsString(expr);
        }

        if (hasCorrectShadow(exprBlock)) {
            return parseLiteralStringInput(exprBlock);
        }

        if (
                exprBlock.input() instanceof BlockRef.IdRef exprInput
                        && target.blocks().get(exprInput.id()) instanceof RawBlock.RawRegularBlock exprInputRegularBlock
        ) {
            return parseBlockStringExpr(state, target, exprInput.id(), exprInputRegularBlock);
        }

        throw new InternalParsingException("Could not parse NumExpr.");
    }

    /**
     * Checks if the block can be parsed as {@link StringExpr} without wrapping in {@link AsString}.
     *
     * @param target    The actor the block appears in.
     * @param exprBlock The expression to check.
     * @return True, iff the {@code exprBlock} can be parsed as {@link NumExpr}.
     */
    static boolean parseableAsStringExpr(final RawTarget target, final RawInput exprBlock) {
        final boolean parseableAsNumberLiteral = isParseableAsStringLiteral(exprBlock);
        final boolean hasNumExprOpcode = hasStringExprOpcode(target, exprBlock);

        return parseableAsNumberLiteral || hasNumExprOpcode;
    }

    private static boolean isParseableAsStringLiteral(final RawInput exprBlock) {
        final boolean hasCorrectType = exprBlock.input() instanceof BlockRef.Block exprInput
                && exprInput.block() instanceof RawBlock.RawStringLiteral;

        return hasCorrectShadow(exprBlock) || hasCorrectType;
    }

    private static boolean hasStringExprOpcode(final RawTarget target, final RawInput exprBlock) {
        if (exprBlock.input() instanceof BlockRef.IdRef inputIdRef) {
            final RawBlock inputBlock = target.blocks().get(inputIdRef.id());
            if (inputBlock == null) {
                return false;
            }

            if (inputBlock instanceof RawBlock.RawRegularBlock inputRegularBlock) {
                return StringExprOpcode.contains(inputRegularBlock.opcode());
            }
        }

        return false;
    }

    private static StringExpr parseLiteralStringInput(final RawInput exprBlock) {
        if (exprBlock.input() instanceof BlockRef.Block inputBlock) {
            final RawBlock.ArrayBlock literalInput = inputBlock.block();
            if (literalInput instanceof RawBlock.RawStringLiteral s) {
                return new StringLiteral(s.value());
            } else if (literalInput instanceof RawBlock.RawBroadcast b) {
                return new StringLiteral(b.name());
            }
        }

        return new UnspecifiedStringExpr();
    }

    private static StringExpr parseBlockStringExpr(
            final ProgramParserState state,
            final RawTarget target,
            final RawBlockId id,
            final RawBlock.RawRegularBlock block
    ) {
        final StringExprOpcode opcode = StringExprOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(id, block);

        return switch (opcode) {
            case sensing_username -> new Username(metadata);
            case sensing_answer -> new Answer(metadata);
            case comm_receive_ir, detect_ir -> new IRMessage(metadata);
            case translate_getViewerLanguage -> new ViewerLanguage(metadata);
            case translate_getTranslate -> parseTranslate();
            case operator_join -> {
                final StringExpr left = convertStringExpr(
                        state, target, block, block.inputs().get(Constants.STRING1_KEY)
                );
                final StringExpr right = convertStringExpr(
                        state, target, block, block.inputs().get(Constants.STRING2_KEY)
                );
                yield new Join(left, right, metadata);
            }
            case operator_letter_of -> {
                final NumExpr num = NumExprConverter.convertNumExpr(
                        state, target, block, block.inputs().get(Constants.LETTER_KEY)
                );
                final StringExpr word = convertStringExpr(
                        state, target, block, block.inputs().get(Constants.STRING_KEY)
                );
                yield new LetterOf(num, word, metadata);
            }
            case looks_costumenumbername -> {
                final String name = block.fields().get(Constants.NUMBER_NAME_KEY).value().toString();
                yield new Costume(new NameNum(name), metadata);
            }
            case looks_backdropnumbername -> {
                final String name = block.fields().get(Constants.NUMBER_NAME_KEY).value().toString();
                yield new Backdrop(new NameNum(name), metadata);
            }
            case data_itemoflist -> parseItemOfList(state, target, metadata, block);
            case sensing_of -> parseSensingOf(state, target, metadata, block);
        };
    }

    private static ItemOfVariable parseItemOfList(
            final ProgramParserState state,
            final RawTarget target,
            final BlockMetadata metadata,
            final RawBlock.RawRegularBlock exprBlock
    ) {
        final NumExpr index = NumExprConverter.convertNumExpr(
                state, target, exprBlock, exprBlock.inputs().get(Constants.INDEX_KEY)
        );
        final RawBlockId listId = exprBlock.fields().get(Constants.LIST_KEY).id()
                .orElseThrow(() -> new InternalParsingException("ItemOfVariable block is missing reference to list!"));
        final String listName = exprBlock.fields().get(Constants.LIST_KEY).value().toString();
        final ExpressionListInfo listInfo = getListInfo(state, listId, listName);
        final Identifier variable = new Qualified(
                new StrId(listInfo.getActor()),
                new ScratchList(new StrId(listInfo.getVariableName()))
        );

        return new ItemOfVariable(index, variable, metadata);
    }

    private static ExpressionListInfo getListInfo(
            final ProgramParserState state, final RawBlockId listId, final String listName
    ) {
        final Optional<ExpressionListInfo> listInfo = state.getSymbolTable().getList(
                listId.id(), listName, state.getCurrentActor().getName()
        );
        if (listInfo.isPresent()) {
            return listInfo.get();
        } else {
            final ExpressionList content = new ExpressionList(Collections.emptyList());
            return state.getSymbolTable().addExpressionListInfo(
                    listId.id(), listName, content, true, state.getCurrentActor().getName()
            );
        }
    }

    private static AttributeOf parseSensingOf(
            final ProgramParserState state,
            final RawTarget target,
            final BlockMetadata metadata,
            final RawBlock.RawRegularBlock exprBlock
    ) {
        final String property = exprBlock.fields().get("PROPERTY").value().toString();
        final Attribute attribute = switch (property) {
            case "y position", "x position", "direction", "costume #", "costume name", "size", "volume",
                 "backdrop name", "backdrop #" -> new AttributeFromFixed(new FixedAttribute(property));
            default -> new AttributeFromVariable(new Variable(new StrId(property)));
        };

        final ElementChoice elementChoice;
        final RawInput input = exprBlock.inputs().get(Constants.OBJECT_KEY);

        if (ShadowType.SHADOW.equals(input.shadowType())) {
            if (input.input() instanceof BlockRef.IdRef menuBlockRef) {
                elementChoice = convertSensingOfMenu(state, target, menuBlockRef.id());
            } else {
                throw new InternalParsingException("Expected a menu item for sensing block.");
            }
        } else {
            final Expression expr = ExprConverter.convertExpr(state, target, exprBlock, input);
            elementChoice = new WithExpr(expr, new NoBlockMetadata());
        }

        return new AttributeOf(attribute, elementChoice, metadata);
    }

    private static ElementChoice convertSensingOfMenu(
            final ProgramParserState state, final RawTarget target, final RawBlockId menuId
    ) {
        final RawBlock menuBlock = target.blocks().get(menuId);
        final BlockMetadata menuMetadata = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);

        if (menuBlock instanceof RawBlock.RawRegularBlock menuRegularBlock) {
            if (DependentBlockOpcode.sensing_of_object_menu.getName().equalsIgnoreCase(menuRegularBlock.opcode())) {
                final String actorName = menuRegularBlock.fields().get(Constants.OBJECT_KEY).value().toString();
                return new WithExpr(new StrId(actorName), menuMetadata);
            } else {
                final Expression expr = ExpressionConverter.convertExpr(
                        state, target, menuRegularBlock, menuRegularBlock.inputs().get(Constants.OBJECT_KEY)
                );
                return new WithExpr(expr, menuMetadata);
            }
        } else {
            throw new InternalParsingException("Malformed menu for sensing block.");
        }
    }

    /**
     * Todo, see {@link de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser#parseTranslate(ProgramParserState, JsonNode, BlockMetadata, JsonNode)}
     * @return todo
     */
    private static TranslateTo parseTranslate() {
        throw new UnsupportedOperationException("todo: translate");
    }
}
