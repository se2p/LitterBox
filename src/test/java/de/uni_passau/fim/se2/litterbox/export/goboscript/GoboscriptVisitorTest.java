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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptVisitor} export functionality.
 *
 * This test class uses the ScratchBlocks parsing approach where possible,
 * following the pattern established in PyStageVisitorTest.
 */
public class GoboscriptVisitorTest implements JsonTest {

    // ==================== Motion Blocks ====================

    @Test
    public void testMotionBlocks() throws IOException, ParsingException {
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
    public void testGlideBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/glideblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("glide_to_random_position 1;");
        assertThat(result).contains("glide 2, 100, 50;");
    }

    @Test
    public void testGlideToSprite() throws IOException, ParsingException {
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
    public void testControlBlocks() throws IOException, ParsingException {
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
    public void testWaitUntil() throws IOException, ParsingException {
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
    public void testLooksBlocks() throws IOException, ParsingException {
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
    public void testSayForSecs() throws IOException, ParsingException {
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
    public void testSoundBlocks() throws IOException, ParsingException {
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
    public void testVariableBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("= 0;");
        assertThat(result).contains("+= 1;");
        assertThat(result).contains("add");
        assertThat(result).contains("delete");
    }

    @Test
    public void testGlobalVariableAccess() throws IOException, ParsingException {
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
    public void testArithmeticExpressions() throws IOException, ParsingException {
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
    public void testMathCeiling() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                set x to ([ceiling v] of (3.2))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("ceil(3.2)");
    }

    @Test
    public void testDaysSince2000() throws IOException, ParsingException {
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
    public void testBooleanExpressions() throws IOException, ParsingException {
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
    public void testTouchingColor() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                wait until <touching color (#ff0000)?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("touching_color(0xff0000)");
    }

    @Test
    public void testColorIsTouchingColor() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                wait until <color (#ff0000) is touching (#00ff00)?>
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("color_is_touching_color(0xff0000, 0x00ff00)");
    }

    @Test
    public void testMouseDown() throws IOException, ParsingException {
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
    public void testCloneBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("onclone {");
        assertThat(result).contains("clone;");
        assertThat(result).contains("delete_this_clone;");
    }

    @Test
    public void testCreateCloneOfMyself() throws IOException, ParsingException {
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
    public void testMessageBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("on \"message1\"");
        assertThat(result).contains("broadcast");
    }

    // ==================== Custom Blocks ====================

    @Test
    public void testCustomBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("proc");
        assertThat(result).contains("block1");
        assertThat(result).contains("block2");
    }

    // ==================== Sensing Blocks ====================

    @Test
    public void testSensingStatements() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("ask \"What's your name?\";");
        assertThat(result).contains("set_drag_mode_draggable;");
        assertThat(result).contains("reset_timer;");
    }

    @Test
    public void testSensingReporters() throws IOException, ParsingException {
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
    public void testUndefinedProcedureCall() throws IOException, ParsingException {
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
    public void testProcedureNameConflict() throws IOException, ParsingException {
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

    // ==================== Qualified Access ====================

    @Test
    public void testQualifiedVariableAccess() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say ([Score v] of (Stage v))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Stage variables are global in Goboscript, but AttributeOf currently emits qualification
        assertThat(result).contains("say \"Stage\".Score;");
    }

    // ==================== Other Sensing Repoters ====================

    @Test
    public void testOtherSensingReporters() throws IOException, ParsingException {
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
    public void testJoin() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say (join [Hello ] [World])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("(\"Hello \" & \"World\")");
    }

    @Test
    public void testNestedJoin() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say (join (join [A] [B]) [C])
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("((\"A\" & \"B\") & \"C\")");
    }

    @Test
    public void testLetterOf() throws IOException, ParsingException {
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
    public void testBackdropSwitch() throws IOException, ParsingException {
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
    public void testBackdropAndCostumeReporters() throws IOException, ParsingException {
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
    public void testGraphicEffects() throws IOException, ParsingException {
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
    public void testAttributeOfStage() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say ([backdrop # v] of (Stage v))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        assertThat(result).contains("\"Stage\".backdrop_number");
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

        // Accessing non-Stage sprite attributes is not supported, should fall back to 0
        assertThat(result).contains("say 0;");
        assertThat(result).doesNotContain("\"OtherSprite\".x");
    }

    // ==================== Special Number Values ====================

    @Test
    public void testSpecialNumberValues() throws IOException, ParsingException {
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
    public void testListAccess() throws IOException, ParsingException {
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
    public void testUndeclaredVariableOrList() throws IOException, ParsingException {
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
    public void testNonAsciiIdentifier() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                change [Meine_Züge v] by (1)
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // Goboscript should escape non-ASCII characters
        assertThat(result).contains("Meine_Zuege += 1;");
    }

    // ==================== Numeric Index Wrapping ====================

    @Test
    public void testSwitchCostumeNumericWrapping() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                switch costume to ((0) - (1))
                """;
        Program program = parseProgram(code);
        String result = GoboscriptVisitor.of(program);

        // The emptyProject.json has 2 costumes for Sprite1
        assertThat(result).contains("switch_costume (((0 - 1) - 1) % 2 + 1);");
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
    public void testCostumeAndSounds() throws IOException, ParsingException {
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

    // ==================== Static of() Method ====================

    @Test
    public void testStaticOf() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("onflag");
        assertThat(result).contains("move");
    }

    // ==================== Helper Methods ====================

    private Program parseProgram(String code) throws IOException, ParsingException {
        return parseProgram(code, "Sprite1");
    }

    private Program parseProgram(String code, String targetName) throws IOException, ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        return parser.extendProject(baseProject, targetName, code);
    }
}
