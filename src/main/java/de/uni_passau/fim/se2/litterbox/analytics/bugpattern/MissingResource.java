/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * If the costume of a sprite should be set to one not available or play a sound that is not registered in the sprite
 * it will not work.
 */
public class MissingResource extends AbstractIssueFinder {
    public static final String NAME = "missing_resource";

    private Set<String> images;
    private Set<String> backdrops;
    private Set<String> sounds;

    @Override
    public void visit(Program node) {
        for (ActorDefinition actor : node.getActorDefinitionList().getDefinitions()) {
            if (actor.getActorType().isStage()) {
                backdrops = collectResourceNames(actor.getActorMetadata().getCostumes().getList());
                break;
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(ActorDefinition actor) {
        images = collectResourceNames(actor.getActorMetadata().getCostumes().getList());
        sounds = collectResourceNames(actor.getActorMetadata().getSounds().getList());
        super.visit(actor);
    }

    private Set<String> collectResourceNames(final List<? extends ResourceMetadata> resources) {
        return resources.stream().map(ResourceMetadata::getName).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (node.getElementChoice() instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String backdropName = strId.getName();
            checkBackdropIssue(node, backdropName);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (node.getElementChoice() instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String backdropName = strId.getName();
            checkBackdropIssue(node, backdropName);
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (node.getCostumeChoice() instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String costume = strId.getName();
            checkCostumeIssue(node, costume);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (node.getElementChoice() instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String sound = strId.getName();
            checkSoundIssue(node, sound);
        }
    }

    @Override
    public void visit(StartSound node) {
        if (node.getElementChoice() instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String sound = strId.getName();
            checkSoundIssue(node, sound);
        }
    }

    private void checkBackdropIssue(final ASTNode node, final String backdropName) {
        if (!backdrops.contains(backdropName)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    private void checkCostumeIssue(final ASTNode node, final String costumeName) {
        if (!images.contains(costumeName)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    private void checkSoundIssue(final ASTNode node, final String soundName) {
        if (!sounds.contains(soundName)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
