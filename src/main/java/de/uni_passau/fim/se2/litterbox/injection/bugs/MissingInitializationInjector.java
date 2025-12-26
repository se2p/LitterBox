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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.*;

/**
 * Injects the {@link MissingInitialization} bug by removing initialization statements
 * from the start of GreenFlag scripts.
 * <p>
 * <b>Bug Pattern:</b> A sprite/stage lacks proper initialization at the start of the program,
 * causing state to persist between runs.
 * <p>
 * <b>Preconditions for injection:</b>
 * <ol>
 *   <li>The attribute must be USED (read) later in the script</li>
 *   <li>No other GreenFlag script may initialize the same attribute</li>
 *   <li>No later unconditional definition may "cover" the use</li>
 * </ol>
 */
public class MissingInitializationInjector extends AbstractBugInjector {

    /**
     * Statement types that represent initialization operations.
     * Note: Hide/Show are excluded because the finder explicitly skips VISIBILITY attributes.
     */
    private static final Set<Class<? extends Stmt>> INITIALIZATION_TYPES = Set.of(
            // Motion
            GoToPosXY.class, GoToPos.class, SetXTo.class, SetYTo.class,
            PointInDirection.class, PointTowards.class, SetRotationStyle.class, SetDragMode.class,
            // Sprite look (excluding Hide/Show)
            SwitchCostumeTo.class, SetSizeTo.class, GoToLayer.class,
            // Actor look
            SwitchBackdrop.class, SetGraphicEffectTo.class, ClearGraphicEffects.class,
            // Sound
            StopAllSounds.class, ClearSoundEffects.class, SetSoundEffectTo.class, SetVolumeTo.class,
            // Common
            SetVariableTo.class, ResetTimer.class
    );

