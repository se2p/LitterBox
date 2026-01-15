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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.KeySetPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Injects the "Key Set Position" bug pattern by converting key-polling motion code
 * to event-driven code.
 *
 * <p>This injector finds patterns like:
 * <pre>
 *   forever {
 *     if (key pressed?) {
 *       move / change x / change y
 *     }
 *   }
 * </pre>
 * and converts them to:
 * <pre>
 *   when [key] pressed {
 *     move / change x / change y
 *   }
 * </pre>
 *
 * <p>This is problematic because event-driven key handling doesn't have the same
 * continuous repeat behavior as polling in a forever loop, causing motion to only
 * happen once per key press instead of continuously while the key is held.
 */
public class KeySetPositionInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new KeySetPosition();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        ExtractingCloneVisitor visitor = new ExtractingCloneVisitor();
        Program cloned = (Program) program.accept(visitor);

        if (!visitor.extractedAny) {
            return Collections.emptyList();
        }

        // Fix for empty script output: Calculate context strings explicitly
        String originalScript = "";
        String mutatedScript = "";
        String spriteName = "";
        String originalScriptId = "";
        String mutatedScriptId = "";

        if (visitor.firstModifiedOriginal != null && visitor.firstModifiedReplacement != null) {
            // Establish parent links in the new tree
            cloned.accept(new ParentVisitor());

            spriteName = findContainingActorName(visitor.firstModifiedOriginal);
            originalScriptId = findContainingScriptId(visitor.firstModifiedOriginal);
            originalScript = safeGetScratchBlocks(findContextNode(visitor.firstModifiedOriginal));
            ASTNode mutatedContext = findContextNode(visitor.firstModifiedReplacement);
            mutatedScript = safeGetScratchBlocks(mutatedContext);
            mutatedScriptId = findContainingScriptId(visitor.firstModifiedReplacement);
        }

        return Collections.singletonList(
                new InjectionResult(cloned, spriteName, originalScriptId, mutatedScriptId, originalScript,
                        mutatedScript));
    }

    /**
     * Custom CloneVisitor that extracts key-polling patterns from forever loops into
     * event-driven scripts.
     *
     * <p>
     * This requires a custom visitor because the transformation involves:
     * 1. Detecting patterns within forever loops
     * 2. Creating new Script objects with KeyPressed events
     * 3. Adding these new scripts to the ScriptList
     *
     * <p>
     * The standard mutation patterns cannot create new top-level scripts during cloning.
     */
    private static class ExtractingCloneVisitor extends CloneVisitor {
        private final List<Script> extraScripts = new ArrayList<>();
        private boolean extractedAny = false;

        // Track nodes for CSV output
        ASTNode firstModifiedOriginal = null;
        ASTNode firstModifiedReplacement = null;
        Script currentOriginalScript = null;

        @Override
        public ASTNode visit(Script node) {
            currentOriginalScript = node;
            return super.visit(node);
        }

        @Override
        public ASTNode visit(ScriptList node) {
            extraScripts.clear();
            List<Script> scripts = node.getScriptList().stream()
                    .map(s -> (Script) s.accept(this))
                    .collect(Collectors.toCollection(ArrayList::new));
            scripts.addAll(extraScripts);
            return new ScriptList(scripts);
        }

        @Override
        public ASTNode visit(RepeatForeverStmt node) {
            List<Stmt> stmts = new ArrayList<>();
            for (Stmt stmt : node.getStmtList().getStmts()) {
                stmts.add((Stmt) stmt.accept(this));
                if (isCandidate(stmt)) {
                    extractToEvent((IfThenStmt) stmt);
                }
            }
            return new RepeatForeverStmt(new StmtList(stmts), apply(node.getMetadata()));
        }

        /**
         * Identifies "If [supported key] Pressed then [convertible motion]" statements.
         *
         * <p>Only matches patterns where ChangeXBy/ChangeYBy can be converted to SetXTo/SetYTo,
         * which is the bug pattern the finder looks for. The axis must also match:
         * - UP/DOWN/W/S keys: ChangeYBy
         * - LEFT/RIGHT/A/D keys: ChangeXBy
         */
        private boolean isCandidate(Stmt stmt) {
            if (!(stmt instanceof IfThenStmt ifStmt)) {
                return false;
            }

            BoolExpr cond = ifStmt.getBoolExpr();
            if (cond instanceof AsBool asBool) {
                cond = (BoolExpr) asBool.getOperand1();
            }

            if (!(cond instanceof IsKeyPressed keyPressed)) {
                return false;
            }
            int keyCode = getKeyCode(keyPressed);
            if (keyCode == -1) {
                return false;
            }

            // Check if the body has a convertible motion statement matching the key axis
            List<Stmt> body = ifStmt.getThenStmts().getStmts();
            return !body.isEmpty() && hasMatchingMotion(body, keyCode);
        }

        private int getKeyCode(IsKeyPressed node) {
            ASTNode expr = node.getKey().getKey();
            if (expr instanceof AsNumber asNumber) {
                expr = asNumber.getOperand1();
            }

            if (expr instanceof NumberLiteral num) {
                int key = (int) num.getValue();
                if (isVerticalKey(key) || isHorizontalKey(key)) {
                    return key;
                }
            }
            return -1;
        }

        /**
         * Checks if any of the first two statements is a convertible motion matching the key axis.
         */
        private boolean hasMatchingMotion(List<Stmt> body, int keyCode) {
            for (int i = 0; i < Math.min(body.size(), 2); i++) {
                if (isConvertibleMotion(body.get(i), keyCode)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks if a statement is a motion that creates the bug pattern when extracted.
         *
         * <p>Valid patterns that create the bug:
         * <ul>
         *   <li>ChangeYBy or SetYTo with UP/DOWN/W/S keys</li>
         *   <li>ChangeXBy or SetXTo with LEFT/RIGHT/A/D keys</li>
         * </ul>
         *
         * <p>The bug is created by extracting motion from a polling loop to an event handler.
         * ChangeXBy/ChangeYBy are converted to SetXTo/SetYTo during extraction.
         * SetXTo/SetYTo are kept as-is but still create the bug when moved to an event.
         */
        private boolean isConvertibleMotion(Stmt stmt, int keyCode) {
            if (stmt instanceof ChangeYBy || stmt instanceof SetYTo) {
                // Vertical motion: valid with UP/DOWN/W/S keys
                return isVerticalKey(keyCode);
            }
            if (stmt instanceof ChangeXBy || stmt instanceof SetXTo) {
                // Horizontal motion: valid with LEFT/RIGHT/A/D keys
                return isHorizontalKey(keyCode);
            }
            return false;
        }

        private boolean isVerticalKey(int key) {
            return key == KeyCode.UP_ARROW.getKeycode() || key == KeyCode.DOWN_ARROW.getKeycode()
                    || key == 'w' || key == 'W' || key == 's' || key == 'S';
        }

        private boolean isHorizontalKey(int key) {
            return key == KeyCode.LEFT_ARROW.getKeycode() || key == KeyCode.RIGHT_ARROW.getKeycode()
                    || key == 'a' || key == 'A' || key == 'd' || key == 'D';
        }

        private void extractToEvent(IfThenStmt ifStmt) {
            BoolExpr cond = ifStmt.getBoolExpr();
            if (cond instanceof AsBool asBool) {
                cond = (BoolExpr) asBool.getOperand1();
            }

            IsKeyPressed keyPressed = (IsKeyPressed) cond;
            KeyPressed event = new KeyPressed(
                    apply(keyPressed.getKey()), TopNonDataBlockMetadata.emptyTopNonBlockMetadata());

            // Convert relative motion to absolute motion to inject the bug
            StmtList convertedBody = convertToAbsoluteMotion(ifStmt.getThenStmts());
            Script newScript = new Script(event, convertedBody);
            extraScripts.add(newScript);

            if (!extractedAny) {
                firstModifiedOriginal = currentOriginalScript;
                firstModifiedReplacement = newScript;
            }
            extractedAny = true;
        }

        /**
         * Converts relative motion statements to absolute motion statements.
         * ChangeXBy -> SetXTo, ChangeYBy -> SetYTo
         */
        private StmtList convertToAbsoluteMotion(StmtList original) {
            List<Stmt> convertedStmts = new ArrayList<>();
            for (Stmt stmt : original.getStmts()) {
                if (stmt instanceof ChangeXBy changeX) {
                    // Convert "change x by (N)" to "set x to (N)"
                    convertedStmts.add(new SetXTo(apply(changeX.getNum()), apply(changeX.getMetadata())));
                } else if (stmt instanceof ChangeYBy changeY) {
                    // Convert "change y by (N)" to "set y to (N)"
                    convertedStmts.add(new SetYTo(apply(changeY.getNum()), apply(changeY.getMetadata())));
                } else {
                    convertedStmts.add((Stmt) stmt.accept(this));
                }
            }
            return new StmtList(convertedStmts);
        }
    }
}
