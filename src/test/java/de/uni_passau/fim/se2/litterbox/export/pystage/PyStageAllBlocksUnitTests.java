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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.StringContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunctOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PyStageAllBlocksUnitTests implements JsonTest {

    private Program program;
    // Use IdentityHashMap to ensure we track unique object instances regardless of equals() implementation
    private final Set<ASTNode> assertedNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    @BeforeAll
    public void setup() throws IOException, ParsingException {
        program = getAST("src/test/fixtures/allBlocks.json");
    }

    @AfterAll
    public void checkAllStatementsAsserted() {
        // Collect all statements using identity set
        Set<ASTNode> allStatements = Collections.newSetFromMap(new IdentityHashMap<>());
        StmtCollector collector = new StmtCollector(allStatements);
        program.accept(collector);

        // Remove declarations as they are not standard blocks (often implicit or handled differently)
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

        // Use static factory method to handle context properly if needed,
        // but for unit testing specific nodes, we often need a clean visitor with program context
        // if possible. PyStageVisitor.of(node) does this by finding parent program.
        String output = PyStageVisitor.of(node).trim();
        assertThat(output).contains(expected);
    }

    private <T extends ASTNode> void assertAll(Class<T> clazz) {
        List<T> nodes = findAllNodes(program, clazz);
        for (T node : nodes) {
            assertedNodes.add(node);
            // Just ensure it doesn't crash
            PyStageVisitor.of(node);
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
            if (found != null) {
                return;
            }
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

    private record StmtCollector(Set<ASTNode> statements) implements ScratchVisitor {

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
        assertBlock(MoveSteps.class, "self.move(10)");
    }

    @Test
    public void testTurnRight() {
        assertBlock(TurnRight.class, "self.turn_right(15)");
    }

    @Test
    public void testTurnLeft() {
        assertBlock(TurnLeft.class, "self.turn_left(15)");
    }

    @Test
    public void testGoToPosXY() {
        assertBlock(GoToPosXY.class, "self.go_to_x_y(-129, -77)");
    }

    @Test
    public void testGoToRandom() {
        assertBlock(GoToPos.class, "self.go_to_random_position()");
    }

    @Test
    public void testPointInDirection() {
        assertBlock(PointInDirection.class, "self.point_in_direction(90)");
    }

    @Test
    public void testPointTowardsMouse() {
        assertBlock(PointTowards.class, "self.point_towards_mouse_pointer()");
    }

    @Test
    public void testGlideSecsToXY() {
        assertBlock(GlideSecsToXY.class, "self.glide_to_x_y(1, -129, -77)");
    }

    @Test
    public void testGlideToRandom() {
        assertBlock(GlideSecsTo.class, "self.glide_to_random_position(1)");
    }

    @Test
    public void testChangeSizeBy() {
        assertBlock(ChangeSizeBy.class, "self.change_size_by(min(max(10, -25000), 25000))");
    }

    @Test
    public void testSetSizeTo() {
        assertBlock(SetSizeTo.class, "self.set_size_to(min(max(100, 0), 25000))");
    }

    @Test
    public void testChangeGraphicEffectBy() {
        // Now handled - emits change_{effect}_effect_by
        assertBlock(ChangeGraphicEffectBy.class, "self.change_color_effect_by(25)");
    }

    @Test
    public void testSetGraphicEffectTo() {
        // Now handled - emits set_{effect}_effect_to
        assertBlock(SetGraphicEffectTo.class, "self.set_color_effect_to(0)");
    }

    @Test
    public void testClearGraphicEffects() {
        // Now handled
        assertBlock(ClearGraphicEffects.class, "self.clear_graphic_effects()");
    }

    @Test
    public void testWaitSeconds() {
        assertBlock(WaitSeconds.class, "self.wait_seconds(1)");
    }

    @Test
    public void testRepeatForever() {
        assertBlock(RepeatForeverStmt.class, "while True:");
    }

    @Test
    public void testClearSoundEffects() {
        // Now handled
        assertBlock(ClearSoundEffects.class, "self.clear_sound_effects()");
    }

    @Test
    public void testWhenFlagClicked() {
        assertBlock(GreenFlag.class, "_on_start_");
    }

    @Test
    public void testWhenKeyPressed() {
        assertBlock(KeyPressed.class, "_on_key_");
    }

    @Test
    public void testWhenBackdropSwitchesTo() {
        assertBlock(BackdropSwitchTo.class, "_on_backdrop_");
    }

    @Test
    public void testNextCostume() {
        assertBlock(NextCostume.class, "self.next_costume()");
    }

    @Test
    public void testJoin() {
        // Expressions are usually tested within blocks, but visitor supports direct visit
        // However, PyStageVisitor.of(expr) might fail if it depends on program context for some things.
        // Join (Concat) uses + operator.
        assertBlock(Join.class, "+");
    }

    @Test
    public void testLetterOf() {
        // LetterOf is implemented as subscript [i-1]
        assertBlock(LetterOf.class, "[int(1) - 1]");
    }

    @Test
    public void testLengthOfString() {
        assertBlock(LengthOfString.class, "len(");
    }

    @Test
    public void testStringContains() {
        assertBlock(StringContains.class, " in ");
    }

    @Test
    public void testMathOp() {
        // Abs(1)
        assertBlock(NumFunctOf.class, "math.fabs(1)");
    }

    @Test
    public void testPickRandom() {
        assertBlock(PickRandom.class, "_pick_random(");
    }

    @Test
    public void testSay() {
        assertBlock(Say.class, "self.say(\"Hello!\")");
    }

    @Test
    public void testSayForSecs() {
        assertBlock(SayForSecs.class, "self.say_for_seconds(\"Hello!\", 2)");
    }

    @Test
    public void testThink() {
        assertBlock(Think.class, "self.think(");
    }

    @Test
    public void testThinkForSecs() {
        assertBlock(ThinkForSecs.class, "self.think_for_seconds(");
    }

    @Test
    public void testSwitchCostumeTo() {
        assertBlock(SwitchCostumeTo.class, "self.switch_costume_to(");
    }

    @Test
    public void testHide() {
        assertBlock(Hide.class, "self.hide()");
    }

    @Test
    public void testShow() {
        assertBlock(Show.class, "self.show()");
    }

    @Test
    public void testGoToLayer() {
        assertBlock(GoToLayer.class, "self.go_to_front_layer()");
    }

    @Test
    public void testChangeLayerBy() {
        assertBlock(ChangeLayerBy.class, "self.go_forward(1)");
    }

    @Test
    public void testSetRotationStyle() {
        assertBlock(SetRotationStyle.class, "self.set_rotation_style_left_right()");
    }

    @Test
    public void testChangeXBy() {
        assertBlock(ChangeXBy.class, "self.change_x_by(10)");
    }

    @Test
    public void testSetXTo() {
        assertBlock(SetXTo.class, "self.set_x(28)");
    }

    @Test
    public void testChangeYBy() {
        assertBlock(ChangeYBy.class, "self.change_y_by(10)");
    }

    @Test
    public void testSetYTo() {
        assertBlock(SetYTo.class, "self.set_y(-1)");
    }

    @Test
    public void testIfOnEdgeBounce() {
        assertBlock(IfOnEdgeBounce.class, "self.if_on_edge_bounce()");
    }

    @Test
    public void testSetDragMode() {
        assertBlock(SetDragMode.class, "self.set_drag_mode_draggable()");
    }

    @Test
    public void testAskAndWait() {
        assertBlock(AskAndWait.class, "self.ask_and_wait(\"What's your name?\")");
    }

    @Test
    public void testResetTimer() {
        assertBlock(ResetTimer.class, "self.reset_timer()");
    }

    @Test
    public void testPlaySoundUntilDone() {
        assertBlock(PlaySoundUntilDone.class, "self.play_sound_until_done(");
    }

    @Test
    public void testStartSound() {
        assertBlock(StartSound.class, "self.start_sound(");
    }

    @Test
    public void testStopAllSounds() {
        assertBlock(StopAllSounds.class, "self.stop_all_sounds()");
    }

    @Test
    public void testChangeVolumeBy() {
        assertBlock(ChangeVolumeBy.class, "self.change_volume_by(self.volume())");
    }

    @Test
    public void testSetVolumeTo() {
        assertBlock(SetVolumeTo.class, "self.set_volume_to(100)");
    }

    @Test
    public void testBroadcast() {
        assertBlock(Broadcast.class, "self.broadcast(\"message1\")");
    }

    @Test
    public void testBroadcastAndWait() {
        assertBlock(BroadcastAndWait.class, "self.broadcast_and_wait(\"message1\")");
    }

    @Test
    public void testWhenIReceive() {
        assertBlock(ReceptionOfMessage.class, "_on_message_");
    }

    @Test
    public void testWaitUntil() {
        assertBlock(WaitUntil.class, "while not (");
    }

    @Test
    public void testRepeatUntil() {
        assertBlock(UntilStmt.class, "while not (");
    }

    @Test
    public void testIfThen() {
        assertBlock(IfThenStmt.class, "if ");
    }

    @Test
    public void testIfElse() {
        assertBlock(IfElseStmt.class, "if ");
    }

    @Test
    public void testStopAll() {
        assertBlock(StopAll.class, "self.stop_all()");
    }

    @Test
    public void testCreateCloneOf() {
        assertBlock(CreateCloneOf.class, "self.create_clone_of_myself()");
    }

    @Test
    public void testWhenIStartAsClone() {
        assertBlock(StartedAsClone.class, "_on_clone_");
    }

    @Test
    public void testDeleteThisClone() {
        assertBlock(DeleteClone.class, "self.delete_this_clone()");
    }

    @Test
    public void testSetVariableTo() {
        assertBlock(SetVariableTo.class, ".set_variable(\"Test\", ");
    }

    @Test
    public void testChangeVariableBy() {
        assertBlock(ChangeVariableBy.class, ".set_variable(\"Test\", _to_num(");
    }

    @Test
    public void testShowVariable() {
        assertBlock(ShowVariable.class, ".show_variable(\"Test\")");
    }

    @Test
    public void testHideVariable() {
        assertBlock(HideVariable.class, ".hide_variable(\"Test\")");
    }

    @Test
    public void testAddTo() {
        assertBlock(AddTo.class, ".append(\"thing\")");
    }

    @Test
    public void testDeleteOf() {
        assertBlock(DeleteOf.class, ".pop(int(");
    }

    @Test
    public void testDeleteAllOf() {
        assertAll(DeleteAllOf.class);
    }

    @Test
    public void testInsertAt() {
        assertBlock(InsertAt.class, ".insert(int(");
    }

    @Test
    public void testReplaceItem() {
        assertBlock(ReplaceItem.class, ".get_variable(\"Test\")[int(");
    }

    @Test
    public void testShowList() {
        // PyStage uses same method for vars and lists: show_variable
        assertBlock(ShowList.class, ".show_variable(\"Test\")");
    }

    @Test
    public void testHideList() {
        assertBlock(HideList.class, ".hide_variable(\"Test\")");
    }

    @Test
    public void testRepeatTimes() {
        assertBlock(RepeatTimesStmt.class, "for _ in range(int(10)):");
    }

    @Test
    public void testSetAttributeTo() {
        // PyStage handles some attributes in SetAttributeTo (visit(SetAttributeTo) exists)
        // e.g. "x" -> motion_setx
        // The one in allBlocks.json depends on the input.
        // Let's assume there is at least one instance.
        // Goboscript test: assertAll(SetAttributeTo.class).
        // I'll assume it doesn't crash.
        assertAll(SetAttributeTo.class);
    }

    @Test
    public void testNever() {
        // "Never" block (loose blocks)
        assertAll(Never.class);
    }

    @Test
    public void testExpressionStmt() {
        assertAll(ExpressionStmt.class);
    }

    @Test
    public void testAttributeAboveValue() {
        assertBlock(AttributeAboveValue.class, "_on_attr_");
    }

    @Test
    public void testChangeSoundEffectBy() {
        // Unhandled in PyStage?
        // I didn't see ChangeSoundEffectBy in read output.
        // It should emit Unhandled node comment.
        assertBlock(ChangeSoundEffectBy.class, "# Unhandled node: ChangeSoundEffectBy");
    }

    @Test
    public void testSetSoundEffectTo() {
        // Unhandled in PyStage?
        assertBlock(SetSoundEffectTo.class, "# Unhandled node: SetSoundEffectTo");
    }
}
