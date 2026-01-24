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
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.PlayDrumForBeats;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for the {@link PyStageVisitor} export functionality.
 */
public class PyStageVisitorTest implements JsonTest {

    @Test
    public void testProgramStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("from pystage.en import Stage");
        assertThat(result).contains("stage = Stage()");
        assertThat(result).contains("stage.play()");
    }

    @Test
    public void testSpriteCreation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("= stage.add_a_sprite()");
    }

    @Test
    public void testMotionBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.move(10)");
        assertThat(result).contains("self.turn_right(15)");
        assertThat(result).contains("self.turn_left(15)");
        assertThat(result).contains("self.point_in_direction(90)");
    }

    @Test
    public void testControlBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.wait(1)");
        assertThat(result).contains("for _ in range(int(10)):");
        assertThat(result).contains("if ");
        assertThat(result).contains("while True:");
        assertThat(result).contains("self.stop_all()");
    }

    @Test
    public void testLooksBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/lookblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.say(\"Hello!\")");
        assertThat(result).contains("self.think(\"Hmm...\")");
        assertThat(result).contains("self.next_costume()");
        assertThat(result).contains("self.show()");
        assertThat(result).contains("self.hide()");
    }

    @Test
    public void testSoundBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.play_sound_until_done(");
        assertThat(result).contains("self.start_sound(");
        assertThat(result).contains("self.stop_all_sounds()");
    }

    @Test
    public void testVariableBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.set_variable(");
    }

    @Test
    public void testVariableDeclarations() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains(".create_variable(");
    }

    @Test
    public void testArithmeticExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/arithmeticblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("+");
        assertThat(result).contains("-");
        assertThat(result).contains("*");
        assertThat(result).contains("/");
        assertThat(result).contains("round(");
    }

    @Test
    public void testBooleanExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleanblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains(">");
        assertThat(result).contains("<");
        assertThat(result).contains("_equals(");
        assertThat(result).contains("not ");
        assertThat(result).contains(" and ");
        assertThat(result).contains(" or ");
    }

    @Test
    public void testCloneBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("when_i_start_as_a_clone(");
        assertThat(result).contains("self.create_clone_of_myself()");
        assertThat(result).contains("self.delete_this_clone()");
    }

    @Test
    public void testMessageBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("when_i_receive_message(\"message1");
        assertThat(result).contains("self.broadcast(");
    }

    @Test
    public void testEventHandlers() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("def ");
        assertThat(result).contains("(self):");
        assertThat(result).contains(".when_program_starts(");
    }

    @Test
    public void testGreenFlagHandler() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("when_program_starts(");
    }

    @Test
    public void testKeyPressedHandler() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.ask_and_wait(");
        assertThat(result).contains("self.reset_timer()");
    }

    @Test
    public void testPythonIndentation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).doesNotContain("{");
        assertThat(result).doesNotContain("}");
        assertThat(result).contains("    ");
    }

    @Test
    public void testEmptyStmtListGeneratesPass() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("from pystage.en import Stage");
        assertThat(result).contains("stage.play()");
    }

    @Test
    public void testNullMd5ext() {
        ImageMetadata imageMetadata =
                new ImageMetadata(
                        "assetId", "costume1", null, "png", 1.0, 10.0, 10.0);

        List<ImageMetadata> imageList = new ArrayList<>();
        imageList.add(imageMetadata);
        ImageMetadataList costumes =
                new ImageMetadataList(imageList);

        ActorMetadata metadata =
                new ActorMetadata(
                        new CommentMetadataList(Collections.emptyList()),
                        0,
                        costumes,
                        new SoundMetadataList(Collections.emptyList())
                );

        ActorDefinition actor =
                new ActorDefinition(
                        ActorType.getSprite(),
                        new StrId("Sprite1"),
                        new DeclarationStmtList(Collections.emptyList()),
                        new SetStmtList(Collections.emptyList()),
                        new ProcedureDefinitionList(Collections.emptyList()),
                        new ScriptList(Collections.emptyList()),
                        metadata
                );

        String result = PyStageVisitor.of(actor);
        assertThat(result).doesNotContain("null");
        assertThat(result).contains("Sprite1.add_costume(\"images/costume1\", center_x=10.0, center_y=10.0)");
    }

    @Test
    public void testUnhandledNodesAndVariableVisibility() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                show variable [myVar v]
                hide variable [myVar v]
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("def Sprite1_on_start_0(self):");
        assertThat(result).contains("self.show_variable(\"myVar\")");
        assertThat(result).contains("self.hide_variable(\"myVar\")");
    }

    @Test
    public void testChangeGraphicEffectBy() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                change [color v] effect by (25)
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.change_color_effect_by(25)");
        assertThat(result).doesNotContain("# Unhandled node: ChangeGraphicEffectBy");
    }

    @Test
    public void testUnhandledExpressionListAndBroadcast() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                broadcast (message1 v)
                set [myVar v] to [value]
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.broadcast(\"message1\")");
        assertThat(result).contains("set_variable(\"myVar\", \"value\")");
    }

    @Test
    public void testListOperations() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                add [thing] to [myList v]
                delete (1) of [myList v]
                delete all of [myList v]
                insert [thing] at (1) of [myList v]
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.get_variable(\"myList\").append(\"thing\")");
        assertThat(result).contains("self.get_variable(\"myList\").pop(int(1) - 1)");
        assertThat(result).contains("self.set_variable(\"myList\", [])");
        assertThat(result).contains("self.get_variable(\"myList\").insert(int(1) - 1, \"thing\")");

        assertThat(result).doesNotContain("delete_all_of_list");
        assertThat(result).doesNotContain("delete_of_list");
        assertThat(result).doesNotContain("add_to_list");
        assertThat(result).doesNotContain("insert_at_list");
    }

    @Test
    public void testAttributeOf() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say ([x position v] of (Sprite1 v))
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("Sprite1.x_position()");
    }

    @Test
    public void testStageGreenFlag() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                wait (1) secs
                """;

        Program program = parseProgram(code, "Stage");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("stage.when_GREENFLAG_clicked(");
        assertThat(result).doesNotContain("stage.when_program_starts(");
    }

    @Test
    public void testSpriteGreenFlagExplicit() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code, "Sprite1");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("Sprite1.when_program_starts(");
        assertThat(result).doesNotContain("Sprite1.when_GREENFLAG_clicked(");
    }

    @Test
    public void testAttributeOfVariable() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say ([myVar v] of (Sprite1 v))
                """;

        Program program = parseProgram(code, "Sprite1");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("Sprite1.get_variable(\"myVar\")");
    }

    @Test
    public void testCostumeNode() throws IOException, ParsingException {
        // Test visiting costume expression nodes
        Costume costumeNum =
                new Costume(
                        new NameNum("number"),
                        new NoBlockMetadata()
                );

        // Use a dummy visitor for unit testing this node
        PyStageVisitor visitor = new PyStageVisitor();
        // Force inHandler to true to test "self." generation
        try {
            java.lang.reflect.Field f = PyStageVisitor.class.getDeclaredField("inHandler");
            f.setAccessible(true);
            f.setBoolean(visitor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        costumeNum.accept(visitor);
        assertThat(visitor.getOutput()).contains("self.costume_number()");

        Costume costumeName =
                new Costume(
                        new NameNum("name"),
                        new NoBlockMetadata()
                );

        visitor = new PyStageVisitor(); // Reset
        try {
            java.lang.reflect.Field f = PyStageVisitor.class.getDeclaredField("inHandler");
            f.setAccessible(true);
            f.setBoolean(visitor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        costumeName.accept(visitor);
        assertThat(visitor.getOutput()).contains("self.costume_name()");
    }

    @Test
    public void testBackdropNode() throws IOException, ParsingException {
        Backdrop backdropNum =
                new Backdrop(
                        new NameNum("number"),
                        new NoBlockMetadata()
                );
        String resultNum = PyStageVisitor.of(backdropNum);
        assertThat(resultNum).contains("stage.backdrop_number()");

        Backdrop backdropName =
                new Backdrop(
                        new NameNum("name"),
                        new NoBlockMetadata()
                );
        String resultName = PyStageVisitor.of(backdropName);
        assertThat(resultName).contains("stage.backdrop_name()");
    }

    @Test
    public void testStageWaitAndLoops() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                wait (1) secs
                forever
                    wait until (1 = 1)
                end
                """;

        Program program = parseProgram(code, "Stage");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.wait_seconds(1)");
        assertThat(result).doesNotContain("self.wait(");
        assertThat(result).contains("self.wait_seconds(0)");
    }

    @Test
    public void testStageBroadcastHandler() throws IOException, ParsingException {
        String code = """
                when I receive [message1 v]
                wait (1) secs
                """;

        Program program = parseProgram(code, "Stage");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("stage.when_i_receive(\"message1\", stage_on_message_0)");
        assertThat(result).doesNotContain("stage._core.code_manager.code_blocks[\"stage_on_message_0\"] = stage_on_message_0");
    }

    @Test
    public void testProcedureNameMismatch() throws IOException, ParsingException {
        String code = """
                define Text (text) at (x) (y) with color (color) with sound (sound)
                Text [Hello] at (0) (0) with color [red] with sound []
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).doesNotContain("__s_");

        java.util.regex.Pattern defPattern = java.util.regex.Pattern.compile("def Sprite1_Text_+at_+with_color_+with_sound");
        if (!defPattern.matcher(result).find()) {
            org.junit.jupiter.api.Assertions.fail("Definition pattern not found. Result was:\n" + result);
        }

        java.util.regex.Pattern callPattern = java.util.regex.Pattern.compile("Sprite1_Text_+at_+with_color_+with_sound\\(self");
        if (!callPattern.matcher(result).find()) {
            org.junit.jupiter.api.Assertions.fail("Call pattern not found. Result was:\n" + result);
        }
    }

    @Test
    public void testMathFunctions() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say ([sqrt v] of (9))
                say ([sin v] of (90))
                say ([floor v] of (3.5))
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("import math");
        assertThat(result).contains("math.sqrt(9)");
        assertThat(result).contains("math.sin(math.radians(90))");
        assertThat(result).contains("math.floor(3.5)");
    }

    @Test
    public void testUnspecifiedExpression() throws IOException, ParsingException {
        UnspecifiedExpression unspecified =
                new UnspecifiedExpression();

        String result = PyStageVisitor.of(unspecified);
        assertThat(result).contains("None");
        assertThat(result).doesNotContain("# Unhandled node");
    }

    @Test
    public void testUnspecifiedExpressionInContext() throws IOException, ParsingException {
        UnspecifiedExpression unspecified =
                new UnspecifiedExpression();

        StrId varName =
                new StrId("myVar");

        SetVariableTo setVar =
                new SetVariableTo(
                        varName,
                        unspecified,
                        new NoBlockMetadata()
                );

        String result = PyStageVisitor.of(setVar);
        assertThat(result).contains("set_variable(\"myVar\", None)");
        assertThat(result).doesNotContain("# Unhandled node");
    }

    @Test
    public void testPenBlocksInIfGeneratesPass() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <(1) = (1)> then
                    erase all
                end
                """;

        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("if _equals(1, 1):");
        assertThat(result).contains("# pen block (not supported in PyStage)");
        assertThat(result).contains("pass");
    }

    @Test
    public void testUnspecifiedBoolExpr() throws IOException, ParsingException {
        UnspecifiedBoolExpr unspecified =
                new UnspecifiedBoolExpr();

        String result = PyStageVisitor.of(unspecified);
        assertThat(result).contains("False");
        assertThat(result).doesNotContain("# Unhandled node");
    }

    @Test
    public void testUnspecifiedBoolExprInIf() throws IOException, ParsingException {
        UnspecifiedBoolExpr unspecified =
                new UnspecifiedBoolExpr();

        IfThenStmt ifStmt =
                new IfThenStmt(
                        unspecified,
                        new StmtList(Collections.emptyList()),
                        new NoBlockMetadata()
                );

        String result = PyStageVisitor.of(ifStmt);

        assertThat(result).contains("if False:");
        assertThat(result).contains("pass");
        assertThat(result).doesNotContain("# Unhandled node");
    }

    @Test
    public void testTranslateBlock() throws IOException, ParsingException {
        TranslateTo translateTo =
                new TranslateTo(
                        new StringLiteral("hello"),
                        new TFixedLanguage("de", new NoBlockMetadata()),
                        new NoBlockMetadata()
                );

        String result = PyStageVisitor.of(translateTo);
        assertThat(result).contains("\"\"");
        assertThat(result).doesNotContain("# Unhandled node");
    }

    @Test
    public void testMusicBlockAsStatement() throws IOException, ParsingException {
        PlayDrumForBeats drum =
                new PlayDrumForBeats(
                        new FixedDrum("1", new NoBlockMetadata()),
                        new NumberLiteral(0.25),
                        new NoBlockMetadata()
                );

        java.util.List<Stmt> stmts = new java.util.ArrayList<>();
        stmts.add(drum);
        stmts.add(drum);

        RepeatForeverStmt forever =
                new RepeatForeverStmt(
                        new StmtList(stmts),
                        new NoBlockMetadata()
                );

        String result = PyStageVisitor.of(forever);

        assertThat(result).doesNotContain("    0    0");
        assertThat(result).contains("# Unhandled node: PlayDrumForBeats");
        assertThat(result).contains("pass");
    }

    @Test
    public void testStringEscaping() throws IOException, ParsingException {
        // Manually construct nodes to avoid parsing issues with quotes in block args
        StringLiteral msgLiteral =
                new StringLiteral("msg \"with\" quotes");

        Message messageWrapper =
                new Message(msgLiteral);

        Broadcast broadcast =
                new Broadcast(
                        messageWrapper,
                        new NoBlockMetadata()
                );

        Say say =
                new Say(
                        new StringLiteral("Hello \"World\""),
                        new NoBlockMetadata()
                );

        java.util.List<Stmt> stmts = new java.util.ArrayList<>();
        stmts.add(broadcast);
        stmts.add(say);
        StmtList stmtList = new StmtList(stmts);

        ReceptionOfMessage reception =
                new ReceptionOfMessage(
                        messageWrapper,
                        new NoBlockMetadata()
                );

        Script script =
                new Script(reception, stmtList);

        reception.setParentNode(script);
        stmtList.setParentNode(script);

        ActorMetadata metadata =
                new ActorMetadata(
                        new CommentMetadataList(java.util.Collections.emptyList()),
                        0,
                        new ImageMetadataList(java.util.Collections.emptyList()),
                        new SoundMetadataList(java.util.Collections.emptyList())
                );

        ActorDefinition actor =
                new ActorDefinition(
                        ActorType.getSprite(),
                        new StrId("Sprite1"),
                        new DeclarationStmtList(java.util.Collections.emptyList()),
                        new SetStmtList(java.util.Collections.emptyList()),
                        new ProcedureDefinitionList(java.util.Collections.emptyList()),
                        new ScriptList(java.util.List.of(script)),
                        metadata
                );

        ExtensionMetadata extensions =
                new ExtensionMetadata(java.util.Collections.emptyList());

        MetaMetadata meta =
                new MetaMetadata("", "", "");

        ProgramMetadata progMeta =
                new ProgramMetadata(
                        new MonitorMetadataList(java.util.Collections.emptyList()),
                        extensions,
                        meta
                );

        Program program = new Program(
                new StrId("Prog"),
                new ActorDefinitionList(java.util.List.of(actor)),
                null,
                null,
                progMeta
        );

        String result = PyStageVisitor.of(program);

        assertThat(result).contains("when_i_receive_message(\"msg \\\"with\\\" quotes");
        assertThat(result).contains("broadcast(\"msg \\\"with\\\" quotes");
        assertThat(result).contains("say(\"Hello \\\"World\\\"\")");
    }

    @Test
    public void testStageBackdropSwitchAndWait() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                switch backdrop to (backdrop1 v) and wait
                """;
        Program program = parseProgram(code, "Stage");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("stage.switch_backdrop_to_and_wait(");
    }

    // Consolidated tests from small files

    @Test
    public void testAnyKeySensing_Consolidated() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/any_key_sensing.json");
        String result = PyStageVisitor.of(program);
        assertThat(result).contains("any(pygame.key.get_pressed())");
        assertThat(result).contains("import pygame");
    }

    @Test
    public void testSwitchBackdropAndWait_Consolidated() throws IOException, ParsingException {
        // Manual AST construction
        SwitchBackdropAndWait switchNode = new SwitchBackdropAndWait(
                new WithExpr(new StringLiteral("backdrop1"), new NoBlockMetadata()),
                new NoBlockMetadata()
        );
        String result = PyStageVisitor.of(switchNode);
        assertThat(result).contains("switch_backdrop_to_and_wait");
    }

    @Test
    public void testCreateCloneOfLiteral_Consolidated() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                create clone of (Beetle v)
                """;
        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.create_clone_of_sprite(Beetle)");
    }

    @Test
    public void testDaysSince2000Export_Consolidated() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say (days since 2000)
                """;
        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("self.days_since()");
    }

    @Test
    public void testGlobalAndLocalVariableCollision_Consolidated() {
        // Create Program with colliding variable names
        List<ActorDefinition> actorDefs = new ArrayList<>();

        DeclarationStmtList stageDecls = new DeclarationStmtList(new ArrayList<>());
        Variable globalVar = new Variable(new StrId("zombie_lives"));
        stageDecls.getDeclarationStmts().add(new DeclarationIdentAsTypeStmt(globalVar, new NumberType()));

        ActorMetadata stageMetadata = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                new StrId("Stage"),
                stageDecls,
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                stageMetadata
        );
        actorDefs.add(stage);

        DeclarationStmtList spriteDecls = new DeclarationStmtList(new ArrayList<>());
        Variable localVar = new Variable(new StrId("zombie_lives"));
        spriteDecls.getDeclarationStmts().add(new DeclarationIdentAsTypeStmt(localVar, new NumberType()));

        ActorMetadata spriteMetadata = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("EvilMe"),
                spriteDecls,
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                spriteMetadata
        );
        actorDefs.add(sprite);

        ProgramMetadata programMetadata = new ProgramMetadata(
                new MonitorMetadataList(new ArrayList<>()),
                new ExtensionMetadata(new ArrayList<>()),
                new MetaMetadata("3.0.0", "0.2.0", "Mozilla/5.0")
        );

        Program program = new Program(
                new StrId("Program"),
                new ActorDefinitionList(actorDefs),
                null,
                null,
                programMetadata
        );

        String output = PyStageVisitor.of(program);

        assertThat(output).contains("stage.create_variable(\"zombie_lives\")");
        assertThat(output).contains("EvilMe._core.pystage_makevariable(\"zombie_lives_local\", all_sprites=False)");
    }

    @Test
    public void testEmitAsNumber_Consolidated() throws IOException, ParsingException {
        // Manual AST construction for nested arithmetic
        NoBlockMetadata meta = new NoBlockMetadata();
        Add inner = new Add(new NumberLiteral(1), new NumberLiteral(2), meta);
        Add outer = new Add(inner, new NumberLiteral(3), meta);
        String arithmeticResult = PyStageVisitor.of(outer);
        assertThat(arithmeticResult).doesNotContain("_to_num");

        // Manual AST for Boolean to number
        StrId varName = new StrId("myVar");
        Equals equals = new Equals(new NumberLiteral(1), new NumberLiteral(1), meta);
        ChangeVariableBy change = new ChangeVariableBy(varName, equals, meta);
        String boolResult = PyStageVisitor.of(change);
        assertThat(boolResult).contains("_to_num(_equals(1, 1))");

        // Manual AST for Variable to number
        Variable var = new Variable(new StrId("myVar"));
        AsNumber asNum = new AsNumber(var, meta);
        Div div = new Div(new NumberLiteral(1), asNum, meta);
        String varResult = PyStageVisitor.of(div);
        assertThat(varResult).contains("_to_num(self.get_variable(\"myVar\"))");
    }

    @Test
    public void testEqualsTypeCoercion_Consolidated() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <[5] = (5)> then
                end
                """;
        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("_equals(\"5\", 5)");
    }

    @Test
    public void testGoToWithRandomNumberIncludesHelper_Consolidated() throws IOException, ParsingException {
        // Manual AST
        PickRandom pickRandom = new PickRandom(new NumberLiteral(1), new NumberLiteral(10), new NoBlockMetadata());
        FromExpression fromExpr = new FromExpression(new AsString(pickRandom), new NoBlockMetadata());
        GoToPos goTo = new GoToPos(fromExpr, new NoBlockMetadata());

        String result = PyStageVisitor.of(goTo);
        assertThat(result).contains("_go_to_helper(self, _pick_random(1, 10))");
    }

    @Test
    public void testIndexOfMixedTypes_Consolidated() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say (item # of (5) in [myList v])
                """;
        Program program = parseProgram(code);
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("_index_of(");
    }

    @Test
    public void testDeleteAllOfList_Consolidated() throws IOException, ParsingException {
        // Manual AST
        DeleteOf deleteNode = new DeleteOf(
                new AsNumber(new StringLiteral("all")),
                new StrId("myList"),
                new NoBlockMetadata()
        );

        String result = PyStageVisitor.of(deleteNode);

        assertThat(result.trim()).isEqualTo("self.set_variable(\"myList\", [])");
    }

    @Test
    public void testSoundExportUsesFilePath_Consolidated() {
        SoundMetadata sound = new SoundMetadata("assetId", "pop", "83a9787d4cb6f3b7632b4ddfebf74367.wav", "wav", 123, 123);
        List<SoundMetadata> sounds = new ArrayList<>();
        sounds.add(sound);
        SoundMetadataList soundList = new SoundMetadataList(sounds);

        ActorMetadata actorMetadata = new ActorMetadata(new CommentMetadataList(new ArrayList<>()), 0, new ImageMetadataList(new ArrayList<>()), soundList);

        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("MySprite"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMetadata
        );

        List<ActorDefinition> actors = new ArrayList<>();
        actors.add(sprite);
        ActorDefinitionList actorList = new ActorDefinitionList(actors);

        ProgramMetadata programMetadata = new ProgramMetadata(
                new MonitorMetadataList(new ArrayList<>()),
                new ExtensionMetadata(new ArrayList<>()),
                new MetaMetadata("3.0.0", "0.2.0", "Mozilla")
        );

        Program program = new Program(new StrId("TestProgram"), actorList, null, null, programMetadata);

        String code = PyStageVisitor.of(program);
        assertThat(code).contains("MySprite.add_sound(\"sounds/pop.wav\")");
    }

    @Test
    public void testSoundNameResolvedToFilePath_Consolidated() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        String result = PyStageVisitor.of(program);

        assertThat(result).contains("start_sound(\"sounds/Meow.wav\")");
        assertThat(result).contains("play_sound_until_done(\"sounds/Meow.wav\")");
    }

    @Test
    public void testStageSay_Consolidated() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stage_with_say.json");
        String output = PyStageVisitor.of(program);

        assertThat(output).doesNotContain("stage.say(");
        assertThat(output).doesNotContain("self.say(");
        assertThat(output).contains("# Stage cannot say");
    }

    @Test
    public void testStageSize_Consolidated() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                set size to (100)%
                change size by (10)
                """;
        Program program = parseProgram(code, "Stage");
        String result = PyStageVisitor.of(program);

        assertThat(result).doesNotContain("self.set_size_to");
        assertThat(result).doesNotContain("self.change_size_by");
        assertThat(result).contains("pass");
    }

    @Test
    public void testExportWithNullMd5ext_Consolidated(@TempDir Path tempDir) {
        Path sb3File = tempDir.resolve("project.sb3");
        assertDoesNotThrow(() -> {
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(sb3File))) {
                zos.putNextEntry(new ZipEntry("project.json"));
                zos.write("{}".getBytes());
                zos.closeEntry();
            }
        });

        String json = "{\"targets\":[{\"isStage\":true,\"name\":\"Stage\",\"variables\":{},\"lists\":{},\"broadcasts\":{},\"blocks\":{},\"comments\":{},\"currentCostume\":0,\"costumes\":[{\"assetId\":\"id1\",\"name\":\"c1\",\"dataFormat\":\"png\",\"md5ext\":null,\"rotationCenterX\":0,\"rotationCenterY\":0}],\"sounds\":[],\"volume\":100,\"layerOrder\":1,\"visible\":true,\"x\":0,\"y\":0,\"size\":100,\"direction\":90,\"draggable\":false,\"rotationStyle\":\"all around\"}], \"meta\": {\"semver\": \"3.0.0\", \"vm\": \"0.2.0\", \"agent\": \"Mozilla\"}}";

        Scratch3Parser parser = new Scratch3Parser();
        Program program = assertDoesNotThrow(() -> parser.parseString("testproject", json));

        Path outputDir = tempDir.resolve("output");
        PyStageProjectExporter exporter = new PyStageProjectExporter();

        assertDoesNotThrow(() -> exporter.export(sb3File, program, outputDir));
    }

    @Test
    public void testSanitizedAssetPaths() {
        // Create metadata with assets having special characters
        ImageMetadata costume = new ImageMetadata("abc1234567890", "Costume ?! Name", "abc1234567890.png", "png", 1.0, 10.0, 10.0);
        SoundMetadata sound = new SoundMetadata("def1234567890", "Sound ?! Name", "def1234567890.wav", "wav", 44100, 100);

        ActorMetadata metadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(List.of(costume)),
                new SoundMetadataList(List.of(sound))
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                new ScriptList(Collections.emptyList()),
                metadata
        );

        String result = PyStageVisitor.of(actor);

        // Expect standard sanitization: special chars replaced by underscores
        // Check costume path
        assertThat(result).contains("images/Costume _! Name.png");

        // Check sound path
        assertThat(result).contains("sounds/Sound _! Name.wav");
    }

    @Test
    public void testSvgAssetPaths() {
        // Create metadata with an SVG asset
        ImageMetadata costume = new ImageMetadata("svg123", "Vector Costume", "svg123.svg", "svg", 1.0, 10.0, 10.0);

        ActorMetadata metadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(List.of(costume)),
                new SoundMetadataList(Collections.emptyList())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                new ScriptList(Collections.emptyList()),
                metadata
        );

        String result = PyStageVisitor.of(actor);

        // SVG extension should be preserved
        assertThat(result).contains("images/Vector Costume.svg");
    }

    @Test
    public void testStageBroadcastRegistration() throws IOException, ParsingException {
        String input = """
                when I receive [test_msg v]
                say [hello]
                """;
        Program program = parseProgram(input, "Stage");
        String pyCode = PyStageVisitor.of(program);

        // Check for correct method call
        assertThat(pyCode).contains("stage.when_i_receive(\"test_msg\", stage_on_message_0)");

        // Check absence of manual injection
        assertThat(pyCode).doesNotContain("_core.code_manager.code_blocks");
        assertThat(pyCode).doesNotContain("_core.code_manager.broadcast_blocks");
    }

    @Test
    public void testSpriteBroadcastRegistration() throws IOException, ParsingException {
        String input = """
                when I receive [test_msg v]
                say [meow]
                """;
        Program program = parseProgram(input, "Sprite1");
        String pyCode = PyStageVisitor.of(program);

        // Check for correct method call on sprite
        assertThat(pyCode).contains("Sprite1.when_i_receive_message(\"test_msg\", Sprite1_on_message_0)");
    }

    private Program parseProgram(String code) throws IOException, ParsingException {
        return parseProgram(code, "Sprite1");
    }

    private Program parseProgram(String code, String targetName) throws IOException, ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        return parser.extendProject(baseProject, targetName, code);
    }
}
