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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.HideWithoutShow;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects HideWithoutShow bug by removing Show statements and adding Hide if needed.
 *
 * <p>
 * Prerequisite: Actor has Show + visible change blocks (motion, look, etc.)
 *
 * <p>
 * Transformation:
 * <ul>
 *   <li>Remove all Show statements</li>
 *   <li>Add Hide at start of first script (if not already present)</li>
 * </ul>
 */
public class HideWithoutShowInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new HideWithoutShow();

    private boolean hasShow;
    private boolean hasHide;
    private boolean hasVisibleChange;
    private Script firstScript;

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Script node) {
        // Override to allow visiting Never blocks (dead code)
        // so we can remove Show statements even from dead code
        visitChildren(node);
    }

    @Override
    public void visit(ActorDefinition node) {
        // Reset state for each actor
        hasShow = false;
        hasHide = false;
        hasVisibleChange = false;
        firstScript = null;

        // First pass: check prerequisites and find first script
        checkActorPrerequisites(node);

        // Only proceed if actor has Show + visible changes
        if (hasShow && hasVisibleChange) {
            // Use ActorDefinition as context to capture all changes (Show removals across scripts)
            String scriptId = "";
            if (firstScript != null) {
                scriptId = AstNodeUtil.getBlockId(firstScript);
                if (scriptId == null) {
                    scriptId = "";
                }
            }

            modifyClonedNode(node, clonedActor -> {
                // Remove ALL Show statements from ALL scripts in this actor
                clonedActor.accept(new ScratchVisitor() {
                    @Override
                    public void visit(StmtList stmtList) {
                        stmtList.getStmts().removeIf(stmt -> stmt instanceof Show);
                        visitChildren(stmtList);
                    }
                });

                // If no Hide existed in the original actor, add one at start of first script
                if (!hasHide) {
                    // Find the first non-empty script in the cloned actor
                    clonedActor.accept(new ScratchVisitor() {
                        private boolean found = false;

                        @Override
                        public void visit(Script script) {
                            if (!found && !script.getStmtList().getStmts().isEmpty()
                                    && !(script.getEvent() instanceof Never)) {
                                Hide hide = new Hide(NonDataBlockMetadata.emptyNonBlockMetadata());
                                script.getStmtList().getStmts().addFirst(hide);
                                found = true;
                            }
                        }
                    });
                }
            }, scriptId);
        }
    }

    private void checkActorPrerequisites(ActorDefinition node) {
        node.accept(new ScratchVisitor() {
            private boolean inNeverBlock = false;

            @Override
            public void visit(Script script) {
                boolean isNever = script.getEvent() instanceof Never;

                // Track first non-Never, non-empty script
                if (firstScript == null && !script.getStmtList().getStmts().isEmpty() && !isNever) {
                    firstScript = script;
                }

                // Skip Never blocks for hasShow/hasVisibleChange detection
                // (matching bug finder behavior)
                if (isNever) {
                    inNeverBlock = true;
                    visitChildren(script);
                    inNeverBlock = false;
                } else {
                    visitChildren(script);
                }
            }

            @Override
            public void visit(Show show) {
                if (!inNeverBlock) {
                    hasShow = true;
                }
            }

            @Override
            public void visit(Hide hide) {
                // Hide is checked only if not in Never block
                if (!inNeverBlock) {
                    hasHide = true;
                }
            }

            @Override
            public void visit(SpriteMotionStmt stmt) {
                if (!inNeverBlock) {
                    hasVisibleChange = true;
                }
            }

            @Override
            public void visit(SpriteLookStmt stmt) {
                if (!inNeverBlock) {
                    hasVisibleChange = true;
                }
            }

            @Override
            public void visit(ActorLookStmt stmt) {
                if (!inNeverBlock) {
                    hasVisibleChange = true;
                }
            }
        });
    }
}

