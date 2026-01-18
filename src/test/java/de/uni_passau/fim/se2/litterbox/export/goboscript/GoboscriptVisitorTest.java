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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetDragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;


import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptVisitor} export functionality.
 */
public class GoboscriptVisitorTest implements JsonTest {

    @Test
    public void testMotionBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Verify structure
        assertThat(result).contains("sprite");
        assertThat(result).contains("onflag {");
        assertThat(result).contains("move 10;");
        assertThat(result).contains("turn_right 15;");
        assertThat(result).contains("turn_left 15;");
        assertThat(result).contains("goto");
        assertThat(result).contains("point_in_direction 90;");
    }

    @Test
    public void testControlBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("onflag {");
        assertThat(result).contains("wait 1;");
        assertThat(result).contains("repeat 10 {");
        assertThat(result).contains("if");
        assertThat(result).contains("forever {");
        assertThat(result).contains("stop_all;");
    }

    @Test
    public void testLooksBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/lookblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

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
    public void testSoundBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("play_sound_until_done");
        assertThat(result).contains("start_sound");
        assertThat(result).contains("stop_all_sounds;");
        assertThat(result).contains("change_volume");
        assertThat(result).contains("set_volume");
    }

    @Test
    public void testVariableBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Variable set and change
        assertThat(result).contains("= 0;");
        assertThat(result).contains("+= 1;");

        // List operations
        assertThat(result).contains("add");
        assertThat(result).contains("delete");
    }

    @Test
    public void testArithmeticExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/arithmeticblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Math operators
        assertThat(result).contains("+");
        assertThat(result).contains("-");
        assertThat(result).contains("*");
        assertThat(result).contains("/");
        assertThat(result).contains("round");
        assertThat(result).contains("random");
    }

    @Test
    public void testBooleanExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleanblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Comparison and logic
        assertThat(result).contains(">");
        assertThat(result).contains("<");
        assertThat(result).contains("==");
        assertThat(result).contains("not");
        assertThat(result).contains("and");
        assertThat(result).contains("or");
    }

    @Test
    public void testCloneBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("onclone {");
        assertThat(result).contains("clone;");
        assertThat(result).contains("delete_this_clone;");
    }

    @Test
    public void testMessageBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("on \"message1\"");
        assertThat(result).contains("broadcast");
    }

    @Test
    public void testCustomBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("proc");
        assertThat(result).contains("block1");
        assertThat(result).contains("block2");
    }

    @Test
    public void testStaticOf() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("onflag");
        assertThat(result).contains("move");
    }

    @Test
    public void testSensingStatements() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Statements
        assertThat(result).contains("ask \"What's your name?\";");
        assertThat(result).contains("set_drag_mode_draggable;");
        assertThat(result).contains("reset_timer;");
    }

    @Test
    public void testSensingReporters() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingreporters.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("distance_to_mouse_pointer()");
        assertThat(result).contains("days_since_2000");
        assertThat(result).contains("current_year");
        assertThat(result).contains("contains(");
        assertThat(result).contains("length(");
        assertThat(result).contains("\"apple\"[1]");
    }

    @Test
    public void testGlideBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/glideblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("glide_to_random_position 1;");
        assertThat(result).contains("glide 2, 100, 50;");
    }

    @Test
    public void testCostumeAndSounds() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

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

    @Test
    public void testQualifiedAccess() {
        // In Goboscript, Stage variables are global and accessed directly by name
        // without a "Stage." prefix. The Stage.varName syntax doesn't exist in goboscript.
        StrId stageId = new StrId("Stage");
        StrId varId = new StrId("Leben");
        Variable var = new Variable(varId);
        Qualified qualified = new Qualified(stageId, var);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(qualified);
        String result = visitor.getOutput();

        assertThat(result).isEqualTo("Leben");
    }

    @Test
    public void testFangenExportReproduction() {
        // Manually construct the AST to bypass parser issues
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // 1. set drag mode [not draggable]
        stmtList.add(new SetDragMode(
            new DragMode("not draggable"),
            meta
        ));

        // 2. wait until <touching "Scheibe">
        stmtList.add(new WaitUntil(
            new Touching(
                new SpriteTouchable(
                    new StringLiteral("Scheibe"),
                    meta
                ),
                meta
            ),
            meta
        ));

        // 3. create clone of [myself]
        stmtList.add(new CreateCloneOf(
            new Myself(meta),
            meta
        ));

        // 4. attribute [backdrop #] of [Stage]
        stmtList.add(new Say(
            new AttributeOf(
                new AttributeFromFixed(
                     new FixedAttribute(
                         FixedAttribute.FixedAttributeType.BACKDROP_NUMBER.getType()
                     )
                ),
                new WithExpr(
                    new StringLiteral("Stage"),
                    meta
                ),
                meta
            ),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        assertThat(result).contains("set_drag_mode_not_draggable;");
        assertThat(result).contains("until touching(\"Scheibe\") {}");
        assertThat(result).contains("clone;");
        assertThat(result).contains("\"Stage\".backdrop_number");
    }

    @Test
    public void testSayThinkForSecsReproduction() {
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        
        stmtList.add(new SayForSecs(
            new StringLiteral("Hello"),
            new NumberLiteral(2),
            meta
        ));
        
        stmtList.add(new ThinkForSecs(
            new StringLiteral("Hmm"),
            new NumberLiteral(1.5),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        assertThat(result).contains("say \"Hello\", 2;");
        assertThat(result).contains("think \"Hmm\", 1.5;");
    }
    @Test
    public void testNullPointerExceptionReproduction() {
        // Construct ActorDefinition with nulls in metadata
        
        List<ImageMetadata> costumes = new ArrayList<>();
        // Passing null for referenceID (md5ext) to trigger NPE in escapeString
        costumes.add(new ImageMetadata("assetId", "costume1", null, "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);
        
        List<SoundMetadata> sounds = new ArrayList<>();
        SoundMetadataList soundList = new SoundMetadataList(sounds);
        
        List<CommentMetadata> comments = new ArrayList<>();
        CommentMetadataList commentList = new CommentMetadataList(comments);
        
        ActorMetadata actorMeta = new ActorMetadata(commentList, 0, costumeList, soundList);
        
        // Minimal ActorDefinition
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
        // This should not throw NPE
        visitor.visit(actor);
    }
    @Test
    public void testMissingCostumeReproduction() {
        // Construct ActorDefinition with one valid costume
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "valid_costume", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        List<SoundMetadata> sounds = new ArrayList<>();
        SoundMetadataList soundList = new SoundMetadataList(sounds);

        List<CommentMetadata> comments = new ArrayList<>();
        CommentMetadataList commentList = new CommentMetadataList(comments);

        ActorMetadata actorMeta = new ActorMetadata(commentList, 0, costumeList, soundList);

        // SwitchCostumeTo invalid costume
        List<Stmt> stmts = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        stmts.add(new SwitchCostumeTo(
            new WithExpr(new StringLiteral("missing_costume"), meta),
            meta
        ));

        ActorDefinition actor = new ActorDefinition(
            ActorType.getSprite(),
            new StrId("Sprite1"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(new ArrayList<>()), // Empty scripts
            actorMeta
        );
        
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        // Visit actor to populate validCostumes
        visitor.visit(actor);
        
        // Visit the statement directly
        stmts.get(0).accept(visitor);
        
        String result = visitor.getOutput();
        assertThat(result).contains("costumes \"md5\" as \"valid_costume\";");
        // EXPECTATION: Should be commented out because "missing_costume" is not in validCostumes
        assertThat(result).contains("# switch_costume \"missing_costume\";");
        assertThat(result).doesNotContain("    switch_costume \"missing_costume\";");
    }
    @Test
    public void testAllCostumesMissingReproduction() {
        // Construct ActorDefinition with one costume that is NOT in availableAssets
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "missing_costume", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        List<SoundMetadata> sounds = new ArrayList<>();
        SoundMetadataList soundList = new SoundMetadataList(sounds);

        List<CommentMetadata> comments = new ArrayList<>();
        CommentMetadataList commentList = new CommentMetadataList(comments);

        ActorMetadata actorMeta = new ActorMetadata(commentList, 0, costumeList, soundList);

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
        // EXPECTATION: We should have the placeholder costume emitted
        assertThat(result).contains("costumes \"other_md5\" as \"placeholder\";");
    }

    @Test
    public void testMissingBackdropAndWaitReproduction() {
        // Construct ActorDefinition (Stage) with one valid backdrop
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "valid_backdrop", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);
        List<SoundMetadata> sounds = new ArrayList<>();
        SoundMetadataList soundList = new SoundMetadataList(sounds);
        List<CommentMetadata> comments = new ArrayList<>();
        CommentMetadataList commentList = new CommentMetadataList(comments);
        ActorMetadata actorMeta = new ActorMetadata(commentList, 0, costumeList, soundList);

        // SwitchBackdropAndWait to invalid backdrop
        List<Stmt> stmts = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        stmts.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait(
            new WithExpr(new StringLiteral("missing_backdrop"), meta),
            meta
        ));

        ActorDefinition stage = new ActorDefinition(
            ActorType.getStage(),
            new StrId("Stage"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(new ArrayList<>()), // Empty scripts
            actorMeta
        );
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        // Visit stage to populate available backdrops/costumes
        visitor.visit(stage);
        
        // Visit the statement directly
        stmts.get(0).accept(visitor);
        
        String result = visitor.getOutput();
        // EXPECTATION: Should be commented out with # because "missing_backdrop" is not in validCostumes
        assertThat(result).contains("# switch_backdrop_and_wait \"missing_backdrop\";");
        assertThat(result).doesNotContain("// switch_backdrop_and_wait \"missing_backdrop\";");
    }

    @Test
    public void testGlideToSpriteAndAttributeOfReproduction() {
        // Reproduces errors from 400008389_13-05-2021_15-13-23.sb3
        // 1. glide_to "Ball", 3; - Goboscript doesn't support glide_to with sprite target
        // 2. "Ball".direction - Cross-sprite attribute access not supported
        
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // 1. GlideSecsTo with sprite target (FromExpression)
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo(
            new NumberLiteral(3),
            new de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression(
                new StringLiteral("Ball"),
                meta
            ),
            meta
        ));
        
        // 2. AttributeOf accessing another sprite's direction (not Stage)
        stmtList.add(new Say(
            new AttributeOf(
                new AttributeFromFixed(
                     new FixedAttribute(
                         FixedAttribute.FixedAttributeType.DIRECTION.getType()
                     )
                ),
                new WithExpr(
                    new StringLiteral("Ball"),
                    meta
                ),
                meta
            ),
            meta
        ));
        
        // 3. AttributeOf accessing Stage (should remain uncommented)
        stmtList.add(new Say(
            new AttributeOf(
                new AttributeFromFixed(
                     new FixedAttribute(
                         FixedAttribute.FixedAttributeType.BACKDROP_NUMBER.getType()
                     )
                ),
                new WithExpr(
                    new StringLiteral("Stage"),
                    meta
                ),
                meta
            ),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();
        
        // 1. glide_to with sprite should use goto with comment
        assertThat(result).contains("goto \"Ball\"; # was: glide_to 3s");
        assertThat(result).doesNotContain("glide_to \"Ball\", 3;");
        
        // 2. AttributeOf with non-Stage sprite should be exported as 0 (safe fallback)
        assertThat(result).contains("say 0;");
        assertThat(result).doesNotContain("\"Ball\".direction");
        
        // 3. AttributeOf with Stage should remain working
        assertThat(result).contains("say \"Stage\".backdrop_number;");
    }

    @Test
    public void testItemOfVariableWithQualified() {
        // Reproduces issue from 400011212_13-05-2021_17-10-38.sb3
        // where list item access generates "(index)listname" instead of "listname[index]"
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // Create identifier: Collision.Balls (Qualified node with ScratchList)
        StrId actorId = new StrId("Collision");
        StrId listId = new StrId("Balls");
        de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList list = 
            new de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList(listId, meta);
        Qualified qualified = new Qualified(actorId, list);
        
        // Create index expression: $a + 1 (parameter + number)
        de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter paramA = 
            new de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter(
                new StrId("a"),
                new de.uni_passau.fim.se2.litterbox.ast.model.type.StringType(),
                meta
            );
        de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add indexExpr = 
            new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add(
                new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber(paramA),
                new NumberLiteral(1),
                meta
            );
        
        // Create ItemOfVariable: Balls[($a + 1)]
        de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable itemOf = 
            new de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable(
                indexExpr,
                qualified,
                meta
            );
        
        // Visit the expression directly
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        itemOf.accept(visitor);
        String result = visitor.getOutput();
        
        // List access should be listname[index], not (index)listname
        // Expected: Balls_list[...] (the list name with _list suffix first, then brackets with index)
        // Lists use "_list" suffix to avoid name collisions with variables
        assertThat(result).contains("Balls_list[");
        assertThat(result).doesNotContain("]Balls");
        assertThat(result).doesNotContain("Balls_list ["); // No space before bracket
    }

    @Test
    public void testSwitchCostumeWithNumericExpression() {
        // Construct ActorDefinition with 3 costumes
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("asset1", "costume1", "md5_1", "svg", 1.0, 0, 0));
        costumes.add(new ImageMetadata("asset2", "costume2", "md5_2", "svg", 1.0, 0, 0));
        costumes.add(new ImageMetadata("asset3", "costume3", "md5_3", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        ActorMetadata actorMeta = new ActorMetadata(new CommentMetadataList(new ArrayList<>()), 0, costumeList, new SoundMetadataList(new ArrayList<>()));

        ActorDefinition actor = new ActorDefinition(
            ActorType.getSprite(),
            new StrId("Sprite1"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(new ArrayList<>()),
            actorMeta
        );
        
        // Statement: switch_costume ( - 1 )
        // Using Minus(0, 1) or just NumberLiteral(-1)
        NoBlockMetadata meta = new NoBlockMetadata();
        de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus minusExpr = 
            new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus(
                new NumberLiteral(0),
                new NumberLiteral(1),
                meta
            );
            
        SwitchCostumeTo stmt = new SwitchCostumeTo(
            new WithExpr(minusExpr, meta),
            meta
        );
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        // Set available assets so all 3 costumes are valid
        visitor.setAvailableAssets(new HashSet<>(Arrays.asList("md5_1", "md5_2", "md5_3")));
        
        visitor.visit(actor); // populate validCostumes (size=3)
        
        stmt.accept(visitor);
        
        String result = visitor.getOutput();
        // Expect: switch_costume (((0 - 1) - 1) % 3 + 1);
        // "0 - 1" comes from Minus.accept which adds parens: (0 - 1)
        assertThat(result).contains("switch_costume (((0 - 1) - 1) % 3 + 1);");
    }

    @Test
    public void testBackdropAndCostumeReporters() {
        NoBlockMetadata meta = new NoBlockMetadata();

        // backdrop [number]
        Backdrop backdropNum = new Backdrop(new NameNum("number"), meta);
        GoboscriptVisitor v1 = new GoboscriptVisitor();
        backdropNum.accept(v1);
        assertThat(v1.getOutput()).isEqualTo("backdrop_number()");

        // backdrop [name]
        Backdrop backdropName = new Backdrop(new NameNum("name"), meta);
        GoboscriptVisitor v2 = new GoboscriptVisitor();
        backdropName.accept(v2);
        assertThat(v2.getOutput()).isEqualTo("backdrop_name()");

        // costume [number]
        Costume costumeNum = new Costume(new NameNum("number"), meta);
        GoboscriptVisitor v3 = new GoboscriptVisitor();
        costumeNum.accept(v3);
        assertThat(v3.getOutput()).isEqualTo("costume_number()");

        // costume [name]
        Costume costumeName = new Costume(new NameNum("name"), meta);
        GoboscriptVisitor v4 = new GoboscriptVisitor();
        costumeName.accept(v4);
        assertThat(v4.getOutput()).isEqualTo("costume_name()");
    }
}