    private ActorDefinition currentActor;
    private Map<Defineable, Set<Script>> globalInitializations;

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingInitialization();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        globalInitializations = collectGlobalInitializations(program);
        return super.inject(program);
    }

    @Override
    public void visit(ActorDefinition actor) {
        this.currentActor = actor;
        super.visit(actor);
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof GreenFlag) {
            processGreenFlagScript(script);
        }
        super.visit(script);
    }

    /**
     * Processes a GreenFlag script, creating mutations for each removable initialization.
     */
    private void processGreenFlagScript(Script script) {
        List<Stmt> statements = script.getStmtList().getStmts();

        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);

            if (!isInitializationStatement(stmt)) {
                break; // Stop at first non-initialization statement
            }

            if (canRemoveInitialization(script, statements, i)) {
                final int index = i;
                mutateClone(script.getStmtList(), clonedStmtList ->
                        clonedStmtList.getStmts().remove(index)
                );
            }
        }
    }

    /**
     * Determines if removing the initialization at the given index would create a detectable bug.
     */
    private boolean canRemoveInitialization(Script script, List<Stmt> statements, int index) {
        Set<Defineable> definitions = getDefinitions(statements.get(index));

        // Must not be already initialized by an EARLIER statement in the same script
        // (e.g., "go to random position" initializes BOTH x and y, so removing "set y to"
        // won't create a detectable bug since y is still initialized)
        if (isInitializedEarlierInScript(statements, index, definitions)) {
            return false;
        }

        // Must be used later in the script
        if (!isUsedAfterIndex(statements, index + 1, definitions)) {
            return false;
        }

        // Must not be initialized by another GreenFlag script
        if (isInitializedByOtherScript(script, definitions)) {
            return false;
        }

        // Must not have a later unconditional definition that covers the use
        return !hasCoveringDefinition(statements, index + 1, definitions);
    }

    /**
     * Checks if any of the target definitions are already initialized by an earlier
     * statement in the same script.
     */
    private boolean isInitializedEarlierInScript(List<Stmt> statements, int currentIndex, Set<Defineable> targets) {
        for (int i = 0; i < currentIndex; i++) {
            Stmt stmt = statements.get(i);
            if (!isInitializationStatement(stmt)) {
                break; // Only check initialization block at the start
            }
            Set<Defineable> earlierDefs = getDefinitions(stmt);
            if (!Collections.disjoint(earlierDefs, targets)) {
                return true; // Found earlier initialization of same attribute
            }
        }
        return false;
    }

    // ==================== Global Initialization Tracking ====================

    /**
     * Pre-scans all GreenFlag scripts to track which scripts initialize which attributes.
     */
    private Map<Defineable, Set<Script>> collectGlobalInitializations(Program program) {
        Map<Defineable, Set<Script>> result = new HashMap<>();

        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            for (Script script : actor.getScripts().getScriptList()) {
                collectScriptInitializations(script, actor, result);
            }
        }
        return result;
    }

    private void collectScriptInitializations(Script script, ActorDefinition actor,
                                              Map<Defineable, Set<Script>> result) {
        ActorDefinition savedActor = this.currentActor;
        this.currentActor = actor;

        script.accept(new ScratchVisitor() {
            @Override
            public void visit(ASTNode node) {
                if (node instanceof Stmt stmt && isInitializationStatement(stmt)) {
                    for (Defineable def : getDefinitions(stmt)) {
                        result.computeIfAbsent(def, k -> new HashSet<>()).add(script);
                    }
                }
                visitChildren(node);
            }
        });

        this.currentActor = savedActor;
    }

    private boolean isInitializedByOtherScript(Script currentScript, Set<Defineable> definitions) {
        for (Defineable def : definitions) {
            Set<Script> scripts = globalInitializations.get(def);
            if (scripts != null && scripts.stream().anyMatch(s -> s != currentScript)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Definition & Use Analysis ====================

    private boolean isInitializationStatement(Stmt stmt) {
        return INITIALIZATION_TYPES.contains(stmt.getClass());
    }

    /**
     * Gets all defineables (attributes, variables, lists) that this statement defines.
     */
    private Set<Defineable> getDefinitions(Stmt stmt) {
        Set<Defineable> defs = new HashSet<>();

        if (currentActor != null) {
            AttributeDefinitionVisitor attrVisitor = new AttributeDefinitionVisitor(currentActor);
            stmt.accept(attrVisitor);
            defs.addAll(attrVisitor.getDefineables());
        }

        VariableDefinitionVisitor varVisitor = new VariableDefinitionVisitor();
        stmt.accept(varVisitor);
        defs.addAll(varVisitor.getDefineables());

        ListDefinitionVisitor listVisitor = new ListDefinitionVisitor();
        stmt.accept(listVisitor);
        defs.addAll(listVisitor.getDefineables());

        return defs;
    }


    /**
     * Checks if any of the target definitions are used after the given index.
     */
    private boolean isUsedAfterIndex(List<Stmt> statements, int startIndex, Set<Defineable> targets) {
        for (int i = startIndex; i < statements.size(); i++) {
            Set<Defineable> uses = getUses(statements.get(i));
            if (!Collections.disjoint(uses, targets)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all target definitions are "covered" by later unconditional definitions
     * before they are used.
     * <p>
     * A definition is "covered" if it appears in a top-level initialization statement
     * before any use of that definition. Only top-level statements count because
     * definitions inside control flow are conditional.
     */
    private boolean hasCoveringDefinition(List<Stmt> statements, int startIndex, Set<Defineable> targets) {
        Set<Defineable> remainingTargets = new HashSet<>(targets);

        for (int i = startIndex; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);

            // 1. Check for covering definitions within this statement
            Set<Defineable> coveredByStmt = getCoveringDefinitions(stmt, remainingTargets);
            remainingTargets.removeAll(coveredByStmt);

            if (remainingTargets.isEmpty()) {
                return true; // All targets are covered
            }

            // 2. Check for uses within this statement of the REMAINING targets.
            // (Note: getCoveringDefinitions already checked for uses before definitions within certain stmt types,
            // but for others like IfThen or loops, we still need to check if they use anything).
            Set<Defineable> usesInStmt = getUses(stmt);
            if (!Collections.disjoint(usesInStmt, remainingTargets)) {
                return false; // Found a use before any covering definition
            }
        }

        // No use found for remaining targets by end of script.
        // Effectively covered (undetectable by finder).
        return true;
    }

    /**
     * Identifies defineables from the 'targets' set that are GUARANTEED to be defined
     * by this statement BEFORE being used, assuming the statement is reached.
     */
    private Set<Defineable> getCoveringDefinitions(Stmt stmt, Set<Defineable> targets) {
        if (isInitializationStatement(stmt)) {
            Set<Defineable> defs = getDefinitions(stmt);
            Set<Defineable> uses = getUses(stmt);
            Set<Defineable> covered = new HashSet<>(defs);
            covered.retainAll(targets);
            covered.removeAll(uses); // Defines must happen before Uses (e.g., ChangeVariable uses then defines)
            return covered;
        }

        if (stmt instanceof de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt ifElse) {
            // Condition uses happen BEFORE branches
            Set<Defineable> usesInCondition = getUses(ifElse.getBoolExpr());
            if (!Collections.disjoint(usesInCondition, targets)) {
                return Collections.emptySet();
            }

            Set<Defineable> thenCovered = getCoveringDefinitions(ifElse.getThenStmts().getStmts(), targets);
            Set<Defineable> elseCovered = getCoveringDefinitions(ifElse.getElseStmts().getStmts(), targets);

            Set<Defineable> intersection = new HashSet<>(thenCovered);
            intersection.retainAll(elseCovered);
            return intersection;
        }

        return Collections.emptySet();
    }

    private Set<Defineable> getCoveringDefinitions(List<Stmt> statements, Set<Defineable> targets) {
        Set<Defineable> coveredInThisList = new HashSet<>();
        Set<Defineable> remainingTargets = new HashSet<>(targets);

        for (Stmt stmt : statements) {
            Set<Defineable> coveredByStmt = getCoveringDefinitions(stmt, remainingTargets);
            coveredInThisList.addAll(coveredByStmt);
            remainingTargets.removeAll(coveredByStmt);

            if (remainingTargets.isEmpty()) {
                break;
            }

            // Check if ANY of the still-remaining targets are used here
            Set<Defineable> usesInStmt = getUses(stmt);
            if (!Collections.disjoint(usesInStmt, remainingTargets)) {
                // These are used before being covered. They can't be covered by later stmts.
                remainingTargets.removeAll(usesInStmt);
            }

            if (remainingTargets.isEmpty()) {
                break;
            }
        }
        return coveredInThisList;
    }

    private Set<Defineable> getUses(de.uni_passau.fim.se2.litterbox.ast.model.ASTNode node) {
        UseCollector collector = new UseCollector(currentActor);
        node.accept(collector);
        return collector.uses;
    }

    // ==================== Use Collector ====================

    /**
     * Collects all USED (read) attributes, variables, and lists from a statement tree.
     * Handles definition statements specially to avoid counting assignment targets as uses.
     */
    private static class UseCollector implements ScratchVisitor {
        private final Set<Defineable> uses = new HashSet<>();
        private final ActorDefinition actor;

        UseCollector(ActorDefinition actor) {
            this.actor = actor;
        }

        @Override
        public void visit(ASTNode node) {
            collectUses(node);
            visitChildren(node);
        }

        @Override
        public void visit(SetVariableTo node) {
            // Only the expression is a use, not the variable being assigned
            node.getExpr().accept(this);
        }

        @Override
        public void visit(ChangeVariableBy node) {
            // The variable is a use (read before modify) AND the expression
            collectUses(node);
            node.getExpr().accept(this);
        }

        @Override
        public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable node) {
            // Don't traverse - HideVariable doesn't use the variable's value
        }

        @Override
        public void visit(de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable node) {
            // Don't traverse - ShowVariable doesn't use the variable's value
        }

        @Override
        public void visit(ProcedureDefinition node) {
            // Don't traverse into procedure definitions
        }

        private void collectUses(ASTNode node) {
            if (actor != null) {
                AttributeUseVisitor attrVisitor = new AttributeUseVisitor(actor);
                node.accept(attrVisitor);
                uses.addAll(attrVisitor.getDefineables());
            }

            VariableUseVisitor varVisitor = new VariableUseVisitor();
            node.accept(varVisitor);
            uses.addAll(varVisitor.getDefineables());

            ListUseVisitor listVisitor = new ListUseVisitor();
            node.accept(listVisitor);
            uses.addAll(listVisitor.getDefineables());
        }
    }
}
