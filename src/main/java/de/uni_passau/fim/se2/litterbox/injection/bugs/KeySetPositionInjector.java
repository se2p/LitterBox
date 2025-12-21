/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    public IssueFinder getIssueFinder() {
        return new KeySetPosition();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        ExtractingCloneVisitor visitor = new ExtractingCloneVisitor();
        Program cloned = (Program) program.accept(visitor);
        return Collections.singletonList(new InjectionResult(cloned, "", ""));
    }

    /**
     * Checks if a statement is a motion statement that benefits from continuous polling.
     * <p>
     * Currently limited to statements that the {@link KeySetPosition} detector flags.
     * <p>
     * Future extension: The polling vs event-driven issue applies to any continuous/incremental
     * changes that benefit from rapid repeated execution. Consider expanding to include:
     * <ul>
     *   <li>Motion: TurnRight, TurnLeft</li>
     *   <li>Looks: ChangeSize, ChangeGraphicEffectBy</li>
     *   <li>Sound: ChangeVolumeBy</li>
     * </ul>
     * Note: The detector would also need to be extended to flag these patterns.
     */
    private boolean isMotionStatement(Stmt stmt) {
        return stmt instanceof ChangeXBy || stmt instanceof ChangeYBy
                || stmt instanceof MoveSteps || stmt instanceof SetXTo || stmt instanceof SetYTo;
    }

    private boolean isKeyPressedIfWithMotion(Stmt stmt) {
        if (!(stmt instanceof IfThenStmt ifStmt)) {
            return false;
        }
        if (!(ifStmt.getBoolExpr() instanceof IsKeyPressed)) {
            return false;
        }
        return ifStmt.getThenStmts().getStmts().stream().anyMatch(this::isMotionStatement);
    }

    /**
     * Clone visitor that extracts key-pressed-if statements from forever loops
     * into separate event-driven scripts.
     */
    private class ExtractingCloneVisitor extends CloneVisitor {
        private final List<Script> extractedScripts = new ArrayList<>();

        @Override
        public ASTNode visit(ScriptList node) {
            extractedScripts.clear();

            List<Script> newScripts = new ArrayList<>();
            for (Script script : node.getScriptList()) {
                newScripts.add(processScript(script));
            }
            newScripts.addAll(extractedScripts);

            return new ScriptList(newScripts);
        }

        private Script processScript(Script script) {
            List<Stmt> newStmts = new ArrayList<>();
            boolean modified = false;

            for (Stmt stmt : script.getStmtList().getStmts()) {
                if (stmt instanceof RepeatForeverStmt loop) {
                    RepeatForeverStmt processedLoop = processForeverLoop(loop);
                    if (processedLoop != loop) {
                        modified = true;
                        newStmts.add(processedLoop);
                    } else {
                        newStmts.add(apply(stmt));
                    }
                } else {
                    newStmts.add(apply(stmt));
                }
            }

            if (modified) {
                return new Script(apply(script.getEvent()), new StmtList(newStmts));
            }
            return (Script) super.visit(script);
        }

        private RepeatForeverStmt processForeverLoop(RepeatForeverStmt loop) {
            List<Stmt> remainingStmts = new ArrayList<>();
            boolean extracted = false;

            for (Stmt stmt : loop.getStmtList().getStmts()) {
                if (isKeyPressedIfWithMotion(stmt)) {
                    extractAsKeyPressedScript((IfThenStmt) stmt);
                    extracted = true;
                } else {
                    remainingStmts.add(apply(stmt));
                }
            }

            if (extracted) {
                return new RepeatForeverStmt(
                        new StmtList(remainingStmts),
                        apply(loop.getMetadata())
                );
            }
            return loop;
        }

        private void extractAsKeyPressedScript(IfThenStmt ifStmt) {
            IsKeyPressed condition = (IsKeyPressed) ifStmt.getBoolExpr();
            KeyPressed event = new KeyPressed(
                    apply(condition.getKey()),
                    new NoBlockMetadata()
            );
            StmtList body = apply(ifStmt.getThenStmts());
            extractedScripts.add(new Script(event, body));
        }
    }
}
