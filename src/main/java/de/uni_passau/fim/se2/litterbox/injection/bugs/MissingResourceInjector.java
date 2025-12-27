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
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.util.ResourceValidator;

import java.util.function.BiFunction;
import java.util.function.Predicate;

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

    private final IssueFinder issueFinder = new MissingResource();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        // Clear state before visiting new actor
        this.currentActor = actor;
        super.visit(actor);
        this.currentActor = null;
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        handleResourceStmt(node, node.getCostumeChoice(),
                name -> ResourceValidator.hasCostume(currentActor.getActorMetadata(), name),
                SwitchCostumeTo::new,
                INVALID_COSTUME_NAME);
        super.visit(node);
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        handleResourceStmt(node, node.getElementChoice(),
                name -> ResourceValidator.hasSound(currentActor.getActorMetadata(), name),
                PlaySoundUntilDone::new,
                INVALID_SOUND_NAME);
        super.visit(node);
    }

    @Override
    public void visit(StartSound node) {
        handleResourceStmt(node, node.getElementChoice(),
                name -> ResourceValidator.hasSound(currentActor.getActorMetadata(), name),
                StartSound::new,
                INVALID_SOUND_NAME);
        super.visit(node);
    }

    private <T extends Stmt> void handleResourceStmt(
            T node,
            ElementChoice choice,
            Predicate<String> validator,
            BiFunction<WithExpr, BlockMetadata, T> constructor,
            String invalidName) {

        if (choice instanceof WithExpr withExpr && withExpr.getExpression() instanceof StrId strId) {
            String resourceName = strId.getName();
            if (validator.test(resourceName)) {
                replaceWithInvalidResource(node, withExpr, invalidName, node.getMetadata(), constructor);
            }
        }
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

        modifyClonedNode(node, clonedNode -> {
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
}
