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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.KeySetPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the KeySetPosition bug.
 *
 * <p>The bug is reported when an arrow key event directly calls SetX/SetY (absolute positioning)
 * instead of ChangeX/ChangeY (relative movement). This fix heuristic checks if the problematic
 * SetX/SetY block is no longer in a context that triggers the bug:
 * <ul>
 *   <li>The arrow key was changed to a non-arrow key</li>
 *   <li>The block is no longer directly inside the key event handler</li>
 *   <li>The block was replaced with a different statement type (or deleted)</li>
 * </ul>
 */
public class KeySetPositionFix extends AbstractFixHeuristic {

    /**
     * Sentinel value indicating no arrow key context.
     */
    private static final int NO_ARROW_KEY = -1;

    /**
     * Current arrow key context (-1 if not in arrow key context).
     */
    private int currentArrowKey = NO_ARROW_KEY;

    /**
     * Whether we're inside an arrow key event or if-key-pressed block.
     */
    private boolean isInKeyContext = false;

    private final KeySetPosition relatedBugFinder = new KeySetPosition();

    public KeySetPositionFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    protected void resetState() {
        currentArrowKey = NO_ARROW_KEY;
        isInKeyContext = false;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Optional<BlockIdNodeFinder.NodeContext> contextOpt = initializeAndCheckBlockDeletion(program);

        // If block was deleted, fix already added
        if (!fixes.isEmpty()) {
            return Collections.unmodifiableSet(fixes);
        }

        // Check if block type changed (e.g., SetX replaced with ChangeX)
        if (contextOpt.isPresent()) {
            BlockIdNodeFinder.NodeContext context = contextOpt.get();
            ASTNode node = context.node();

            if (!(node instanceof SetXTo) && !(node instanceof SetYTo)) {
                // Block was replaced with a different type - fixed!
                setContext(context);
                addFix(node, node.getMetadata());
                return Collections.unmodifiableSet(fixes);
            }
        }

        // Traverse to check if context changed (no longer in arrow key handler)
        refreshBugInstances(program);
        program.accept(this);

        return Collections.unmodifiableSet(fixes);
    }

    /**
     * Sets the actor/script/procedure context from the NodeContext.
     */
    private void setContext(BlockIdNodeFinder.NodeContext context) {
        this.currentActor = context.actor();
        this.currentScript = context.script();
        this.currentProcedure = context.procedure();
    }

    @Override
    public void visit(Script node) {
        currentArrowKey = NO_ARROW_KEY;
        isInKeyContext = false;

        if (node.getEvent() instanceof KeyPressed keyPressed
                && !node.getStmtList().getStmts().isEmpty()) {
            int arrowKey = extractArrowKey(keyPressed.getKey());
            if (arrowKey != NO_ARROW_KEY) {
                currentArrowKey = arrowKey;
                isInKeyContext = true;
            }
        }

        visitChildren(node);

        isInKeyContext = false;
        currentArrowKey = NO_ARROW_KEY;
    }

    @Override
    public void visit(IfThenStmt node) {
        int savedArrowKey = currentArrowKey;
        boolean savedInKeyContext = isInKeyContext;

        // Check for if(key pressed?) with arrow key
        if (node.getBoolExpr() instanceof IsKeyPressed isKeyPressed) {
            int arrowKey = extractArrowKey(isKeyPressed.getKey());
            if (arrowKey != NO_ARROW_KEY) {
                currentArrowKey = arrowKey;
                isInKeyContext = true;
            }
        }

        checkFirstTwoStatements(node.getThenStmts().getStmts());
        visitChildren(node);

        currentArrowKey = savedArrowKey;
        isInKeyContext = savedInKeyContext;
    }

    @Override
    public void visit(SetXTo node) {
        if (matchesBugLocation(node)) {
            checkIfFixed(node, true);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetYTo node) {
        if (matchesBugLocation(node)) {
            checkIfFixed(node, false);
        }
        visitChildren(node);
    }

    /**
     * Checks the first two statements for SetX/SetY at bug location.
     * The bug finder only reports on statements at the start of the block.
     */
    private void checkFirstTwoStatements(List<Stmt> stmts) {
        for (int i = 0; i < stmts.size() && i < 2; i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof SetXTo setX && matchesBugLocation(setX)) {
                checkIfFixed(setX, true);
            } else if (stmt instanceof SetYTo setY && matchesBugLocation(setY)) {
                checkIfFixed(setY, false);
            }
        }
    }

    /**
     * Checks if the bug is fixed for the given SetX/SetY node.
     *
     * <p>The bug triggers when:
     * <ul>
     *   <li>SetY is used with UP/DOWN arrow keys</li>
     *   <li>SetX is used with LEFT/RIGHT arrow keys</li>
     * </ul>
     *
     * <p>So the bug is fixed if we're not in a matching arrow key context.
     *
     * @param node   the SetX/SetY node
     * @param isSetX true if SetXTo, false if SetYTo
     */
    private void checkIfFixed(Stmt node, boolean isSetX) {
        if (!isInKeyContext) {
            addFix(node, node.getMetadata());
            return;
        }

        boolean isBuggyContext = isSetX
                ? isHorizontalArrowKey(currentArrowKey)
                : isVerticalArrowKey(currentArrowKey);

        if (!isBuggyContext) {
            addFix(node, node.getMetadata());
        }
    }

    /**
     * Extracts the arrow key code from a Key node, or returns NO_ARROW_KEY if not an arrow key.
     */
    private int extractArrowKey(Key keyNode) {
        if (keyNode.getKey() instanceof NumberLiteral keyNum) {
            int key = (int) keyNum.getValue();
            if (isArrowKey(key)) {
                return key;
            }
        }
        return NO_ARROW_KEY;
    }

    private boolean isArrowKey(int key) {
        return isHorizontalArrowKey(key) || isVerticalArrowKey(key);
    }

    private boolean isHorizontalArrowKey(int key) {
        return key == KeyCode.LEFT_ARROW.getKeycode()
                || key == KeyCode.RIGHT_ARROW.getKeycode();
    }

    private boolean isVerticalArrowKey(int key) {
        return key == KeyCode.UP_ARROW.getKeycode()
                || key == KeyCode.DOWN_ARROW.getKeycode();
    }
}
