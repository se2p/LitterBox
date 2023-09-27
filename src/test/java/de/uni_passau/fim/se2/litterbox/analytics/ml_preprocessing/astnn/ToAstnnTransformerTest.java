/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.*;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.IfOnEdgeBounce;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ToAstnnTransformerTest implements JsonTest {
    private static Program dummyProgram;

    @BeforeAll
    static void setup() throws Exception {
        dummyProgram = JsonTest.parseProgram("src/test/fixtures/emptyProject.json");
    }

    @Test
    void testTransformAdd() {
        final NumberLiteral n1 = new NumberLiteral(1D);
        final NumberLiteral n2 = new NumberLiteral(2D);
        final Add add1 = new Add(n1, n2, new NoBlockMetadata());
        final NumberLiteral n3 = new NumberLiteral(3D);
        final Add add2 = new Add(add1, n3, new NoBlockMetadata());

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, add2);

        assertAll(
                () -> assertThat(node.children()).hasSize(2),
                () -> assertThat(node.children().get(0).children()).hasSize(2),
                () -> assertThat(node.children().get(1).isLeaf()).isTrue()
        );
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformColorLiteral(boolean abstractTokens) {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final ColorLiteral color = new ColorLiteral(189, 56, 246);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, color);

        final String expectedColor = abstractTokens ? AbstractToken.LITERAL_COLOR.name() : "#bd38f6";
        assertThat(node.label()).isEqualTo(expectedColor);
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformStringLiteral(boolean abstractTokens) {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final StringLiteral literal = new StringLiteral("some string");
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, literal);

        final String expectedLabel = abstractTokens ? AbstractToken.LITERAL_STRING.name() : "some_string";
        assertThat(node.label()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @ValueSource(strings = {"some string", "some  string", "Some String", "SOME STRING", "some_string", "some\nstring"})
    void testNormaliseStringLiteral(final String literal) {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final StringLiteral literalNode = new StringLiteral(literal);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, literalNode);
        assertThat(node.label()).isEqualTo("some_string");
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformNumberLiteral(boolean abstractTokens) {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final NumberLiteral literal = new NumberLiteral(13D);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, literal);

        final String expectedLabel = abstractTokens ? AbstractToken.LITERAL_NUMBER.name() : "13";
        assertThat(node.label()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformBoolLiteral(boolean abstractTokens) {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final BoolLiteral literal = new BoolLiteral(true);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, literal);

        final String expectedLabel = abstractTokens ? AbstractToken.LITERAL_BOOL.name() : "true";
        assertThat(node.label()).isEqualTo(expectedLabel);
    }

    @Test
    void testTransformUntilLoopEmptyCondition() {
        final var condition = new UnspecifiedBoolExpr();
        final var stmt = new IfOnEdgeBounce(new NoBlockMetadata());
        final var untilStmt = new UntilStmt(condition, new StmtList(stmt), new NoBlockMetadata());

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, untilStmt);

        assertAll(
                () -> assertThat(node.isStatement()).isTrue(),
                () -> assertThat(node.label()).isEqualTo(StatementType.CONTROL_REPEAT_UNTIL.toString()),
                () -> assertThat(node.children()).hasSize(2),
                () -> assertThat(node.children().get(1).label()).isEqualTo("BLOCK")
        );

        final List<AstnnNode> children = node.children();
        assertAll(
                () -> assertThat(children.get(0)).isInstanceOf(AstnnAstLeaf.class),
                () -> assertThat(children.get(0).label()).isEqualTo(NodeType.EMPTY_BOOL.toString())
        );

        final List<AstnnNode> statements = node.children().get(1).children();
        assertAll(
                () -> assertThat(statements).hasSize(1),
                () -> assertThat(statements.get(0)).isInstanceOf(AstnnAstLeaf.class),
                () -> assertThat(statements.get(0).label()).isEqualTo(StatementType.MOTION_IFONEDGEBOUNCE.toString())
        );
    }

    @Test
    void testTransformIfElse() {
        final var condition = new BoolLiteral(true);
        final var thenStmt = new StmtList(new Show(new NoBlockMetadata()));
        final var elseStmt = new StmtList(new Hide(new NoBlockMetadata()));
        final var ifElseStmt = new IfElseStmt(condition, thenStmt, elseStmt, new NoBlockMetadata());

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final AstnnNode node = toAstnnTransformer.transform(dummyProgram, ifElseStmt);
        assertThat(node.label()).isEqualTo("if");
        assertChildLabels(node, StatementType.CONTROL_IF.name(), StatementType.CONTROL_ELSE.name());

        final AstnnNode conditionNode = walkTree(node, 0, 0);
        assertThat(conditionNode.label()).isEqualTo("true");

        // node -> IF -> BLOCK -> stmt
        final AstnnNode thenStmtNode = walkTree(node, 0, 1, 0);
        assertThat(thenStmtNode.getStatementType()).isEqualTo(StatementType.LOOKS_SHOW);

        // node -> ELSE -> BLOCK -> stmt
        final AstnnNode elseStmtNode = walkTree(node, 1, 0, 0);
        assertThat(elseStmtNode.getStatementType()).isEqualTo(StatementType.LOOKS_HIDE);
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformCustomBlock(boolean abstractTokens) throws Exception {
        final Program program = getProgram("custom_block.json");
        final var customBlock = program.getActorDefinitionList()
                .getDefinitions()
                .get(0)
                .getProcedureDefinitionList()
                .getList()
                .get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final AstnnNode customBlockNode = toAstnnTransformer.transform(program, customBlock);
        assertThat(customBlockNode.getStatementType()).isEqualTo(StatementType.PROCEDURES_DEFINITION);
        final String blockName = abstractTokens ? "procedure_definition" : "astnn_custom_block_label_text";
        assertThat(customBlockNode.label()).isEqualTo(blockName);
        assertChildLabels(customBlockNode, "parameters", AstnnNode.BLOCK_LABEL);

        final List<AstnnNode> parameters = walkTree(customBlockNode, 0).children();
        assertThat(parameters).hasSize(2);

        final String[] expectedParameterNames;
        if (abstractTokens) {
            expectedParameterNames= new String[] { "parameter", "parameter" };
        } else {
            expectedParameterNames = new String[] { "some_param", "other_param" };
        }

        assertChildLabels(parameters.get(0), "string", expectedParameterNames[0]);
        assertChildLabels(parameters.get(1), "boolean", expectedParameterNames[1]);

        final List<AstnnNode> statements = walkTree(customBlockNode, 1).children();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatementType()).isEqualTo(StatementType.SOUND_STOPALLSOUNDS);
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformCustomBlockCall(boolean abstractTokens) throws Exception {
        final Program program = getProgram("custom_block.json");
        final CallStmt customBlockCall = NodeFilteringVisitor.getBlocks(program, CallStmt.class).get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final AstnnNode customBlockNode = toAstnnTransformer.transform(program, customBlockCall);
        assertThat(customBlockNode.getStatementType()).isEqualTo(StatementType.PROCEDURES_CALL);

        final String blockLabel = abstractTokens ? "custom_block" : "astnn_custom_block_label_text";
        final String stringParam = abstractTokens ? AbstractToken.LITERAL_STRING.name() : "EMPTY_STRING";
        assertChildLabels(
                customBlockNode,
                blockLabel,
                stringParam,
                NodeType.EMPTY_BOOL.name()
        );
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testProgramSpriteLabels(boolean abstractTokens) throws Exception {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final Program program = getAST("src/test/fixtures/multipleSprites.json");
        final AstnnNode node = toAstnnTransformer.transform(program, true, true);

        if (abstractTokens) {
            assertChildLabels(node, "stage", "sprite", "sprite");
        } else {
            assertChildLabels(node, "stage", "cat", "abby");
        }
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformMessage(boolean abstractTokens) throws Exception {
        final Program program = getProgram("messages.json");
        final Script script = NodeFilteringVisitor.getBlocks(program, Script.class).get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final AstnnNode node = toAstnnTransformer.transform(program, script);
        assertChildLabels(node, StatementType.EVENT_WHENBROADCASTRECEIVED.name(), AstnnNode.BLOCK_LABEL);

        final AstnnNode receivedMessage = walkTree(node, 0, 0);
        assertThat(receivedMessage.label()).isEqualTo(NodeType.EVENT_MESSAGE.name());
        final String message = abstractTokens ? AbstractToken.LITERAL_STRING.name() : "message_1";
        assertThat(receivedMessage.children().get(0).label()).isEqualTo(message);

        {
            final AstnnNode broadcastBlock = walkTree(node, 1, 0);
            assertThat(broadcastBlock.getStatementType()).isEqualTo(StatementType.EVENT_BROADCAST);
            final AstnnNode sentMessage1 = walkTree(broadcastBlock, 0, 0);
            final String msg = abstractTokens ? AbstractToken.LITERAL_STRING.name() : "message_1";
            assertThat(sentMessage1.label()).isEqualTo(msg);
        }

        {
            final AstnnNode broadcastAndWaitBlock = walkTree(node, 1, 1);
            assertThat(broadcastAndWaitBlock.getStatementType()).isEqualTo(StatementType.EVENT_BROADCASTANDWAIT);
            final AstnnNode sentMessage2 = walkTree(broadcastAndWaitBlock, 0, 0);
            final String msg = abstractTokens ? AbstractToken.LITERAL_STRING.name() : "message_with_spaces";
            assertThat(sentMessage2.label()).isEqualTo(msg);
        }
    }

    @Test
    void testTransformRepeatTimes() throws Exception {
        final Program program = getProgram("control_blocks.json");
        final RepeatTimesStmt repeatTimesStmt = NodeFilteringVisitor.getBlocks(program, RepeatTimesStmt.class).get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final AstnnNode node = toAstnnTransformer.transform(program, repeatTimesStmt);
        assertThat(node.getStatementType()).isEqualTo(StatementType.CONTROL_REPEAT_TIMES);
        assertChildLabels(node, NodeType.SENSING_CURRENT.name(), AstnnNode.BLOCK_LABEL);

        assertChildLabels(walkTree(node, 0), "year");

        {
            final AstnnNode setDragMode = walkTree(node, 1, 0);
            assertThat(setDragMode.getStatementType()).isEqualTo(StatementType.SENSING_SETDRAGMODE);
            assertChildLabels(setDragMode, "draggable");
        }

        {
            final AstnnNode motionGoTo = walkTree(node, 1, 1);
            assertThat(motionGoTo.getStatementType()).isEqualTo(StatementType.MOTION_GOTO);
            assertChildLabels(motionGoTo, NodeType.MOTION_RANDOMPOSITION.name());
        }

        {
            final AstnnNode resetTimer = walkTree(node, 1, 2);
            assertThat(resetTimer.getStatementType()).isEqualTo(StatementType.SENSING_RESETTIMER);
            assertThat(resetTimer.isLeaf()).isTrue();
        }
    }

    @Test
    void testTransformIf() throws Exception {
        final Program program = getProgram("control_blocks.json");
        final IfThenStmt ifThenStmt = NodeFilteringVisitor.getBlocks(program, IfThenStmt.class).get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);
        final AstnnNode node = toAstnnTransformer.transform(program, ifThenStmt);
        assertThat(node.label()).isEqualTo("if");
        assertChildLabels(node, StatementType.CONTROL_IF.name());

        final AstnnNode ifNode = walkTree(node, 0);
        assertThat(ifNode.getStatementType()).isEqualTo(StatementType.CONTROL_IF);
        assertChildLabels(ifNode, NodeType.SENSING_KEYPRESSED.name(), AstnnNode.BLOCK_LABEL);

        {
            final AstnnNode keyPressed = walkTree(ifNode, 0);
            assertChildLabels(keyPressed, NodeType.SENSING_KEY.name());
        }

        {
            final AstnnNode askAndWait = walkTree(ifNode, 1, 0);
            assertThat(askAndWait.getStatementType()).isEqualTo(StatementType.SENSING_ASKANDWAIT);
            assertChildLabels(askAndWait, NodeType.SENSING_LOUDNESS.name());
        }
    }

    @ParameterizedTest(name = "{displayName} abstractTokens={0}")
    @ValueSource(booleans = {true, false})
    void testTransformCreateCloneVariable(boolean abstractTokens) throws Exception {
        final Program program = getProgram("control_blocks.json");
        final CreateCloneOf createCloneOf = NodeFilteringVisitor.getBlocks(program, CreateCloneOf.class).get(0);

        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(
                ActorNameNormalizer.getDefault(), abstractTokens
        );
        final AstnnNode node = toAstnnTransformer.transform(program, createCloneOf);
        assertThat(node.getStatementType()).isEqualTo(StatementType.CONTROL_CREATE_CLONE_OF);
        assertChildLabels(node, NodeType.DATA_VARIABLE.name());

        final AstnnNode variable = walkTree(node, 0);

        final String variableName = abstractTokens ? AbstractToken.VAR.name() : "my_variable";
        assertChildLabels(variable, variableName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/translate_blocks.json"
    })
    void testAllBlocksVisitable(final String filename) throws Exception {
        final ToAstnnTransformer toAstnnTransformer = new ToAstnnTransformer(ActorNameNormalizer.getDefault(), false);

        final Program program = getAST(filename);
        final AstnnNode node = toAstnnTransformer.transform(program, true, true);
        assertThat(node).isNotNull();
        assertThat(node).isInstanceOf(AstnnAstNode.class);
        assertNoUnknownNode(node);
    }

    private void assertNoUnknownNode(final AstnnNode node) {
        assertThat(node.label().toLowerCase(Locale.ROOT))
                .isNotEqualTo(NodeType.UNKNOWN.name().toLowerCase(Locale.ROOT));

        node.children().forEach(this::assertNoUnknownNode);
    }

    private void assertChildLabels(final AstnnNode node, final String... labels) {
        assertThat(node.children()).hasSize(labels.length);

        for (int i = 0; i < node.children().size(); ++i) {
            assertThat(node.children().get(i).label()).isEqualTo(labels[i]);
        }
    }

    private AstnnNode walkTree(final AstnnNode root, int... path) {
        AstnnNode result = root;

        for (int idx : path) {
            assertThat(result.children().size()).isGreaterThan(idx);
            result = result.children().get(idx);
        }

        return result;
    }

    private Program getProgram(final String filename) throws Exception {
        return getAST("src/test/fixtures/ml_preprocessing/astnn/" + filename);
    }
}
