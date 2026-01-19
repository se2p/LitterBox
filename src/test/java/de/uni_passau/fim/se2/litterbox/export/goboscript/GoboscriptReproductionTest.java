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
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Reproduction tests for edge cases in the GoboscriptVisitor.
 *
 * These tests manually construct AST nodes to test specific edge cases
 * that cannot be easily expressed through scratchblocks parsing.
 */
public class GoboscriptReproductionTest implements JsonTest {

    // ==================== Helper Methods ====================

    private ActorDefinition createMinimalActor(String name) {
        return createMinimalActor(name, ActorType.getSprite());
    }

    private ActorDefinition createMinimalActor(String name, ActorType type) {
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "costume1", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                costumeList,
                new SoundMetadataList(new ArrayList<>())
        );

        return new ActorDefinition(
                type,
                new StrId(name),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );
    }

    private Program createMinimalProgram(ActorDefinition... actors) {
        ProgramMetadata programMetadata = new ProgramMetadata(
                new MonitorMetadataList(new ArrayList<>()),
                new ExtensionMetadata(new ArrayList<>()),
                new MetaMetadata("3.0.0", "0.0.0", "agent")
        );

        List<ActorDefinition> actorList = new ArrayList<>(Arrays.asList(actors));
        return new Program(
                new StrId("Program"),
                new ActorDefinitionList(actorList),
                null,
                null,
                programMetadata
        );
    }

    // ==================== Cross-Sprite Attribute Access ====================

    @Test
    public void testCrossSpriteAttributeAccessFallback() {
        // Reproduces "attempted to codegen Expr::Dot" error
        // Accessing another sprite's attribute is not supported in Goboscript
        ActorDefinition actor = createMinimalActor("Sprite1");
        NoBlockMetadata meta = new NoBlockMetadata();

        // Say ([y position] of [Player 2])
        AttributeOf attrOf = new AttributeOf(
                new AttributeFromFixed(
                        new FixedAttribute(FixedAttribute.FixedAttributeType.Y_POSITION.getType())
                ),
                new WithExpr(new StringLiteral("Player 2"), meta),
                meta
        );

        Say say = new Say(attrOf, meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(actor);
        say.accept(visitor);

        String result = visitor.getOutput();

        // Should produce "0" as a fallback for cross-sprite access
        assertThat(result).contains("say 0;");
        assertThat(result).doesNotContain("\"Player 2\".y");
    }

    @Test
    public void testCrossSpriteDirectionAccess() {
        NoBlockMetadata meta = new NoBlockMetadata();

        // AttributeOf accessing another sprite's direction (not Stage)
        Say say = new Say(
                new AttributeOf(
                        new AttributeFromFixed(
                                new FixedAttribute(FixedAttribute.FixedAttributeType.DIRECTION.getType())
                        ),
                        new WithExpr(new StringLiteral("Ball"), meta),
                        meta
                ),
                meta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        say.accept(visitor);
        String result = visitor.getOutput();

        // Cross-sprite attribute access should be exported as 0 (safe fallback)
        assertThat(result).contains("say 0;");
        assertThat(result).doesNotContain("\"Ball\".direction");
    }

    // ==================== Undefined Procedure Calls ====================

    @Test
    public void testUndefinedProcedureCall() {
        // Calling a procedure that is NOT defined in the current sprite
        ActorDefinition actor = createMinimalActor("Sprite1");
        NoBlockMetadata meta = new NoBlockMetadata();

        CallStmt call = new CallStmt(
                new StrId("take_center"),
                new ExpressionList(new ArrayList<>()),
                meta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(actor);
        call.accept(visitor);

        String result = visitor.getOutput();

        // Should be commented out
        assertThat(result).contains("# Unsupported procedure call (cross-sprite or undefined): take_center;");
    }

    // ==================== Procedure Name Conflicts ====================

    @Test
    public void testProcedureNameConflictWithBuiltinMove() {
        // User procedure named "move" conflicts with Goboscript's built-in "move steps"
        NoBlockMetadata meta = new NoBlockMetadata();

        ProcedureMetadata procMeta = new ProcedureMetadata(meta, meta);
        ProcedureDefinition procDef = new ProcedureDefinition(
                new StrId("move"),
                new ParameterDefinitionList(new ArrayList<>()),
                new StmtList(new ArrayList<>()),
                procMeta
        );

        List<ProcedureDefinition> procs = new ArrayList<>();
        procs.add(procDef);

        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "costume1", "md5", "svg", 1.0, 0, 0));

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(costumes),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(procs),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(actor);
        String result = visitor.getOutput();

        // Procedure definition should be escaped to avoid conflict with built-in "move"
        assertThat(result).contains("proc move_proc {");
        assertThat(result).doesNotContain("proc move {");

        // Test procedure call uses escaped name
        CallStmt callStmt = new CallStmt(
                new StrId("move"),
                new ExpressionList(new ArrayList<>()),
                meta
        );
        callStmt.accept(visitor);
        String callResult = visitor.getOutput();

        assertThat(callResult).contains("move_proc;");
    }

    // ==================== Unspecified Expressions ====================

    @Test
    public void testMoveWithUnspecifiedExpression() {
        NoBlockMetadata meta = new NoBlockMetadata();

        MoveSteps move = new MoveSteps(new UnspecifiedNumExpr(), meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        move.accept(visitor);

        String result = visitor.getOutput();

        // Should use default value
        assertThat(result).contains("move 10;");
    }

    // ==================== Special Number Values ====================

    @Test
    public void testNegativeInfinity() {
        NumberLiteral node = new NumberLiteral(Double.NEGATIVE_INFINITY);
        String result = GoboscriptVisitor.of(node);
        assertThat(result).isEqualTo("(-1.0 / 0.0)");
    }

    @Test
    public void testPositiveInfinity() {
        NumberLiteral node = new NumberLiteral(Double.POSITIVE_INFINITY);
        String result = GoboscriptVisitor.of(node);
        assertThat(result).isEqualTo("(1.0 / 0.0)");
    }

    @Test
    public void testNaN() {
        NumberLiteral node = new NumberLiteral(Double.NaN);
        String result = GoboscriptVisitor.of(node);
        assertThat(result).isEqualTo("(0.0 / 0.0)");
    }

    // ==================== Null/Missing Costume Metadata ====================

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
    public void testMissingCostumeCommentedOut() {
        // Switching to a costume that doesn't exist should be commented out
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "valid_costume", "md5", "svg", 1.0, 0, 0));
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

        NoBlockMetadata meta = new NoBlockMetadata();
        SwitchCostumeTo stmt = new SwitchCostumeTo(
                new WithExpr(new StringLiteral("missing_costume"), meta),
                meta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(actor);
        stmt.accept(visitor);

        String result = visitor.getOutput();

        assertThat(result).contains("costumes \"md5\" as \"valid_costume\";");
        // Missing costume should be commented out
        assertThat(result).contains("# switch_costume \"missing_costume\";");
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

    // ==================== Missing Backdrop ====================

    @Test
    public void testMissingBackdropAndWait() {
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "valid_backdrop", "md5", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                costumeList,
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                new StrId("Stage"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );

        NoBlockMetadata meta = new NoBlockMetadata();
        SwitchBackdropAndWait stmt = new SwitchBackdropAndWait(
                new WithExpr(new StringLiteral("missing_backdrop"), meta),
                meta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stage);
        stmt.accept(visitor);

        String result = visitor.getOutput();

        // Missing backdrop should be commented out
        assertThat(result).contains("# switch_backdrop_and_wait \"missing_backdrop\";");
    }

    // ==================== Switch Costume with Numeric Expression ====================

    @Test
    public void testSwitchCostumeWithNumericExpression() {
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("asset1", "costume1", "md5_1", "svg", 1.0, 0, 0));
        costumes.add(new ImageMetadata("asset2", "costume2", "md5_2", "svg", 1.0, 0, 0));
        costumes.add(new ImageMetadata("asset3", "costume3", "md5_3", "svg", 1.0, 0, 0));
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

        NoBlockMetadata meta = new NoBlockMetadata();
        Minus minusExpr = new Minus(new NumberLiteral(0), new NumberLiteral(1), meta);
        SwitchCostumeTo stmt = new SwitchCostumeTo(new WithExpr(minusExpr, meta), meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setAvailableAssets(new HashSet<>(Arrays.asList("md5_1", "md5_2", "md5_3")));
        visitor.visit(actor);
        stmt.accept(visitor);

        String result = visitor.getOutput();

        // Numeric expression should use modulo for wrapping
        assertThat(result).contains("switch_costume (((0 - 1) - 1) % 3 + 1);");
    }

    // ==================== List Access ====================

    @Test
    public void testItemOfVariableWithQualified() {
        // List access should generate listname[index], not (index)listname
        NoBlockMetadata meta = new NoBlockMetadata();

        // Qualified: Collision.Balls
        StrId actorId = new StrId("Collision");
        StrId listId = new StrId("Balls");
        ScratchList list = new ScratchList(listId, meta);
        Qualified qualified = new Qualified(actorId, list);

        // Index: $a + 1
        Parameter paramA = new Parameter(new StrId("a"), new StringType(), meta);
        Add indexExpr = new Add(new AsNumber(paramA), new NumberLiteral(1), meta);

        // ItemOfVariable: Balls[($a + 1)]
        ItemOfVariable itemOf = new ItemOfVariable(indexExpr, qualified, meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        itemOf.accept(visitor);
        String result = visitor.getOutput();

        // List access should be listname[index]
        assertThat(result).contains("Balls_list[");
        assertThat(result).doesNotContain("]Balls");
    }

    @Test
    public void testIndexOfListElement() {
        // "item # of (expression) in (list)" should output "expression in list_name"
        NoBlockMetadata meta = new NoBlockMetadata();

        StrId actorId = new StrId("Stage");
        StrId listId = new StrId("CODE");
        ScratchList list = new ScratchList(listId, meta);
        Qualified qualifiedList = new Qualified(actorId, list);

        LetterOf letterOf = new LetterOf(new NumberLiteral(1), new StringLiteral("abc"), meta);

        IndexOf indexOf = new IndexOf(letterOf, qualifiedList, meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        indexOf.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains(" in ");
        assertThat(result).contains("CODE_list");
        assertThat(result).isEqualTo("\"abc\"[1] in CODE_list");
    }

    // ==================== Video Sensing Extension ====================

    @Test
    public void testSetVideoTransparencyEmitsComment() {
        NoBlockMetadata meta = new NoBlockMetadata();

        SetVideoTransparency stmt = new SetVideoTransparency(new NumberLiteral(0), meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        stmt.accept(visitor);
        String result = visitor.getOutput();

        // Should emit a comment about unsupported feature
        assertThat(result).contains("# Unsupported: set video transparency");
        assertThat(result).doesNotContain("0;");
    }

    @Test
    public void testTurnVideoEmitsComment() {
        NoBlockMetadata meta = new NoBlockMetadata();

        TurnVideo stmt = new TurnVideo("on", meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        stmt.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("# Unsupported: turn video on/off");
    }

    @Test
    public void testVideoOnInComparison() {
        // VideoOn in comparison operations
        DataBlockMetadata meta = new DataBlockMetadata("id1", null, 0, 0);

        VideoOn videoOn = new VideoOn("motion", "Stage", meta);

        BiggerThan condition = new BiggerThan(videoOn, new NumberLiteral(50), meta);
        StmtList thenStmts = new StmtList(new ArrayList<>());
        IfThenStmt ifStmt = new IfThenStmt(condition, thenStmts, meta);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        ifStmt.accept(visitor);
        String result = visitor.getOutput();

        // VideoOn should output 0 as fallback
        assertThat(result).contains("if (0 > 50)");
    }

    // ==================== Music Extension ====================

    @Test
    public void testTempoInComparison() {
        // Tempo in comparison: if (tempo > 50) { ... }
        DataBlockMetadata meta = new DataBlockMetadata("id1", null, 0, 0);

        Tempo tempo = new Tempo(meta);
        BiggerThan condition = new BiggerThan(tempo, new NumberLiteral(50), meta);
        StmtList thenStmts = new StmtList(new ArrayList<>());
        IfThenStmt ifStmt = new IfThenStmt(condition, thenStmts, meta);

        StmtList eventStmts = new StmtList(new ArrayList<>());
        eventStmts.getStmts().add(ifStmt);

        GreenFlag event = new GreenFlag(meta);
        Script script = new Script(event, eventStmts);
        event.setParentNode(script);
        eventStmts.setParentNode(script);

        ScriptList scripts = new ScriptList(new ArrayList<>());
        scripts.getScriptList().add(script);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                scripts,
                actorMeta
        );

        Program program = createMinimalProgram(sprite);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(sprite);

        String output = visitor.getOutput();

        assertThat(output).contains("if (60 > 50)");
    }

    // ==================== Qualified Access ====================

    @Test
    public void testQualifiedAccess() {
        // In Goboscript, Stage variables are global and accessed directly by name
        StrId stageId = new StrId("Stage");
        StrId varId = new StrId("Leben");
        de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable var =
                new de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable(varId);
        Qualified qualified = new Qualified(stageId, var);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(qualified);
        String result = visitor.getOutput();

        assertThat(result).isEqualTo("Leben");
    }

    // ==================== Undeclared Variable ====================

    @Test
    public void testUndeclaredVariableShowEmitsComment() throws Exception {
        // Corrupted Scratch project referencing a variable that doesn't exist
        Program program = JsonTest.parseProgram("./src/test/fixtures/repro/401097585_23-05-2021_06-38-38.sb3");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Undeclared variable references should be replaced with comments
        assertThat(result).contains("# Skipped: show undeclared variable");
        assertThat(result).contains("# Skipped: hide undeclared variable");

        // Declared variable should still work normally
        assertThat(result).contains("show __World_Record_;");
        assertThat(result).contains("hide __World_Record_;");

        // Undeclared variable name should NOT appear in show/hide statements
        assertThat(result).doesNotContain("show __World_Record;");
        assertThat(result).doesNotContain("hide __World_Record;");
    }

    // ==================== Non-ASCII Identifiers ====================

    @Test
    public void testNonAsciiIdentifier() {
        // Create a declaration with a non-ASCII name "Meine_Züge"
        String varName = "Meine_Züge";
        StrId ident = new StrId(varName);
        Variable variable = new Variable(ident);

        DeclarationIdentAsTypeStmt decl = new DeclarationIdentAsTypeStmt(variable, new NumberType());

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        decl.accept(visitor);

        String result = visitor.getOutput();

        // Output should not contain the original invalid char
        assertThat(result).doesNotContain("ü");
        // And should look somewhat sane (starts with var)
        assertThat(result).startsWith("var ");
    }

    // ==================== Video Event ====================

    @Test
    public void testWhenVideoMotionGreaterThan() {
        DataBlockMetadata meta = new DataBlockMetadata("id1", null, 0, 0);

        // Create WhenVideoMotionGreaterThan(50)
        WhenVideoMotionGreaterThan event = new WhenVideoMotionGreaterThan(new NumberLiteral(50), meta);

        StmtList eventStmts = new StmtList(new ArrayList<>());
        Script script = new Script(event, eventStmts);
        event.setParentNode(script);
        eventStmts.setParentNode(script);

        ScriptList scripts = new ScriptList(new ArrayList<>());
        scripts.getScriptList().add(script);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                scripts,
                actorMeta
        );

        Program program = createMinimalProgram(sprite);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(sprite);

        String output = visitor.getOutput();

        // Should emit a comment instead of invalid code
        assertThat(output).contains("# Unsupported event: when video motion > ...");
        assertThat(output).doesNotContain("\n50");
    }

    // ==================== Switch Backdrop And Wait ====================

    @Test
    public void testSwitchBackdropAndWaitWithSpecialChoices() {
        NoBlockMetadata meta = new NoBlockMetadata();
        List<Stmt> stmtList = new ArrayList<>();

        // switch backdrop to "Blue Sky 1" and wait (missing backdrop)
        stmtList.add(new SwitchBackdropAndWait(
                new WithExpr(new StringLiteral("Blue Sky 1"), meta),
                meta
        ));

        // switch backdrop to (previous backdrop) and wait
        stmtList.add(new SwitchBackdropAndWait(
                new de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev(meta),
                meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // Missing backdrop should be commented out
        assertThat(result).contains("# switch_backdrop_and_wait \"Blue Sky 1\";");
        // Previous backdrop should work (as non-waiting version)
        assertThat(result).contains("previous_backdrop;");
    }

    @Test
    public void testSwitchBackdropAndWaitWithValidBackdrop() {
        // Setup metadata with valid backdrop
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "Backdrop1", "md5ext", "svg", 1.0, 0, 0));

        ImageMetadataList costumeList = new ImageMetadataList(costumes);
        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                costumeList,
                new SoundMetadataList(new ArrayList<>())
        );

        NoBlockMetadata meta = new NoBlockMetadata();
        Script script = new Script(
                new GreenFlag(meta),
                new StmtList(List.of(
                        new SwitchBackdropAndWait(
                                new WithExpr(new StringLiteral("Backdrop1"), meta),
                                meta
                        )
                ))
        );
        script.getEvent().setParentNode(script);

        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                new StrId("Stage"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(List.of(script)),
                actorMeta
        );

        ActorDefinitionList actorList = new ActorDefinitionList(List.of(stage));

        ProgramMetadata progMeta = new ProgramMetadata(
                new MonitorMetadataList(new ArrayList<>()),
                new ExtensionMetadata(new ArrayList<>()),
                new MetaMetadata("1.0", "1.0", "test")
        );

        Program program = new Program(
                new StrId("Program"),
                actorList,
                new SymbolTable(),
                new ProcedureDefinitionNameMapping(),
                progMeta
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(stage);

        String output = visitor.getOutput();

        // Should use switch_backdrop, NOT switch_backdrop_and_wait
        assertThat(output).contains("switch_backdrop \"Backdrop1\";");
        assertThat(output).doesNotContain("switch_backdrop_and_wait");
    }

    // ==================== Global Variable Declaration ====================

    @Test
    public void testGlobalVariableDeclaration() {
        // Create a Stage with a global variable "Score"
        DeclarationIdentAsTypeStmt decl = new DeclarationIdentAsTypeStmt(
                new Variable(new StrId("Score")),
                new NumberType()
        );

        DeclarationStmtList decls = new DeclarationStmtList(new ArrayList<>());
        decls.getDeclarationStmts().add(decl);

        ActorMetadata actorMeta = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                new StrId("Stage"),
                decls,
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMeta
        );

        Program program = createMinimalProgram(stage);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(stage);

        String output = visitor.getOutput();
        assertThat(output).contains("stage Stage");
        assertThat(output).contains("var Score;");
    }
}
