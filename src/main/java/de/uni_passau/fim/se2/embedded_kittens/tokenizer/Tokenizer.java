/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import de.uni_passau.fim.se2.embedded_kittens.shared.BaseTokenVisitor;
import de.uni_passau.fim.se2.embedded_kittens.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.embedded_kittens.util.AbstractToken;
import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.AsExprType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.FixedColorParam;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ForwardBackwardChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

import java.util.List;

public class Tokenizer extends AbstractTokenizer {

    /**
     * Add parentheses around literals and variable names when they occur inside other expressions.
     *
     * <h2>Example when {@code true}</h2>
     * <ul>
     * <li>{@code movesteps (2)}, but no additional parentheses when inside expression: {@code movesteps (add 2 5)}</li>
     * <li>{@code movesteps (my_variable)} but no additional parentheses when inside expression:
     * {@code movesteps (div my_variable 3)}</li>
     * </ul>
     *
     * <h2>Example when {@code false}</h2>
     * <ul>
     * <li>{@code movesteps (2)} and {@code movesteps (add (2) (5))}</li>
     * <li>{@code movesteps (my_variable)} and {@code movesteps (div (my_variable) (3))}</li>
     * </ul>
     */
    protected final boolean parenthesiseExpressions;

    private final BaseTokenVisitor tokenVisitor = TokenVisitorFactory.getDefaultTokenVisitor(false);
    private final boolean abstractFixedNodeOptions;

    protected Tokenizer(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final boolean abstractTokens,
            final boolean abstractFixedNodeOptions,
            final boolean parenthesiseExpressions,
            final MaskingStrategy maskingStrategy
    ) {
        super(procedureNameMapping, abstractTokens, maskingStrategy);
        this.abstractFixedNodeOptions = abstractFixedNodeOptions;
        this.parenthesiseExpressions = parenthesiseExpressions;
    }

    public static List<String> tokenize(
            final Program program, final ASTNode node, final boolean abstractTokens,
            final boolean abstractFixedNodeOptions, final MaskingStrategy maskingStrategy
    ) {
        return tokenize(
            program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions, true, maskingStrategy
        );
    }

    /**
     * Does not add parentheses around literals and variable names that occur inside other expressions.
     *
     * <p>
     * For example, in {@code move_steps (2)} the parentheses remain since it is the outermost expression of the
     * argument to the {@code move_steps} block. However, {@code move steps (3 + my_variable)} will be tokenized like
     * {@code move_steps (add 3 5)} without the additional parentheses around {@code 3} and {@code my_variable}.
     *
     * @param program                  Some Scratch program.
     * @param node                     The node inside the program that should be tokenized.
     * @param abstractTokens           True, if literals and names should be replaced by fixed abstract tokens.
     * @param abstractFixedNodeOptions True, if fixed node options should be replaced by a fixed abstract token.
     * @param maskingStrategy          The masking strategy that should be applied during tokenization.
     * @return A sequence of tokens obtained when traversing the AST of {@code node}.
     */
    public static List<String> tokenizeWithReducedParentheses(
            final Program program, final ASTNode node, final boolean abstractTokens,
            final boolean abstractFixedNodeOptions, final MaskingStrategy maskingStrategy
    ) {
        return tokenize(
            program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions, false, maskingStrategy
        );
    }

    private static List<String> tokenize(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final ASTNode node,
            final boolean abstractTokens,
            final boolean abstractFixedNodeOptions,
            final boolean parenthesiseAtoms,
            final MaskingStrategy maskingStrategy
    ) {
        final Tokenizer v = new Tokenizer(
                procedureNameMapping, abstractTokens, abstractFixedNodeOptions, parenthesiseAtoms, maskingStrategy
        );
        node.accept(v);
        return v.getTokens();
    }

