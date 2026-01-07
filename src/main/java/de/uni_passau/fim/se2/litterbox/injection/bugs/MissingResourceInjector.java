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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ResourceMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.util.ResourceValidator;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.uni_passau.fim.se2.litterbox.injection.util.InjectionConstants.INVALID_COSTUME_NAME;
import static de.uni_passau.fim.se2.litterbox.injection.util.InjectionConstants.INVALID_SOUND_NAME;

/**
 * Injects MissingResource bug by replacing valid costume/sound names with invalid ones.
 *
 * <p>
 * The detector checks if costume or sound names referenced in blocks exist in the
 * actor's metadata. This injector replaces valid names with invalid ones to trigger detection.
 */
public class MissingResourceInjector extends AbstractBugInjector {

    private ActorDefinition currentActor;
    private Set<String> stageBackdrops = Collections.emptySet();

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
    public List<InjectionResult> inject(Program program) {
        // Create a strict deep copy of the program to prevent any mutation of the original AST
        // during the injection process. This addresses the issue where "innocent bystander" blocks
        // were disappearing or the original AST was being mutated in complex statements.
        CloneVisitor cloner = new CloneVisitor();
        Program workingCopy = cloner.apply(program);

        // Ensure parent links are correctly established in the working copy
        workingCopy.accept(new ParentVisitor());

        return super.inject(workingCopy);
    }

    @Override
    public void visit(Program node) {
        // Collect backdrops from Stage
        for (ActorDefinition actor : node.getActorDefinitionList().getDefinitions()) {
            if (actor.getActorType().isStage()) {
                stageBackdrops = actor.getActorMetadata().getCostumes().getList().stream()
                        .map(ResourceMetadata::getName)
                        .collect(Collectors.toUnmodifiableSet());
                break;
            }
        }
        super.visit(node);
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
    public void visit(SwitchBackdrop node) {
        handleResourceStmt(node, node.getElementChoice(),
                name -> stageBackdrops.contains(name),
                SwitchBackdrop::new,
                INVALID_COSTUME_NAME);
        super.visit(node);
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        handleResourceStmt(node, node.getElementChoice(),
                name -> stageBackdrops.contains(name),
                SwitchBackdropAndWait::new,
                INVALID_COSTUME_NAME);
        super.visit(node);
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (node.getBackdrop() instanceof StrId strId) {
            String backdropName = strId.getName();
            if (stageBackdrops.contains(backdropName)) {
                injectBackdropEvent(node);
            }
        }
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
                replaceWithInvalidResource(node, withExpr, invalidName, constructor);
            }
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Generic method to replace a resource reference with an invalid one.
     *
     * @param node             The original statement to replace
     * @param originalWithExpr The original WithExpr containing the valid resource
     * @param invalidName      The invalid resource name to use
     * @param constructor      Constructor for the replacement statement
     * @param <T>              The statement type
     */
    private <T extends Stmt> void replaceWithInvalidResource(
            T node,
            WithExpr originalWithExpr,
            String invalidName,
            BiFunction<WithExpr, BlockMetadata, T> constructor) {

        modifyClonedNode(node, clonedNode -> {
            WithExpr newWithExpr = createInvalidWithExpr(originalWithExpr, invalidName);
            T newStmt = constructor.apply(newWithExpr, clonedNode.getMetadata());
            newWithExpr.setParentNode(newStmt);
            ParentVisitor.apply(newStmt);

            replaceInParent(clonedNode, newStmt);
        });
    }

    /**
     * Creates a new WithExpr with an invalid resource name and fresh metadata.
     * Using fresh metadata (with a new block ID) prevents duplicate block IDs in serialized JSON.
     */
    private WithExpr createInvalidWithExpr(WithExpr originalWithExpr, String invalidName) {
        StrId invalidStrId = new StrId(invalidName);

        // Create new metadata with a fresh block ID to avoid duplicate IDs in serialization
        BlockMetadata newMetadata;
        if (originalWithExpr.getMetadata() instanceof NonDataBlockMetadata originalMeta) {
            // Use createArtificialNonBlockMetadata to generate fresh metadata with proper shadow flag
            newMetadata = NonDataBlockMetadata.createArtificialNonBlockMetadata(originalMeta.isShadow());
        } else {
            // Fallback: use original metadata if not NonDataBlockMetadata
            newMetadata = originalWithExpr.getMetadata();
        }

        WithExpr newWithExpr = new WithExpr(invalidStrId, newMetadata);
        invalidStrId.setParentNode(newWithExpr);
        return newWithExpr;
    }

    private void injectBackdropEvent(BackdropSwitchTo node) {
        modifyClonedNode(node, clonedNode -> {
            StrId invalidId = new StrId(INVALID_COSTUME_NAME);
            // Use existing metadata
            BackdropSwitchTo newEvent = new BackdropSwitchTo(invalidId, clonedNode.getMetadata());
            invalidId.setParentNode(newEvent);

            if (clonedNode.getParentNode() instanceof Script oldScript
                    && oldScript.getParentNode() instanceof ScriptList scriptList) {

                Script newScript = new Script(newEvent, oldScript.getStmtList());
                newEvent.setParentNode(newScript);
                oldScript.getStmtList().setParentNode(newScript);

                List<Script> scripts = scriptList.getScriptList();
                // Use identity-based search instead of indexOf (which uses equals())
                for (int i = 0; i < scripts.size(); i++) {
                    if (scripts.get(i) == oldScript) {
                        scripts.set(i, newScript);
                        newScript.setParentNode(scriptList);
                        break;
                    }
                }
            }
        });
    }
}
