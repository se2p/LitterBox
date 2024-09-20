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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TLanguage;
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

final class StringExprConverter extends ExprConverter {

    private StringExprConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static StringExpr convertStringExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final KnownInputs inputKey
    ) {
        final RawInput input = containingBlock.getInput(inputKey);
        return convertStringExpr(state, containingBlock, input);
    }

    static StringExpr convertStringExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (!parseableAsStringExpr(state.getCurrentTarget(), exprBlock)) {
            final Expression expr = ExprConverter.convertExpr(state, containingBlock, exprBlock);
            return new AsString(expr);
        }

        if (hasCorrectShadow(exprBlock)) {
            return parseLiteralStringInput(exprBlock);
        }

        if (
                exprBlock.input() instanceof BlockRef.IdRef exprInput
                        && state.getBlock(exprInput.id()) instanceof RawBlock.RawRegularBlock exprInputRegularBlock
        ) {
            return convertStringExpr(state, exprInput.id(), exprInputRegularBlock);
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
        final boolean parseableAsStringLiteral = isParseableAsStringLiteral(exprBlock);
        final boolean hasStringExprOpcode = hasStringExprOpcode(target, exprBlock);

        return parseableAsStringLiteral || hasStringExprOpcode;
    }

    private static boolean isParseableAsStringLiteral(final RawInput exprBlock) {
        final boolean hasCorrectType = exprBlock.input() instanceof BlockRef.Block exprInput
                && (
                        // Raw color literals in string positions can only be achieved by JSON hacking.
                        // Since the JSON contains the hex-string of the color, though, we can parse it as string.
                        //
                        // Broadcasts behave like String messages.
                        exprInput.block() instanceof RawBlock.RawStringLiteral
                                || exprInput.block() instanceof RawBlock.RawColorLiteral
                                || exprInput.block() instanceof RawBlock.RawBroadcast
                );

        return hasCorrectShadow(exprBlock) && hasCorrectType;
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
            } else if (literalInput instanceof RawBlock.RawColorLiteral c) {
                return new StringLiteral(c.color());
            }
        }

        return new UnspecifiedStringExpr();
    }

    static StringExpr convertStringExpr(
            final ProgramParserState state,
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
            case translate_getTranslate -> {
                final TLanguage language = getLanguage(state, block);
                final StringExpr text = StringExprConverter.convertStringExpr(state, block, KnownInputs.WORDS);
                yield new TranslateTo(text, language, metadata);
            }
            case operator_join -> {
                final StringExpr left = convertStringExpr(state, block, KnownInputs.STRING1);
                final StringExpr right = convertStringExpr(state, block, KnownInputs.STRING2);
                yield new Join(left, right, metadata);
            }
            case operator_letter_of -> {
                final NumExpr num = NumExprConverter.convertNumExpr(state, block, KnownInputs.LETTER);
                final StringExpr word = convertStringExpr(state, block, KnownInputs.STRING);
                yield new LetterOf(num, word, metadata);
            }
            case looks_costumenumbername -> {
                final String name = block.getFieldValueAsString(KnownFields.NUMBER_NAME);
                yield new Costume(new NameNum(name), metadata);
            }
            case looks_backdropnumbername -> {
                final String name = block.getFieldValueAsString(KnownFields.NUMBER_NAME);
                yield new Backdrop(new NameNum(name), metadata);
            }
            case data_itemoflist -> parseItemOfList(state, metadata, block);
            case sensing_of -> parseSensingOf(state, metadata, block);
        };
    }

    private static ItemOfVariable parseItemOfList(
            final ProgramParserState state,
            final BlockMetadata metadata,
            final RawBlock.RawRegularBlock exprBlock
    ) {
        final NumExpr index = NumExprConverter.convertNumExpr(state, exprBlock, KnownInputs.INDEX);
        final RawBlockId listId = exprBlock.getField(KnownFields.LIST).id()
                .orElseThrow(() -> new InternalParsingException("ItemOfVariable block is missing reference to list!"));
        final String listName = exprBlock.getFieldValueAsString(KnownFields.LIST);
        final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                listId.id(), listName, state.getCurrentActor().getName(),
                () -> new ExpressionList(Collections.emptyList()), true, "Stage"
        );

        final Identifier variable = new Qualified(
                new StrId(listInfo.getActor()),
                new ScratchList(new StrId(listInfo.getVariableName()))
        );

        return new ItemOfVariable(index, variable, metadata);
    }

    private static AttributeOf parseSensingOf(
            final ProgramParserState state,
            final BlockMetadata metadata,
            final RawBlock.RawRegularBlock exprBlock
    ) {
        final String property = exprBlock.getFieldValueAsString(KnownFields.PROPERTY);
        final Attribute attribute = switch (property) {
            case "y position", "x position", "direction", "costume #", "costume name", "size", "volume",
                 "backdrop name", "backdrop #" -> new AttributeFromFixed(new FixedAttribute(property));
            default -> new AttributeFromVariable(new Variable(new StrId(property)));
        };

        final ElementChoice elementChoice;
        final RawInput input = exprBlock.getInput(KnownInputs.OBJECT);

        if (ShadowType.SHADOW.equals(input.shadowType())) {
            if (input.input() instanceof BlockRef.IdRef menuBlockRef) {
                elementChoice = convertSensingOfMenu(state, menuBlockRef.id());
            } else {
                throw new InternalParsingException("Expected a menu item for sensing block.");
            }
        } else {
            final Expression expr = ExprConverter.convertExpr(state, exprBlock, input);
            elementChoice = new WithExpr(expr, new NoBlockMetadata());
        }

        return new AttributeOf(attribute, elementChoice, metadata);
    }

    private static ElementChoice convertSensingOfMenu(
            final ProgramParserState state, final RawBlockId menuId
    ) {
        final RawBlock menuBlock = state.getCurrentTarget().blocks().get(menuId);
        final BlockMetadata menuMetadata = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);

        if (menuBlock instanceof RawBlock.RawRegularBlock menuRegularBlock) {
            if (DependentBlockOpcode.sensing_of_object_menu.getName().equalsIgnoreCase(menuRegularBlock.opcode())) {
                final RawField field = menuRegularBlock.getField(KnownFields.OBJECT);
                if (field.value() == null) {
                    throw new InternalParsingException(
                            "Invalid project file. Menu for sensing_of block has no value."
                    );
                }

                final String actorName = field.value().toString();
                return new WithExpr(new StrId(actorName), menuMetadata);
            } else {
                final Expression expr = ExprConverter.convertExpr(state, menuRegularBlock, KnownInputs.OBJECT);
                return new WithExpr(expr, menuMetadata);
            }
        } else {
            throw new InternalParsingException("Malformed menu for sensing block.");
        }
    }

    private static TLanguage getLanguage(final ProgramParserState state, final RawBlock.RawRegularBlock block) {
        final RawInput languageInput = block.getInput(KnownInputs.LANGUAGE);

        if (
                ShadowType.SHADOW.equals(languageInput.shadowType())
                && languageInput.input() instanceof BlockRef.IdRef blockIdRef
                && state.getBlock(blockIdRef.id()) instanceof RawBlock.RawRegularBlock languageMenuBlock
                && DependentBlockOpcode.translate_menu_languages.getName().equals(languageMenuBlock.opcode())
        ) {
            final String languageName = languageMenuBlock.getFieldValueAsString(KnownFields.LANGUAGES);
            final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(
                    blockIdRef.id(), languageMenuBlock
            );

            return new TFixedLanguage(languageName, metadata);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, languageInput);
            return new TExprLanguage(expr, new NoBlockMetadata());
        }
    }
}
