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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptVisitor} export functionality.
 * <p>
 * This test class uses the ScratchBlocks parsing approach where possible,
 * following the pattern established in PyStageVisitorTest.
 */
public class GoboscriptVisitorTest implements JsonTest {

    // ==================== Motion Blocks ====================

    @Test
    public void testMotionBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("sprite");
        assertThat(result).contains("onflag {");
        assertThat(result).contains("move 10;");
        assertThat(result).contains("turn_right 15;");
        assertThat(result).contains("turn_left 15;");
        assertThat(result).contains("goto");
        assertThat(result).contains("point_in_direction 90;");
    }

    @Test
    public void testGlideBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/glideblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("glide_to_random_position 1;");
        assertThat(result).contains("glide 2, 100, 50;");
    }

    @Test
    public void testGlideToSprite() throws ParsingException {
        String code = """
                when green flag clicked
                glide (3) secs to (Ball v)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Goboscript doesn't support glide_to with sprite target, should use goto with comment
        assertThat(result).contains("goto \"Ball\"; # was: glide_to 3s");
        assertThat(result).doesNotContain("glide_to \"Ball\", 3;");
    }

    // ==================== Control Blocks ====================

    @Test
    public void testControlBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("onflag {");
        assertThat(result).contains("wait 1;");
        assertThat(result).contains("repeat 10 {");
        assertThat(result).contains("if");
        assertThat(result).contains("forever {");
        assertThat(result).contains("stop_all;");
    }

    @Test
    public void testWaitUntil() throws ParsingException {
        String code = """
                when green flag clicked
                wait until <touching (Scheibe v)?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("until touching(\"Scheibe\") {}");
    }

    // ==================== Looks Blocks ====================

    @Test
    public void testLooksBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/lookblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("say \"Hello!\"");
        assertThat(result).contains("think \"Hmm...\"");
        assertThat(result).contains("switch_costume");
        assertThat(result).contains("next_costume;");
        assertThat(result).contains("change_size");
        assertThat(result).contains("set_size");
        assertThat(result).contains("show;");
        assertThat(result).contains("hide;");
    }

    @Test
    public void testSayForSecs() throws ParsingException {
        String code = """
                when green flag clicked
                say [Hello] for (2) secs
                think [Hmm] for (1.5) secs
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("say \"Hello\", 2;");
        assertThat(result).contains("think \"Hmm\", 1.5;");
    }

    // ==================== Sound Blocks ====================

    @Test
    public void testSoundBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("play_sound_until_done");
        assertThat(result).contains("start_sound");
        assertThat(result).contains("stop_all_sounds;");
        assertThat(result).contains("change_volume");
        assertThat(result).contains("set_volume");
    }

    // ==================== Variable Blocks ====================

    @Test
    public void testVariableBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("= 0;");
        assertThat(result).contains("+= 1;");
        assertThat(result).contains("add");
        assertThat(result).contains("delete");
    }

    @Test
    public void testGlobalVariableAccess() throws ParsingException {
        String code = """
                when green flag clicked
                change [Score v] by (1)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // In Goboscript, global variables are accessed by name directly
        assertThat(result).contains("Score += 1;");
    }

    // ==================== Arithmetic Expressions ====================

    @Test
    public void testArithmeticExpressions() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/arithmeticblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("+");
        assertThat(result).contains("-");
        assertThat(result).contains("*");
        assertThat(result).contains("/");
        assertThat(result).contains("round");
        assertThat(result).contains("random");
    }

    @Test
    public void testMathFunctionsAndInclude() throws ParsingException {
        String code = """
                when green flag clicked
                set x to ([abs v] of (-1))
                set x to ([floor v] of (1.5))
                set x to ([ceiling v] of (1.5))
                set x to ([sqrt v] of (4))
                set x to ([sin v] of (90))
                set x to ([cos v] of (0))
                set x to ([tan v] of (45))
                set x to ([asin v] of (1))
                set x to ([acos v] of (0))
                set x to ([atan v] of (1))
                set x to ([ln v] of (1))
                set x to ([log v] of (10))
                set x to ([e ^ v] of (1))
                set x to ([10 ^ v] of (1))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("%include std/math");
        assertThat(result).contains("abs(-1)");
        assertThat(result).contains("floor(1.5)");
        assertThat(result).contains("ceil(1.5)");
        assertThat(result).contains("sqrt(4)");
        assertThat(result).contains("sin(90)");
        assertThat(result).contains("cos(0)");
        assertThat(result).contains("tan(45)");
        assertThat(result).contains("asin(1)");
        assertThat(result).contains("acos(0)");
        assertThat(result).contains("atan(1)");
        assertThat(result).contains("ln(1)");
        assertThat(result).contains("log(10)");
        assertThat(result).contains("exp(1)");
        assertThat(result).contains("pow10(1)");
    }

    @Test
    public void testNoMathInclude() throws ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).doesNotContain("%include std/math");
    }

    @Test
    public void testMathCeiling() throws ParsingException {
        String code = """
                when green flag clicked
                set x to ([ceiling v] of (3.2))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("ceil(3.2)");
    }

    @Test
    public void testDaysSince2000() throws ParsingException {
        String code = """
                when green flag clicked
                set x to (days since 2000)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("days_since_2000()");
    }

    // ==================== Boolean Expressions ====================

    @Test
    public void testBooleanExpressions() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleanblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains(">");
        assertThat(result).contains("<");
        assertThat(result).contains("==");
        assertThat(result).contains("not");
        assertThat(result).contains("and");
        assertThat(result).contains("or");
    }

    @Test
    public void testTouchingColor() throws ParsingException {
        String code = """
                when green flag clicked
                wait until <touching color (#ff0000)?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("touching_color(0xff0000)");
    }

    @Test
    public void testColorIsTouchingColor() throws ParsingException {
        String code = """
                when green flag clicked
                wait until <color (#ff0000) is touching (#00ff00)?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("color_is_touching_color(0xff0000, 0x00ff00)");
    }

    @Test
    public void testMouseDown() throws ParsingException {
        String code = """
                when green flag clicked
                wait until <mouse down?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("until mouse_down() {}");
    }

    // ==================== Clone Blocks ====================

    @Test
    public void testCloneBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("onclone {");
        assertThat(result).contains("clone;");
        assertThat(result).contains("delete_this_clone;");
    }

    @Test
    public void testCreateCloneOfMyself() throws ParsingException {
        String code = """
                when green flag clicked
                create clone of (myself v)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("clone;");
    }

    // ==================== Message Blocks ====================

    @Test
    public void testMessageBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("on \"message1\"");
        assertThat(result).contains("broadcast");
    }

    // ==================== Custom Blocks ====================

    @Test
    public void testCustomBlocks() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("proc");
        assertThat(result).contains("block1");
        assertThat(result).contains("block2");
    }

    // ==================== Sensing Blocks ====================

    @Test
    public void testSensingStatements() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("ask \"What's your name?\";");
        assertThat(result).contains("set_drag_mode_draggable;");
        assertThat(result).contains("reset_timer;");
    }

    @Test
    public void testSensingReporters() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingreporters.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("distance_to_mouse_pointer()");
        assertThat(result).contains("days_since_2000");
        assertThat(result).contains("current_year");
        assertThat(result).contains("contains(");
        assertThat(result).contains("length(");
        assertThat(result).contains("\"apple\"[1]");
    }

    // ==================== Custom Blocks and Procedures ====================

    @Test
    public void testUndefinedProcedureCall() throws ParsingException {
        String code = """
                when green flag clicked
                undefined_block
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Undefined procedures should be commented out
        assertThat(result).contains("# Unsupported procedure call (cross-sprite or undefined): undefined_block;");
    }

    @Test
    public void testProcedureNameConflict() throws ParsingException {
        String code = """
                define move
                move 10 steps

                when green flag clicked
                move
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // 'move' is a keyword in Goboscript, so it should be renamed to 'move_proc'
        assertThat(result).contains("proc move_proc {");
        assertThat(result).contains("move_proc;");
    }

    @Test
    public void testProcedureOverloading() throws ParsingException {
        // Scratch allows procedure overloading (same name, different parameters)
        // Goboscript does not, so we disambiguate by appending a counter suffix
        String code = """
                define block_name
                say [no params]

                define block_name (value)
                say (value)

                when green flag clicked
                block_name
                block_name [hello]
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Both procedures should get unique names with counter suffix
        assertThat(result).contains("proc block_name_1 {");
        assertThat(result).contains("proc block_name_2 value {");
        // Calls should use the same unique names
        assertThat(result).contains("block_name_1;");
        assertThat(result).contains("block_name_2 \"hello\";");
    }

    @Test
    public void testProcedureOverloadingThreeVariants() throws ParsingException {
        // Test with 0, 1, and 2 parameters to verify suffix handles multiple overloads
        String code = """
                define greet
                say [Hello]

                define greet (name)
                say (join [Hello ] (name))

                define greet (name) (times)
                repeat (times)
                    say (join [Hello ] (name))
                end

                when green flag clicked
                greet
                greet [World]
                greet [World] (3)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // All three variants should get unique counter-based names
        assertThat(result).contains("proc greet_1 {");
        assertThat(result).contains("proc greet_2 name {");
        assertThat(result).contains("proc greet_3 name, times {");
        // Calls should use the correct unique names
        assertThat(result).contains("greet_1;");
        assertThat(result).contains("greet_2 \"World\";");
        assertThat(result).contains("greet_3 \"World\", 3;");
    }

    @Test
    public void testProcedureOverloadingIdenticalSignatures() throws ParsingException {
        // Scratch allows multiple procedures with identical names AND identical parameter counts
        // Each should get a unique counter suffix
        String code = """
                define action (x)
                say [first]

                define action (y)
                say [second]

                define action (z)
                say [third]

                when green flag clicked
                action [a]
                action [b]
                action [c]
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // All three procedures have same name and 1 parameter, but get unique counter suffixes
        assertThat(result).contains("proc action_1 x {");
        assertThat(result).contains("proc action_2 y {");
        assertThat(result).contains("proc action_3 z {");
    }

    @Test
    public void testProcedureNoOverloadingNoSuffix() throws ParsingException {
        // When there's no overloading, no suffix should be added
        String code = """
                define my_proc (x) (y)
                say (join (x) (y))

                when green flag clicked
                my_proc [a] [b]
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // No suffix needed when there's no overloading
        assertThat(result).contains("proc my_proc x, y {");
        assertThat(result).contains("my_proc \"a\", \"b\";");
        // Verify no suffix was added
        assertThat(result).doesNotContain("my_proc_");
    }

    // ==================== Qualified Access ====================

    @Test
    public void testQualifiedVariableAccess() throws ParsingException {
        String code = """
                when green flag clicked
                say ([Score v] of (Stage v))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Stage variables are global in Goboscript, but AttributeOf currently emits qualification
        assertThat(result).contains("say Stage.Score;");
    }

    // ==================== Other Sensing Repoters ====================

    @Test
    public void testOtherSensingReporters() throws ParsingException {
        String code = """
                when green flag clicked
                set x to (mouse x)
                set x to (mouse y)
                set x to (loudness)
                say (answer)
                say (username)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("mouse_x()");
        assertThat(result).contains("mouse_y()");
        assertThat(result).contains("loudness()");
        assertThat(result).contains("answer()");
        assertThat(result).contains("username()");
    }

    // ==================== String Expressions ====================

    @Test
    public void testJoin() throws ParsingException {
        String code = """
                when green flag clicked
                say (join [Hello ] [World])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("(\"Hello \" & \"World\")");
    }

    @Test
    public void testNestedJoin() throws ParsingException {
        String code = """
                when green flag clicked
                say (join (join [A] [B]) [C])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("((\"A\" & \"B\") & \"C\")");
    }

    @Test
    public void testLetterOf() throws ParsingException {
        String code = """
                when green flag clicked
                say (letter (1) of [apple])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("\"apple\"[1]");
    }

    // ==================== Backdrop Blocks ====================

    @Test
    public void testBackdropSwitch() throws ParsingException {
        String code = """
                when green flag clicked
                switch backdrop to (previous backdrop v)
                switch backdrop to (next backdrop v)
                switch backdrop to (random backdrop v)
                """;
        Program program = parseProgram(code, "Stage");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("previous_backdrop;");
        assertThat(result).contains("next_backdrop;");
        assertThat(result).contains("random_backdrop;");
    }

    @Test
    public void testBackdropAndCostumeReporters() throws ParsingException {
        String code = """
                when green flag clicked
                say (backdrop [number v])
                say (backdrop [name v])
                say (costume [number v])
                say (costume [name v])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("backdrop_number()");
        assertThat(result).contains("backdrop_name()");
        assertThat(result).contains("costume_number()");
        assertThat(result).contains("costume_name()");
    }

    // ==================== Graphic Effects ====================

    @Test
    public void testGraphicEffects() throws ParsingException {
        String code = """
                when green flag clicked
                change [color v] effect by (25)
                set [ghost v] effect to (50)
                clear graphic effects
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("change_color_effect 25;");
        assertThat(result).contains("set_ghost_effect 50;");
        assertThat(result).contains("clear_graphic_effects;");
    }

    // ==================== Music and Tempo ====================

    @Test
    public void testTempoFallback() {
        // say (tempo)
        NoBlockMetadata meta = new NoBlockMetadata();
        Tempo tempoNode = new Tempo(meta);
        Say sayNode = new Say(new AsString(tempoNode), meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        sayNode.accept(visitor);
        String result = visitor.getOutput();

        // Goboscript doesn't support tempo, should fall back to 60
        assertThat(result).contains("say 60;");
        assertThat(result).doesNotContain("tempo()");
    }

    // ==================== Attribute Of Blocks ====================

    @Test
    public void testAttributeOfStage() throws ParsingException {
        String code = """
                when green flag clicked
                say ([backdrop # v] of (Stage v))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("Stage.backdrop_number");
    }

    @Test
    public void testCrossSpriteAttributeAccessFallback() {
        NoBlockMetadata meta = new NoBlockMetadata();

        // say ([x position] of [OtherSprite])
        AttributeOf attrOf = new AttributeOf(
                new AttributeFromFixed(
                        new FixedAttribute(FixedAttribute.FixedAttributeType.X_POSITION.getType())
                ),
                new WithExpr(new StringLiteral("OtherSprite"), meta),
                meta
        );
        Say say = new Say(attrOf, meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        say.accept(visitor);
        String result = visitor.getOutput();

        // Goboscript supports cross-sprite attribute access
        assertThat(result).contains("say \"OtherSprite\".x;");
    }

    // ==================== Special Number Values ====================

    @Test
    public void testSpecialNumberValues() throws ParsingException {
        String code = """
                when green flag clicked
                set [x v] to ((1) / (0))
                set [x v] to ((-1) / (0))
                set [x v] to ((0) / (0))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("x = (1 / 0);");
        assertThat(result).contains("x = (-1 / 0);");
        assertThat(result).contains("x = (0 / 0);");
    }

    // ==================== Video Sensing (Unsupported) ====================

    @Test
    public void testVideoSensingUnsupported() {
        NoBlockMetadata meta = new NoBlockMetadata();

        GoboscriptVisitor visitor = new GoboscriptVisitor();

        new TurnVideo("on", meta).accept(visitor);
        new SetVideoTransparency(new NumberLiteral(50), meta).accept(visitor);

        String result = visitor.getOutput();

        assertThat(result).contains("# Unsupported: turn video on/off");
        assertThat(result).contains("# Unsupported: set video transparency");
    }

    // ==================== List Access ====================

    @Test
    public void testListAccess() throws ParsingException {
        String code = """
                when green flag clicked
                add [thing] to [my list v]
                delete (1) of [my list v]
                insert [thing] at (1) of [my list v]
                replace item (1) of [my list v] with [new thing]
                say (item (1) of [my list v])
                say (item # of [thing] in [my list v])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("add \"thing\" to my_list_list;");
        assertThat(result).contains("delete my_list_list[1];");
        assertThat(result).contains("insert \"thing\" at my_list_list[1];");
        assertThat(result).contains("my_list_list[1] = \"new thing\";");
        assertThat(result).contains("say my_list_list[1];");
        assertThat(result).contains("say \"thing\" in my_list_list;");
    }

    @Test
    public void testUndeclaredVariableOrList() throws ParsingException {
        String code = """
                when green flag clicked
                show variable [unknown var v]
                hide variable [unknown var v]
                show list [unknown list v]
                hide list [unknown list v]
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("# Skipped: show undeclared variable");
        assertThat(result).contains("# Skipped: hide undeclared variable");
        assertThat(result).contains("# Skipped: show undeclared list");
        assertThat(result).contains("# Skipped: hide undeclared list");
    }

    @Test
    public void testNonAsciiIdentifier() throws ParsingException {
        String code = """
                when green flag clicked
                change [Meine_Züge v] by (1)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Goboscript should escape non-ASCII characters
        assertThat(result).contains("Meine_Zuege += 1;");
    }

    // ==================== Numeric Costume/Backdrop Handling ====================

    @Test
    public void testSwitchCostumeNumericLiteral() throws ParsingException {
        String code = """
                when green flag clicked
                switch costume to (1)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Numeric literal 1 should look up costume at index 1 (costume1)
        // emptyProject.json has 2 costumes: costume1, costume2
        assertThat(result).contains("switch_costume \"costume1\";");
    }

    @Test
    public void testSwitchCostumeNumericLiteralWrapping() throws ParsingException {
        String code = """
                when green flag clicked
                switch costume to (3)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Numeric literal 3 with 2 costumes: (3-1) % 2 = 0 -> costume1
        assertThat(result).contains("switch_costume \"costume1\";");
    }

    @Test
    public void testSwitchCostumeNumericExpression() throws ParsingException {
        String code = """
                when green flag clicked
                switch costume to ((0) - (1))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Non-literal numeric expressions (like subtraction) should be commented out
        // since they always produce numbers, not costume names
        assertThat(result).contains("# switch_costume (0 - 1);");
    }

    @Test
    public void testSwitchCostumeVariable() throws ParsingException {
        String code = """
                when green flag clicked
                switch costume to (my variable)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Variables might contain costume names at runtime, so pass them through
        // emptyProject.json has a global variable "my variable"
        assertThat(result).contains("switch_costume my_variable;");
    }

    // ==================== Manual AST Construction Edge Cases ====================

    @Test
    public void testNullMd5extInCostume() {
        // Costume metadata with null md5ext should not throw NPE
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "costume1", null, "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                costumeList,
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        // Should not throw NPE
        visitor.visit(actor);
    }

    @Test
    public void testAllCostumesMissingPlaceholder() {
        // When all costumes are missing, a placeholder should be emitted
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "missing_costume", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                costumeList,
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        // Set available assets to exclude the costume
        visitor.setAvailableAssets(new HashSet<>(List.of("other_md5")));
        visitor.visit(actor);

        String result = visitor.getOutput();

        assertThat(result).contains("sprite Sprite1");
        // Should have a placeholder costume
        assertThat(result).contains("costumes \"other_md5\" as \"placeholder\";");
    }

    // ==================== Costume and Sound Declarations ====================

    @Test
    public void testCostumeAndSounds() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        String result = GoboscriptVisitor.of(program);

        // Stage
        assertThat(result).contains("stage Stage");
        assertThat(result).contains("costumes \"cd21514d0531fdffb22204e0ec5ed84a.svg\" as \"backdrop1\";");
        assertThat(result).contains("sounds \"83a9787d4cb6f3b7632b4ddfebf74367.wav\" as \"pop\";");

        // Sprite1
        assertThat(result).contains("sprite Sprite1");
        assertThat(result).contains("costumes \"b7853f557e4426412e64bb3da6531a99.svg\" as \"costume1\";");
        assertThat(result).contains("costumes \"e6ddc55a6ddd9cc9d84fe0b4c21e016f.svg\" as \"costume2\";");
        assertThat(result).contains("sounds \"83c36d806dc92327b9e7049a565c6bff.wav\" as \"Meow\";");
    }

    // ==================== String Escaping ====================

    @Test
    public void testSanitizerEscapesControlCharacters() {
        // Test basic escapes
        assertThat(GoboscriptSanitizer.escapeStringLiteral("Hello\nWorld")).isEqualTo("Hello\\nWorld");
        assertThat(GoboscriptSanitizer.escapeStringLiteral("Tab\tChar")).isEqualTo("Tab\\tChar");
        assertThat(GoboscriptSanitizer.escapeStringLiteral("Quote\"Here")).isEqualTo("Quote\\\"Here");
        assertThat(GoboscriptSanitizer.escapeStringLiteral("Backslash\\Here")).isEqualTo("Backslash\\\\Here");

        // Test control characters (0x00 - 0x1F)
        assertThat(GoboscriptSanitizer.escapeStringLiteral("\u0000")).isEqualTo("\\u0000");
        assertThat(GoboscriptSanitizer.escapeStringLiteral("\u0001")).isEqualTo("\\u0001");

        // Hex formatting depends on implementation, asserting lower case as typical
        String escaped1F = GoboscriptSanitizer.escapeStringLiteral("\u001F");
        assertThat(escaped1F.equalsIgnoreCase("\\u001f")).isTrue();

        // Test mixed
        assertThat(GoboscriptSanitizer.escapeStringLiteral("Null\u0000Byte")).isEqualTo("Null\\u0000Byte");
    }

    @Test
    public void testVisitorEscapesStringLiteral() {
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        StringLiteral literal = new StringLiteral("Line1\nLine2\u0000End");

        literal.accept(visitor);

        // Visitor wraps in quotes
        assertThat(visitor.getOutput()).isEqualTo("\"Line1\\nLine2\\u0000End\"");
    }

    // ==================== Static of() Method ====================

    @Test
    public void testStaticOf() throws ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("onflag");
        assertThat(result).contains("move");
    }

    // ==================== Helper Methods ====================

    private Program parseProgram(String code) throws ParsingException {
        return parseProgram(code, "Sprite1");
    }

    private Program parseProgram(String code, String targetName) throws ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        return parser.extendProject(baseProject, targetName, code);
    }
}
