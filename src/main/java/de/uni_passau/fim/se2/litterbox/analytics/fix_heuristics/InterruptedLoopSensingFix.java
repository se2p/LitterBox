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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

/**
 * Fix heuristic for the InterruptedLoopSensing bug.
 *
 * <p>Uses "existence check" strategy - if the interrupting block still exists,
 * but the bug finder doesn't report it, it's considered fixed.
 */
public class InterruptedLoopSensingFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new InterruptedLoopSensing();

    public InterruptedLoopSensingFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> check(de.uni_passau.fim.se2.litterbox.ast.model.Program program) {
        refreshBugInstances(program);
        return super.check(program);
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node) && !isBugStillPresent(node)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(SayForSecs node) {
        check(node);
        super.visit(node);
    }

    @Override
    protected void findSemanticMatch(de.uni_passau.fim.se2.litterbox.ast.model.Program program) {
        // Simple semantic matching: find the first block of a supported type
        // that is NOT still buggy. This is a heuristic itself.
        program.accept(new de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor() {
            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds node) {
                tryMatch(node);
                visitChildren(node);
            }

            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo node) {
                tryMatch(node);
                visitChildren(node);
            }

            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY node) {
                tryMatch(node);
                visitChildren(node);
            }

            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs node) {
                tryMatch(node);
                visitChildren(node);
            }

            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone node) {
                tryMatch(node);
                visitChildren(node);
            }

            @Override
            public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs node) {
                tryMatch(node);
                visitChildren(node);
            }

            private void tryMatch(ASTNode node) {
                if (semanticMatch == null && !isBugStillPresent(node)) {
                    // Check if it's in a script (roughly same context)
                    if (de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.findParent(node, de.uni_passau.fim.se2.litterbox.ast.model.Script.class) != null) {
                        semanticMatch = node;
                    }
                }
            }
        });
    }
}
