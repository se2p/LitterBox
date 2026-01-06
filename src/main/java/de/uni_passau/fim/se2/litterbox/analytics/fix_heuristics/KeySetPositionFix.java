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

import java.util.*;

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

    // Context tracking
    private int currentArrowKey = -1;
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
        currentArrowKey = -1;
        isInKeyContext = false;
    }

    @Override
    public Set<FixResult> check(Program program) {
        this.program = program;
        this.fixes = new LinkedHashSet<>();
        resetState();

        // Handle deleted blocks
        if (getBugLocationBlockId() != null && getBugLocationBlockId().startsWith("__")) {
            addFix(null, null);
            return Collections.unmodifiableSet(fixes);
        }

        Optional<BlockIdNodeFinder.NodeContext> contextOpt = BlockIdNodeFinder.find(program, getBugLocationBlockId());

        if (contextOpt.isPresent()) {
            BlockIdNodeFinder.NodeContext context = contextOpt.get();
            ASTNode node = context.node();

            // If the node exists but is not a SetX or SetY, it means it was replaced (e.g. by ChangeX)
            // enabling the fix condition.
            if (!(node instanceof SetXTo) && !(node instanceof SetYTo)) {
                this.currentActor = context.actor();
                this.currentScript = context.script();
                this.currentProcedure = context.procedure();
                addFix(node, node.getMetadata());
            }
        } else {
            // Block was deleted
            addFix(null, null);
        }

        if (fixes.isEmpty()) {
            refreshBugInstances(program);
            program.accept(this);
        }

        return Collections.unmodifiableSet(fixes);
    }

    @Override
    public void visit(Script node) {
        currentArrowKey = -1;
        isInKeyContext = false;

        if (node.getEvent() instanceof KeyPressed keyPressed
                && !node.getStmtList().getStmts().isEmpty()) {
            Key keyNode = keyPressed.getKey();
            if (keyNode.getKey() instanceof NumberLiteral keyNum) {
                int key = (int) keyNum.getValue();
                if (isArrowKey(key)) {
                    currentArrowKey = key;
                    isInKeyContext = true;
                }
            }
        }
        visitChildren(node);
        isInKeyContext = false;
        currentArrowKey = -1;
    }

    @Override
    public void visit(IfThenStmt node) {
        // Check for if(key pressed?) context
        int savedArrowKey = currentArrowKey;
        boolean savedInKeyContext = isInKeyContext;

        if (node.getBoolExpr() instanceof IsKeyPressed isKeyPressed) {
            Key keyNode = isKeyPressed.getKey();
            if (keyNode.getKey() instanceof NumberLiteral keyNum) {
                int key = (int) keyNum.getValue();
                if (isArrowKey(key)) {
                    currentArrowKey = key;
                    isInKeyContext = true;
                }
            }
        }

        // Check statements inside the if
        checkStmts(node.getThenStmts().getStmts());
        visitChildren(node);

        // Restore context
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

    private void checkStmts(List<Stmt> stmts) {
        for (int i = 0; i < stmts.size() && i < 2; i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof SetXTo setX) {
                if (matchesBugLocation(setX)) {
                    checkIfFixed(setX, true);
                }
            } else if (stmt instanceof SetYTo setY) {
                if (matchesBugLocation(setY)) {
                    checkIfFixed(setY, false);
                }
            }
        }
    }

    /**
     * Check if the bug is fixed for the given node.
     *
     * @param node   the SetX/SetY node
     * @param isSetX true if SetXTo, false if SetYTo
     */
    private void checkIfFixed(Stmt node, boolean isSetX) {
        // The bug triggers when:
        // - SetY is used with UP/DOWN arrow keys
        // - SetX is used with LEFT/RIGHT arrow keys
        //
        // So the bug is FIXED if:
        // - We're not in an arrow key context at all, OR
        // - SetY is used but NOT with UP/DOWN, OR
        // - SetX is used but NOT with LEFT/RIGHT

        if (!isInKeyContext) {
            // Not in any arrow key context - fixed!
            addFix(node, node.getMetadata());
            return;
        }

        boolean isBuggyContext;
        if (isSetX) {
            // SetX is buggy with LEFT/RIGHT arrows
            isBuggyContext = currentArrowKey == KeyCode.LEFT_ARROW.getKeycode()
                    || currentArrowKey == KeyCode.RIGHT_ARROW.getKeycode();
        } else {
            // SetY is buggy with UP/DOWN arrows
            isBuggyContext = currentArrowKey == KeyCode.UP_ARROW.getKeycode()
                    || currentArrowKey == KeyCode.DOWN_ARROW.getKeycode();
        }

        if (!isBuggyContext) {
            // The arrow key context doesn't match the bug pattern - fixed!
            addFix(node, node.getMetadata());
        }
        // Otherwise, the bug still exists - don't report a fix
    }

    private boolean isArrowKey(int key) {
        return key == KeyCode.UP_ARROW.getKeycode()
                || key == KeyCode.DOWN_ARROW.getKeycode()
                || key == KeyCode.LEFT_ARROW.getKeycode()
                || key == KeyCode.RIGHT_ARROW.getKeycode();
    }
}
