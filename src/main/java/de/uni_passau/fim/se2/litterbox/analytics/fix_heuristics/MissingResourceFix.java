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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ResourceMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fix heuristic for the MissingResource bug.
 *
 * <p>The bug is reported when referencing a non-existent resource.
 * The bug is fixed if the resource now exists.
 */
public class MissingResourceFix extends AbstractFixHeuristic {

    private Set<String> images;
    private Set<String> backdrops;
    private Set<String> sounds;

    private final MissingResource relatedBugFinder = new MissingResource();

    public MissingResourceFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        images = null;
        backdrops = null;
        sounds = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(Program node) {
        // Collect backdrops from Stage
        for (ActorDefinition actor : node.getActorDefinitionList().getDefinitions()) {
            if (actor.getActorType().isStage()) {
                backdrops = collectResourceNames(actor.getActorMetadata().getCostumes().getList());
                break;
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(ActorDefinition actor) {
        images = collectResourceNames(actor.getActorMetadata().getCostumes().getList());
        sounds = collectResourceNames(actor.getActorMetadata().getSounds().getList());
        if (actor.getActorType().isStage()) {
            backdrops = collectResourceNames(actor.getActorMetadata().getCostumes().getList());
        }
        super.visit(actor);
    }

    private Set<String> collectResourceNames(List<? extends ResourceMetadata> resources) {
        return resources.stream()
                .map(ResourceMetadata::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void check(ASTNode node, String name, Set<String> resources) {
        if (matchesBugLocation(node)) {
            // Fix: Resource IS present
            if (resources != null && resources.contains(name)) {
                addFix(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {
            check(node, strId.getName(), backdrops);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {
            check(node, strId.getName(), backdrops);
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (node.getCostumeChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {
            check(node, strId.getName(), images);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {
            check(node, strId.getName(), sounds);
        }
    }

    @Override
    public void visit(StartSound node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {
            check(node, strId.getName(), sounds);
        }
    }
}
