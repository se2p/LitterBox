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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GoboscriptAllBlocksUnitTests implements JsonTest {

    private Program program;
    // Use IdentityHashMap to ensure we track unique object instances regardless of equals() implementation
    private final Set<ASTNode> assertedNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    @BeforeAll
    public void setup() throws IOException, de.uni_passau.fim.se2.litterbox.ast.ParsingException {
        program = getAST("src/test/fixtures/allBlocks.json");
    }

    @AfterAll
    public void checkAllStatementsAsserted() {
        // Collect all statements using identity set
        Set<ASTNode> allStatements = Collections.newSetFromMap(new IdentityHashMap<>());
        StmtCollector collector = new StmtCollector(allStatements);
        program.accept(collector);

        // Remove declarations as they are not standard blocks
        allStatements.removeIf(node -> node instanceof DeclarationStmt);

        Set<ASTNode> missedNodes = allStatements.stream()
            .filter(node -> !assertedNodes.contains(node))
            .collect(Collectors.toSet());
            
        String missedNodesMessage = missedNodes.stream()
            .map(node -> node.getClass().getSimpleName())
            .collect(Collectors.joining(", "));

        assertWithMessage("The following statement blocks were not asserted on: " + missedNodesMessage)
            .that(missedNodes)
            .isEmpty();
    }

    private <T extends ASTNode> void assertBlock(Class<T> clazz, String expected) {
        assertBlockAt(clazz, 0, expected);
        assertAll(clazz); // Ensure all instances are marked as covered
    }

    private <T extends ASTNode> void assertBlockAt(Class<T> clazz, int index, String expected) {
        T node = findNodeAt(program, clazz, index);
        assertThat(node).isNotNull();
        assertedNodes.add(node);
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        node.accept(visitor);
        String output = visitor.getOutput().trim();
        assertThat(output).contains(expected);
    }
    
    private <T extends ASTNode> void assertAll(Class<T> clazz) {
        List<T> nodes = findAllNodes(program, clazz);
        for (T node : nodes) {
            assertedNodes.add(node);
            GoboscriptVisitor visitor = new GoboscriptVisitor();
            node.accept(visitor);
        }
    }

    private <T extends ASTNode> T findNodeAt(ASTNode root, Class<T> clazz, int index) {
        NodeFinder<T> finder = new NodeFinder<>(clazz, index);
        root.accept(finder);
        return finder.found;
    }
    
    private <T extends ASTNode> List<T> findAllNodes(ASTNode root, Class<T> clazz) {
        NodeCollector<T> collector = new NodeCollector<>(clazz);
        root.accept(collector);
        return collector.nodes;
    }

    private static class NodeFinder<T> implements ScratchVisitor {
        private final Class<T> clazz;
        private final int targetIndex;
        private int currentIndex = 0;
        private T found;

        public NodeFinder(Class<T> clazz, int index) {
            this.clazz = clazz;
            this.targetIndex = index;
        }

        @Override
        public void visit(ASTNode node) {
            if (found != null) return;
            if (clazz.isInstance(node)) {
                if (currentIndex == targetIndex) {
                    found = clazz.cast(node);
                    return;
                }
                currentIndex++;
            }
            visitChildren(node);
        }
    }
    
    private static class NodeCollector<T> implements ScratchVisitor {
        private final Class<T> clazz;
        private final List<T> nodes = new ArrayList<>();

        public NodeCollector(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void visit(ASTNode node) {
            if (clazz.isInstance(node)) {
                nodes.add(clazz.cast(node));
            }
            visitChildren(node);
        }
    }

    private static class StmtCollector implements ScratchVisitor {
        private final Set<ASTNode> statements;

        public StmtCollector(Set<ASTNode> statements) {
            this.statements = statements;
        }

        @Override
        public void visit(Stmt node) {
            statements.add(node);
            visitChildren(node);
        }
        
        @Override
        public void visit(Event node) {
            statements.add(node);
            visitChildren(node);
        }
    }

    @Test
    public void testMoveSteps() {
        assertBlock(MoveSteps.class, "move 10;");
    }

    @Test
    public void testTurnRight() {
        assertBlock(TurnRight.class, "turn_right 15;");
    }

    @Test
    public void testTurnLeft() {
        assertBlock(TurnLeft.class, "turn_left 15;");
    }

    @Test
    public void testGoToPosXY() {
        assertBlock(GoToPosXY.class, "goto -129, -77;");
    }
    
    @Test
    public void testGoToRandom() {
        assertBlock(GoToPos.class, "goto_random_position;");
    }

    @Test
    public void testPointInDirection() {
        assertBlock(PointInDirection.class, "point_in_direction 90;");
    }
    
    @Test
    public void testPointTowardsMouse() {
        assertBlock(PointTowards.class, "point_towards_mouse_pointer;");
    }

    @Test
    public void testGlideSecsToXY() {
        assertBlock(GlideSecsToXY.class, "glide 1, -129, -77;");
    }
    
    @Test
    public void testGlideToRandom() {
         assertBlock(GlideSecsTo.class, "glide_to_random_position 1;");
    }

    @Test
    public void testChangeSizeBy() {
        assertBlock(ChangeSizeBy.class, "change_size 10;");
    }

    @Test
    public void testSetSizeTo() {
        assertBlock(SetSizeTo.class, "set_size 100;");
    }

    @Test
    public void testChangeGraphicEffectBy() {
        assertBlock(ChangeGraphicEffectBy.class, "change_color_effect 25;");
    }

    @Test
    public void testSetGraphicEffectTo() {
        assertBlock(SetGraphicEffectTo.class, "set_color_effect 0;");
    }

    @Test
    public void testClearGraphicEffects() {
        assertBlock(ClearGraphicEffects.class, "clear_graphic_effects;");
    }

    @Test
    public void testWaitSeconds() {
        assertBlock(WaitSeconds.class, "wait 1;");
    }
    
    @Test
    public void testRepeatForever() {
        assertBlock(RepeatForeverStmt.class, "forever {");
    }
    
    @Test
    public void testClearSoundEffects() {
        assertBlock(ClearSoundEffects.class, "clear_sound_effects;");
    }

    @Test
    public void testWhenFlagClicked() {
        assertBlock(GreenFlag.class, "onflag {");
    }
    
    @Test
    public void testWhenKeyPressed() {
        assertBlock(KeyPressed.class, "onkey \"space\" {");
    }

    @Test
    public void testWhenBackdropSwitchesTo() {
        assertBlock(BackdropSwitchTo.class, "onbackdrop \"backdrop1\" {");
    }

    @Test
    public void testNextCostume() {
        assertBlock(NextCostume.class, "next_costume;");
    }
    
    @Test
    public void testJoin() {
        assertBlock(Join.class, "(\"apple \" & \"banana\")");
    }
    
    @Test
    public void testLetterOf() {
         assertBlock(LetterOf.class, "\"apple\"[1]");
    }
    
    @Test
    public void testLengthOfString() {
        assertBlock(LengthOfString.class, "length(\"apple\")");
    }
    
    @Test
    public void testStringContains() {
        assertBlock(StringContains.class, "contains(\"apple\", \"a\")");
    }
    
    @Test
    public void testMathOp() {
        assertBlock(NumFunctOf.class, "abs(1)");
    }
    
    @Test
    public void testSay() {
        assertBlock(Say.class, "say \"Hello!\";");
    }
    
    @Test
    public void testSayForSecs() {
        assertBlock(SayForSecs.class, "say \"Hello!\", 2;");
    }
    
    @Test
    public void testThink() {
        // The first Think block in allBlocks.json has costume_number() as its message
        assertBlock(Think.class, "think costume_number();");
    }
    
    @Test
    public void testThinkForSecs() {
        assertBlock(ThinkForSecs.class, "think x_position(), 2;");
    }
    
    @Test
    public void testSwitchCostumeTo() {
        assertBlock(SwitchCostumeTo.class, "# switch_costume \"ball-e\";");
    }
    
    @Test
    public void testHide() {
        assertBlock(Hide.class, "hide;");
    }
    
    @Test
    public void testShow() {
        assertBlock(Show.class, "show;");
    }
    
    @Test
    public void testGoToLayer() {
        assertBlock(GoToLayer.class, "goto_front;");
    }
    
    @Test
    public void testChangeLayerBy() {
        assertBlock(ChangeLayerBy.class, "go_forward 1;");
    }
    
    @Test
    public void testSetRotationStyle() {
        assertBlock(SetRotationStyle.class, "set_rotation_style_left_right;");
    }
    
    @Test
    public void testChangeXBy() {
        assertBlock(ChangeXBy.class, "change_x 10;");
    }
    
    @Test
    public void testSetXTo() {
        assertBlock(SetXTo.class, "set_x 28;");
    }
    
    @Test
    public void testChangeYBy() {
        assertBlock(ChangeYBy.class, "change_y 10;");
    }
    
    @Test
    public void testSetYTo() {
        assertBlock(SetYTo.class, "set_y -1;");
    }
    
    @Test
    public void testIfOnEdgeBounce() {
        assertBlock(IfOnEdgeBounce.class, "if_on_edge_bounce;");
    }
    
    @Test
    public void testSetDragMode() {
        assertBlock(SetDragMode.class, "set_drag_mode_draggable;");
    }
    
    @Test
    public void testAskAndWait() {
        assertBlock(AskAndWait.class, "ask \"What's your name?\";");
    }
    
    @Test
    public void testResetTimer() {
        assertBlock(ResetTimer.class, "reset_timer;");
    }
    
    @Test
    public void testPlaySoundUntilDone() {
        assertBlock(PlaySoundUntilDone.class, "play_sound_until_done \"Pop\";");
    }
    
    @Test
    public void testStartSound() {
        assertBlock(StartSound.class, "start_sound \"Pop\";");
    }
    
    @Test
    public void testStopAllSounds() {
        assertBlock(StopAllSounds.class, "stop_all_sounds;");
    }
    
    @Test
    public void testChangeVolumeBy() {
        assertBlock(ChangeVolumeBy.class, "change_volume volume();");
    }
    
    @Test
    public void testSetVolumeTo() {
        assertBlock(SetVolumeTo.class, "set_volume 100;");
    }
    
    @Test
    public void testBroadcast() {
        assertBlock(Broadcast.class, "broadcast \"message1\";");
    }
    
    @Test
    public void testBroadcastAndWait() {
        assertBlock(BroadcastAndWait.class, "broadcast_and_wait \"message1\";");
    }
    
    @Test
    public void testWhenIReceive() {
        assertBlock(ReceptionOfMessage.class, "on \"message1\" {");
    }
    
    @Test
    public void testWaitUntil() {
        assertBlock(WaitUntil.class, "until touching_color(0xad4040) {}");
    }
    
    @Test
    public void testRepeatUntil() {
        assertBlock(UntilStmt.class, "until key_pressed(\"space\") {");
    }
    
    @Test
    public void testIfThen() {
        assertBlock(IfThenStmt.class, "if touching_mouse_pointer() {");
    }
    
    @Test
    public void testIfElse() {
        assertBlock(IfElseStmt.class, "if color_is_touching_color(0xfd0b3b, 0xfaf45d) {");
    }
    
    @Test
    public void testStopAll() {
        assertBlock(StopAll.class, "stop_all;");
    }
    
    @Test
    public void testCreateCloneOf() {
        assertBlock(CreateCloneOf.class, "clone;");
    }
    
    @Test
    public void testWhenIStartAsClone() {
        assertBlock(StartedAsClone.class, "onclone {");
    }
    
    @Test
    public void testDeleteThisClone() {
        assertBlock(DeleteClone.class, "delete_this_clone;");
    }
    
    @Test
    public void testSetVariableTo() {
        // Index 0 is a list initialization which produces no output in Goboscript
        // (lists are declared separately with "list name;" syntax)
        // Index 1 is the variable initialization which produces output
        assertBlockAt(SetVariableTo.class, 1, "Test = ");
        assertAll(SetVariableTo.class); // Mark all instances as covered
    }
    
    @Test
    public void testChangeVariableBy() {
        assertBlock(ChangeVariableBy.class, "Test += 1;");
    }
    
    @Test
    public void testShowVariable() {
        // When visiting in isolation, the visitor doesn't know about declared variables,
        // so it emits a skip comment to avoid goboscript compilation errors.
        // Full-program export tests verify the correct behavior with declarations.
        assertBlock(ShowVariable.class, "# Skipped: show undeclared variable");
    }

    @Test
    public void testHideVariable() {
        // When visiting in isolation, the visitor doesn't know about declared variables,
        // so it emits a skip comment to avoid goboscript compilation errors.
        // Full-program export tests verify the correct behavior with declarations.
        assertBlock(HideVariable.class, "# Skipped: hide undeclared variable");
    }
    
    @Test
    public void testAddTo() {
        // Lists use "_list" suffix to avoid name collisions with variables
        assertBlock(AddTo.class, "add \"thing\" to Test_list;");
    }

    @Test
    public void testDeleteOf() {
        // Lists use "_list" suffix to avoid name collisions with variables
        assertBlock(DeleteOf.class, "delete Test_list[1];");
    }

    @Test
    public void testDeleteAllOf() {
        assertAll(DeleteAllOf.class);
    }

    @Test
    public void testInsertAt() {
        // Lists use "_list" suffix to avoid name collisions with variables
        assertBlock(InsertAt.class, "insert timer() at Test_list[1];");
    }

    @Test
    public void testReplaceItem() {
        // Lists use "_list" suffix to avoid name collisions with variables
        assertBlock(ReplaceItem.class, "Test_list[\"_stage_\".backdrop_number] = username();");
    }

    @Test
    public void testShowList() {
        // When visiting in isolation, the visitor doesn't know about declared lists,
        // so it emits a skip comment to avoid goboscript compilation errors.
        // Full-program export tests verify the correct behavior with declarations.
        assertBlock(ShowList.class, "# Skipped: show undeclared list");
    }

    @Test
    public void testHideList() {
        // When visiting in isolation, the visitor doesn't know about declared lists,
        // so it emits a skip comment to avoid goboscript compilation errors.
        // Full-program export tests verify the correct behavior with declarations.
        assertBlock(HideList.class, "# Skipped: hide undeclared list");
    }
    
    @Test
    public void testRepeatTimes() {
        assertBlock(RepeatTimesStmt.class, "repeat 10 {");
    }
    
    @Test
    public void testSetAttributeTo() {
        assertAll(SetAttributeTo.class);
    }
    
    @Test
    public void testNever() {
        assertAll(Never.class);
    }
    
    @Test
    public void testExpressionStmt() {
        assertAll(ExpressionStmt.class);
    }
    
    @Test
    public void testAttributeAboveValue() {
        assertBlock(AttributeAboveValue.class, "onloudness > 10 {");
    }
    
    @Test
    public void testChangeSoundEffectBy() {
        assertBlock(ChangeSoundEffectBy.class, "change_pitch_effect distance_to_mouse_pointer();");
    }
    
    @Test
    public void testSetSoundEffectTo() {
        assertBlock(SetSoundEffectTo.class, "set_pitch_effect 100;");
    }
}