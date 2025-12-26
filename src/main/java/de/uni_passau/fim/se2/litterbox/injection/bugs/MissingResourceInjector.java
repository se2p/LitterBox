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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.function.BiFunction;

import static de.uni_passau.fim.se2.litterbox.injection.util.InjectionConstants.INVALID_COSTUME_NAME;
import static de.uni_passau.fim.se2.litterbox.injection.util.InjectionConstants.INVALID_SOUND_NAME;

/**
 * Injects MissingResource bug by replacing valid costume/sound names with invalid ones.
 * <p>
 * The detector checks if costume or sound names referenced in blocks exist in the
 * actor's metadata. This injector replaces valid names with invalid ones to trigger detection.
 */
public class MissingResourceInjector extends AbstractBugInjector {

    private ActorDefinition currentActor;

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingResource();
    }

    @Override
    public void visit(ActorDefinition actor) {
        ActorDefinition previousActor = currentActor;
        try {
            currentActor = actor;
            super.visit(actor);
        } finally {
            currentActor = previousActor;
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (node.getCostumeChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {

            String currentCostume = strId.getName();
            if (currentActorHasCostume(currentCostume)) {
                replaceWithInvalidResource(node, withExpr, INVALID_COSTUME_NAME,
                        node.getMetadata(), SwitchCostumeTo::new);
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {

            String currentSound = strId.getName();
            if (currentActorHasSound(currentSound)) {
                replaceWithInvalidResource(node, withExpr, INVALID_SOUND_NAME,
                        node.getMetadata(), PlaySoundUntilDone::new);
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(StartSound node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId strId) {

            String currentSound = strId.getName();
            if (currentActorHasSound(currentSound)) {
                replaceWithInvalidResource(node, withExpr, INVALID_SOUND_NAME,
                        node.getMetadata(), StartSound::new);
            }
        }
        super.visit(node);
    }

    // ==================== Helper Methods ====================

    /**
     * Generic method to replace a resource reference with an invalid one.
     *
     * @param node            The original statement to replace
     * @param originalWithExpr The original WithExpr containing the valid resource
     * @param invalidName     The invalid resource name to use
     * @param metadata        The metadata to preserve
     * @param constructor     Constructor for the replacement statement
     * @param <T>             The statement type
     */
    private <T extends Stmt> void replaceWithInvalidResource(
            T node,
            WithExpr originalWithExpr,
            String invalidName,
            BlockMetadata metadata,
            BiFunction<WithExpr, BlockMetadata, T> constructor) {

        mutateClone(node, clonedNode -> {
            WithExpr newWithExpr = createInvalidWithExpr(originalWithExpr, invalidName);
            T newStmt = constructor.apply(newWithExpr, metadata);
            newWithExpr.setParentNode(newStmt);
            replaceInParent(clonedNode, newStmt);
        });
    }

    /**
     * Creates a new WithExpr with an invalid resource name.
     */
    private WithExpr createInvalidWithExpr(WithExpr originalWithExpr, String invalidName) {
        StrId invalidStrId = new StrId(invalidName);
        WithExpr newWithExpr = new WithExpr(invalidStrId, originalWithExpr.getMetadata());
        invalidStrId.setParentNode(newWithExpr);
        return newWithExpr;
    }

    // ==================== Resource Validation ====================

    /**
     * Checks if the current actor has a costume with the given name.
     * Returns true if metadata is missing (allow injection) or if costume exists.
     */
    private boolean currentActorHasCostume(String costumeName) {
        if (currentActor == null || currentActor.getActorMetadata() == null) {
            return true;
        }
        var costumes = currentActor.getActorMetadata().getCostumes().getList();
        if (costumes.isEmpty()) {
            return true;
        }
        // Check if this is placeholder test metadata
        boolean isPlaceholderMetadata = costumes.stream()
                .allMatch(c -> c.getName().equals(c.getClass().getSimpleName()));
        if (isPlaceholderMetadata) {
            return true;
        }
        return costumes.stream().anyMatch(c -> c.getName().equals(costumeName));
    }

    /**
     * Checks if the current actor has a sound with the given name.
     * Returns true if metadata is missing (allow injection) or if sound exists.
     */
    private boolean currentActorHasSound(String soundName) {
        if (currentActor == null || currentActor.getActorMetadata() == null) {
            return true;
        }
        var sounds = currentActor.getActorMetadata().getSounds().getList();
        if (sounds.isEmpty()) {
            return true;
        }
        // Check if this is placeholder test metadata
        boolean isPlaceholderMetadata = sounds.stream()
                .allMatch(s -> s.getName().equals(s.getClass().getSimpleName()));
        if (isPlaceholderMetadata) {
            return true;
        }
        return sounds.stream().anyMatch(s -> s.getName().equals(soundName));
    }
}