    @Override
    protected void visit(final ASTNode node, final Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
            visitChildren(node);
        }
    }

    protected boolean shouldBeMasked(final ASTNode node) {
        return getMaskingStrategy().shouldBeMasked(node);
    }

    private void visit(final ASTNode node, final String token) {
        addToken(token);
        visitChildren(node);
    }

    @Override
    public void visitChildren(ASTNode node) {
        for (final ASTNode child : node.getChildren()) {
            if (AstNodeUtil.isMetadata(child)) {
                continue;
            }

            visitChild(child);
        }
    }

    protected void visitChild(final ASTNode child) {
        if (child instanceof Expression childExpr) {
            visitExpression(childExpr);
        } else {
            child.accept(this);
        }
    }

    // region motion

    @Override
    public void visit(PositionX node) {
        visit(node, Token.MOTION_XPOSITION);
    }

    @Override
    public void visit(PositionY node) {
        visit(node, Token.MOTION_YPOSITION);
    }

    @Override
    public void visit(Direction node) {
        visit(node, Token.MOTION_DIRECTION);
    }

    @Override
    public void visit(RandomPos node) {
        visit(node, Token.MOTION_RANDOMPOS);
    }

    @Override
    public void visit(MousePos node) {
        visit(node, Token.MOTION_MOUSEPOS);
    }

    @Override
    public void visit(MousePointer node) {
        visit(node, Token.MOTION_MOUSEPOINTER);
    }

    @Override
    public void visit(RotationStyle node) {
        visitFixedNodeOption(node, Token.MOTION_ROTATIONSTYLE);
    }

    @Override
    public void visit(DragMode node) {
        visitFixedNodeOption(node, Token.MOTION_DRAGMODE);
    }

    // endregion motion

    // region looks

    @Override
    public void visit(Costume node) {
        visit(node, Token.LOOKS_COSTUMENUMBERNAME);
    }

    @Override
    public void visit(Backdrop node) {
        visit(node, Token.LOOKS_BACKDROP);
    }

    @Override
    public void visit(Size node) {
        visit(node, Token.LOOKS_SIZE);
    }

    @Override
    public void visit(GraphicEffect node) {
        visitFixedNodeOption(node, Token.LOOKS_GRAPHICEFFECT);
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_FORWARDBACKWARD);
    }

    @Override
    public void visit(LayerChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_LAYERCHOICE);
    }

    @Override
    public void visit(Next node) {
        visit(node, Token.LOOKS_NEXTBACKDROPCHOICE);
    }

    @Override
    public void visit(Prev node) {
        visit(node, Token.LOOKS_PREVBACKDROPCHOICE);
    }

    @Override
    public void visit(Random node) {
        visit(node, Token.LOOKS_RANDOMBACKDROPCHOICE);
    }

    // endregion looks

    // region sound

    @Override
    public void visit(Volume node) {
        visit(node, Token.SOUND_VOLUME);
    }

    @Override
    public void visit(SoundEffect node) {
        visitFixedNodeOption(node, Token.SOUND_EFFECT);
    }

    // endregion sound

    // region events

    @Override
    public void visit(EventAttribute node) {
        visitFixedNodeOption(node, Token.EVENT_ATTRIBUTE);
    }

    @Override
    public void visit(Message node) {
        visit(node, Token.EVENT_MESSAGE);
    }

    // endregion events

    // region control

    @Override
    public void visit(RepeatTimesStmt node) {
        visitControlBlockHead(node, Token.CONTROL_REPEAT, node.getTimes());
        visitControlBlockBody(node.getStmtList());
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        } else {
            addToken(Token.CONTROL_FOREVER);
        }
        visitControlBlockBody(node.getStmtList());
    }

    @Override
    public void visit(IfThenStmt node) {
        visitControlBlockHead(node, Token.CONTROL_IF, node.getBoolExpr());
        visitControlBlockBody(node.getThenStmts());
    }

    @Override
    public void visit(IfElseStmt node) {
        visitControlBlockHead(node, Token.CONTROL_IF_ELSE, node.getBoolExpr());
        visitControlBlockBody(node.getThenStmts());
        addToken(Token.ELSE);
        visitControlBlockBody(node.getElseStmts());
    }

    @Override
    public void visit(UntilStmt node) {
        visitControlBlockHead(node, Token.CONTROL_REPEAT_UNTIL, node.getBoolExpr());
        visitControlBlockBody(node.getStmtList());
    }

    private void visitControlBlockHead(final Stmt stmt, final Token opcode, final Expression condition) {
        if (shouldBeMasked(stmt)) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
            visitExpression(condition);
        }
    }

    private void visitControlBlockBody(final StmtList stmtList) {
        visitSurrounded(Token.BEGIN_SUBSTACK, stmtList, Token.END_SUBSTACK);
    }

    @Override
    public void visit(StopAll node) {
        visitStopBlock(node, "all");
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStopBlock(node, "other_scripts");
    }

    @Override
    public void visit(StopThisScript node) {
        visitStopBlock(node, "this_script");
    }

    /**
     * Handles stop blocks and their fixed node options. In contrast to all other blocks with a rectangular dropdown
     * menu, stop blocks are not represented in the LitterBox AST by a single {@code ASTNode} with a {@code
     * FixedNodeOption} child. Instead, we have three different nodes ({@code StopAll},
     * {@code StopOtherScriptsInSprite}, {@code StopThisScript}) for every possible entry in the dropdown menu, without
     * a {@code FixedNodeOption} child.
     *
     * @param stopBlock The stop block to visit.
     * @param target    The target selected in the dropdown menu.
     */
    private void visitStopBlock(final ASTNode stopBlock, final String target) {
        final boolean shouldBeMasked = shouldBeMasked(stopBlock);

        // Visit the stop block.

        if (shouldBeMasked && getMaskingStrategy() instanceof MaskingStrategy.Block) {
            addToken(Token.MASK);
            return;
        }

        addToken(Token.CONTROL_STOP);

        // Visit its "FixedNodeOption". Basically the same code as in visitFixedNodeOption().

        if (shouldBeMasked && getMaskingStrategy() instanceof MaskingStrategy.FixedOption) {
            addToken(Token.MASK);
        } else if (abstractFixedNodeOptions) {
            addToken("stop_target");
        } else {
            addToken(target);
        }
    }

    // endregion control

    // region sensing

    @Override
    public void visit(Touching node) {
        visit(node, Token.SENSING_TOUCHINGOBJECT);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        visit(node, Token.SENSING_TOUCHINGCOLOR);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        visit(node, Token.SENSING_COLORISTOUCHINGCOLOR);
    }

    @Override
    public void visit(DistanceTo node) {
        visit(node, Token.SENSING_DISTANCETO);
    }

    @Override
    public void visit(Answer node) {
        visit(node, Token.SENSING_ANSWER);
    }

    @Override
    public void visit(IsKeyPressed node) {
        visit(node, Token.SENSING_KEYPRESSED);
    }

    @Override
    public void visit(IsMouseDown node) {
        visit(node, Token.SENSING_MOUSEDOWN);
    }

    @Override
    public void visit(MouseX node) {
        visit(node, Token.SENSING_MOUSEX);
    }

    @Override
    public void visit(MouseY node) {
        visit(node, Token.SENSING_MOUSEY);
    }

    @Override
    public void visit(Loudness node) {
        visit(node, Token.SENSING_LOUDNESS);
    }

    @Override
    public void visit(Timer node) {
        visit(node, Token.SENSING_TIMER);
    }

    @Override
    public void visit(AttributeOf node) {
        visit(node, Token.SENSING_OF);
    }

    @Override
    public void visit(Current node) {
        visit(node, Token.SENSING_CURRENT);
    }

    @Override
    public void visit(DaysSince2000 node) {
        visit(node, Token.SENSING_DAYSSINCE2000);
    }

    @Override
    public void visit(Username node) {
        visit(node, Token.SENSING_USERNAME);
    }

    @Override
    public void visit(TimeComp node) {
        visitFixedNodeOption(node, Token.TIME_COMP);
    }

    @Override
    public void visit(Key node) {
        if (abstractFixedNodeOptions) {
            addToken(Token.KEY);
            addToken(Token.BEGIN_NUM_STR_EXPR);
            addToken("keyid");
            addToken(Token.END_NUM_STR_EXPR);
        } else {
            visit(node, Token.KEY);
        }
    }

    @Override
    public void visit(Edge node) {
        visit(node, Token.SENSING_EDGE);
    }

    // endregion sensing

    // region operators

    protected void visitBinaryOperator(final BinaryExpression<?, ?> node, final Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        } else {
            visitChild(node.getOperand1());
            addToken(opcode);
            visitChild(node.getOperand2());
        }
    }

    @Override
    public void visit(Add node) {
        visitBinaryOperator(node, Token.OPERATOR_ADD);
    }

    @Override
    public void visit(Minus node) {
        visitBinaryOperator(node, Token.OPERATOR_SUBTRACT);
    }

    @Override
    public void visit(Mult node) {
        visitBinaryOperator(node, Token.OPERATOR_MULTIPLY);
    }

    @Override
    public void visit(Div node) {
        visitBinaryOperator(node, Token.OPERATOR_DIVIDE);
    }

    @Override
    public void visit(PickRandom node) {
        visit(node, Token.OPERATOR_RANDOM);
    }

    @Override
    public void visit(BiggerThan node) {
        visitBinaryOperator(node, Token.OPERATOR_GT);
    }

    @Override
    public void visit(LessThan node) {
        visitBinaryOperator(node, Token.OPERATOR_LT);
    }

    @Override
    public void visit(Equals node) {
        visitBinaryOperator(node, Token.OPERATOR_EQUALS);
    }

    @Override
    public void visit(And node) {
        visitBinaryOperator(node, Token.OPERATOR_AND);
    }

    @Override
    public void visit(Or node) {
        visitBinaryOperator(node, Token.OPERATOR_OR);
    }

    @Override
    public void visit(Not node) {
        visit(node, Token.OPERATOR_NOT);
    }

    @Override
    public void visit(Join node) {
        visit(node, Token.OPERATOR_JOIN);
    }

    @Override
    public void visit(LetterOf node) {
        visit(node, Token.OPERATOR_LETTER_OF);
    }

    @Override
    public void visit(LengthOfString node) {
        visit(node, Token.OPERATOR_LENGTH);
    }

    @Override
    public void visit(StringContains node) {
        visit(node, Token.OPERATOR_CONTAINS);
    }

    @Override
    public void visit(Mod node) {
        visitBinaryOperator(node, Token.OPERATOR_MOD);
    }

    @Override
    public void visit(Round node) {
        visit(node, Token.OPERATOR_ROUND);
    }

    @Override
    public void visit(NumFunctOf node) {
        visit(node, Token.OPERATOR_MATHOP);
    }

    @Override
    public void visit(NumFunct node) {
        visitFixedNodeOption(node, Token.NUM_FUNCT);
    }

    // endregion operators

    // region variables

    @Override
    public void visit(ItemOfVariable node) {
        visit(node, Token.DATA_ITEMOFLIST);
    }

    @Override
    public void visit(IndexOf node) {
        visit(node, Token.DATA_ITEMNUMOFLIST);
    }

    @Override
    public void visit(LengthOfVar node) {
        visit(node, Token.DATA_LENGTHOFLIST);
    }

    @Override
    public void visit(ListContains node) {
        visit(node, Token.DATA_LISTCONTAINSITEM);
    }

    // endregion variables

    // region expressions

    protected void visitExpression(final Expression expr) {
        if (!parenthesiseExpressions && isLiteralInsideExpression(expr)) {
            expr.accept(this);
        } else if (expr instanceof AsExprType asExprType) {
            visitExpressionTypeWrapper(asExprType);
        } else if (expr instanceof BoolExpr) {
            visitBoolExpr(expr);
        } else {
            visitNumOrStringExpr(expr);
        }
    }

    private boolean isLiteralInsideExpression(final Expression expr) {
        if (!(expr.getParentNode() instanceof Expression)) {
            return false;
        }

        if (expr instanceof AsExprType asExprType) {
            return isLiteralInsideExpression(asExprType.getOperand1());
        }

        return expr instanceof NumberLiteral
                || expr instanceof ColorLiteral
                || expr instanceof StringLiteral
                || expr instanceof BoolLiteral
                || expr instanceof Qualified
                || expr instanceof Parameter;
    }

    private void visitExpressionTypeWrapper(final AsExprType expr) {
        if (expr instanceof AsBool) {
            visitBoolExpr(expr.getOperand1());
        } else {
            visitNumOrStringExpr(expr.getOperand1());
        }
    }

    protected void visitBoolExpr(final Expression expr) {
        visitSurrounded(Token.BEGIN_BOOL_EXPR, expr, Token.END_BOOL_EXPR);
    }

    protected void visitNumOrStringExpr(final Expression expr) {
        visitSurrounded(Token.BEGIN_NUM_STR_EXPR, expr, Token.END_NUM_STR_EXPR);
    }

    @Override
    public void visit(NumberLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_NUMBER, getNormalisedToken(node));
    }

    @Override
    public void visit(StringLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_STRING, getNormalisedToken(node));
    }

    @Override
    public void visit(BoolLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_BOOL, getNormalisedToken(node));
    }

    @Override
    public void visit(ColorLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_COLOR, getNormalisedToken(node));
    }

    @Override
    public void visit(UnspecifiedExpression node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(NameNum node) {
        visitFixedNodeOption(node, Token.LOOKS_BACKDROPNUMBERNAME);
    }

    @Override
    public void visit(FixedAttribute node) {
        visitFixedNodeOption(node, Token.ATTRIBUTE);
    }

    @Override
    public void visit(StrId node) {
        ifAbstractElse(AbstractToken.NODE_IDENTIFIER, getNormalisedToken(node));
    }

    @Override
    public void visit(Parameter node) {
        if (isAbstractTokens()) {
            addToken(AbstractToken.PARAMETER);
        } else {
            node.getName().accept(this);
        }
    }

    @Override
    public void visit(Variable node) {
        final String name = node.getName().getName();
        ifAbstractElse(AbstractToken.VAR, name);
    }

    @Override
    public void visit(Qualified node) {
        // we don't care about the scope of variables
        node.getSecond().accept(this);
    }

    @Override
    public void visit(ScratchList node) {
        final String name = node.getName().getName();
        ifAbstractElse(AbstractToken.LIST, name);
    }

    // endregion expressions

    // region pen

    @Override
    public void visit(FixedColorParam node) {
        visitFixedNodeOption(node, Token.PEN_PARAM);
    }

    // endregion pen

    // region tts

    @Override
    public void visit(Language node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(FixedLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(ExprLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(Voice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(FixedVoice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(ExprVoice node) {
        visit(node, Token.TTS_VOICE);
    }

    // endregion tts

    // region music

    @Override
    public void visit(Tempo node) {
        visit(node, Token.MUSIC_TEMPO);
    }

    @Override
    public void visit(FixedNote node) {
        ifAbstractElse(Token.MUSIC_LITERAL_NOTE, getNormalisedToken(node));
    }

    @Override
    public void visit(FixedInstrument node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_INSTRUMENT);
    }

    @Override
    public void visit(FixedDrum node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_DRUM);
    }

    // endregion music

    // region helper methods

    private void visitSurrounded(final Token left, final ASTNode node, final Token right) {
        addToken(left);
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        } else {
            node.accept(this);
        }
        addToken(right);
    }

    private void visitFixedNodeOption(final FixedNodeOption option, final Token opcode) {
        if (shouldBeMasked(option)) {
            addToken(Token.MASK);
        } else {
            if (abstractFixedNodeOptions) {
                visit(option, opcode);
            } else {
                visit(option, getNormalisedToken(option));
            }
        }
    }

    private void ifAbstractElse(final Token opcode, final String token) {
        if (isAbstractTokens()) {
            addToken(opcode);
        } else {
            addToken(token);
        }
    }

    private void ifAbstractElse(final AbstractToken abstractToken, final String token) {
        if (isAbstractTokens()) {
            addToken(abstractToken);
        } else {
            addToken(token);
        }
    }

    private String getNormalisedToken(final ASTNode node) {
        return TokenVisitorFactory.getToken(tokenVisitor, node);
    }

    // endregion helper methods
}
