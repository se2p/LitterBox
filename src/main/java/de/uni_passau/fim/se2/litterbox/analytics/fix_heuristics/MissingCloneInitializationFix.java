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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.*;

/**
 * Fix heuristic for the MissingCloneInitialization bug.
 *
 * <p>The bug is reported when CreateCloneOf is used but no initialization handler exists.
 * The bug is fixed if the cloned actor now has a StartedAsClone or SpriteClicked handler.
 */
public class MissingCloneInitializationFix extends AbstractFixHeuristic {

    private Set<String> whenStartsAsCloneActors;
    private List<ASTNode> bugLocators;

    private final MissingCloneInitialization relatedBugFinder = new MissingCloneInitialization();

    public MissingCloneInitializationFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        whenStartsAsCloneActors = new HashSet<>();
        bugLocators = new ArrayList<>();
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(StartedAsClone node) {
        if (currentActor != null) {
            whenStartsAsCloneActors.add(currentActor.getIdent().getName());
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteClicked node) {
        if (currentActor != null) {
            whenStartsAsCloneActors.add(currentActor.getIdent().getName());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Myself node) {
        if (AstNodeUtil.hasBlockId(node.getParentNode(), getBugLocationBlockId())) {
            bugLocators.add(node);
        }
    }

    @Override
    public void visit(WithCloneExpr node) {
        if (AstNodeUtil.hasBlockId(node.getParentNode(), getBugLocationBlockId())) {
            bugLocators.add(node);
        }
    }

    @Override
    public Set<FixResult> check(Program program) {
        // super.check calls resetState(), refreshBugInstances(), and visits the program.

        // Check if block deleted
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        for (ASTNode bugNode : bugLocators) {
            ActorDefinition actor = AstNodeUtil.findActor(bugNode).orElse(null);

            if (actor != null) {
                String targetSprite = actor.getIdent().getName();

                if (bugNode instanceof WithCloneExpr withClone) {
                    if (withClone.getExpression() instanceof StrId strId) {
                        targetSprite = strId.getName();
                    } else if (withClone.getExpression() instanceof StringLiteral strLit) {
                        targetSprite = strLit.getText();
                    }
                }

                if (whenStartsAsCloneActors.contains(targetSprite)) {
                    addFix(bugNode.getParentNode(), bugNode.getParentNode().getMetadata());
                }
            }
        }

        return Collections.unmodifiableSet(fixes);
    }
}
