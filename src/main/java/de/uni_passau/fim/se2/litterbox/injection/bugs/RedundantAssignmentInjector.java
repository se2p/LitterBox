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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.Set;

/**
 * Injects the {@link RedundantAssignment} bug.
 *
 * <p>
 * This injector inserts a duplicate assignment/set statement immediately after
 * an existing one. This makes the original assignment redundant since it's
 * immediately overwritten.
 *
 * <p>
 * Supports: SetVariableTo, SetXTo, SetYTo, GoToPosXY, PointInDirection,
 * SetSizeTo, SetGraphicEffectTo, SetSoundEffectTo, SetVolumeTo, SwitchCostumeTo,
 * SwitchBackdrop, SwitchBackdropAndWait, GoToPos, PointTowards.
 *
 * <p>
 * Preconditions:
 * <ul>
 *   <li>The statement list is not inside a loop (finder treats loops as halting)</li>
 *   <li>The previous statement is not a halting statement (finder marks all vars as live after halts)</li>
 * </ul>
 *
 * <p>
 * Transformation:
 * <pre>
 * Before: [set x to (100)]
 * After:  [set x to (100), set x to (100)]  &lt;-- first is now redundant!
 * </pre>
 */
public class RedundantAssignmentInjector extends AbstractBugInjector {

    private ActorDefinition currentActor;

    private final IssueFinder issueFinder = new RedundantAssignment();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        ActorDefinition previousActor = currentActor;
        try {
            this.currentActor = actor;
            super.visit(actor);
        } finally {
            this.currentActor = previousActor;
        }
    }

    private ActorDefinition getCurrentActor() {
        return currentActor;
    }

    @Override
    public void visit(StmtList stmtList) {
        // Skip if this StmtList is inside a loop (finder treats loops as halting)
        if (isInsideLoop(stmtList)) {
            super.visit(stmtList);
            return;
        }

        for (int i = 0; i < stmtList.getStmts().size(); i++) {
            Stmt stmt = stmtList.getStmts().get(i);
            if (isDuplicatable(stmt)
                    && !isPrecededByHaltingStatement(stmtList, i)
                    && !isSelfReferential(stmt)) {
                addDuplicateStatementMutation(stmtList, i);
            }
        }
        super.visit(stmtList);
    }

    /**
     * Checks if a statement uses the same attribute/variable it defines.
     * E.g., "switch costume to ((costume number) - 1)" uses costume to set costume.
     * Duplicating such statements is NOT redundant because each execution changes the value.
     */
    private boolean isSelfReferential(Stmt stmt) {
        // Collect what the statement defines
        AttributeDefinitionVisitor attrDefVisitor = new AttributeDefinitionVisitor(getCurrentActor());
        stmt.accept(attrDefVisitor);
        Set<Defineable> definitions = new HashSet<>(attrDefVisitor.getDefineables());

        VariableDefinitionVisitor varDefVisitor = new VariableDefinitionVisitor();
        stmt.accept(varDefVisitor);
        definitions.addAll(varDefVisitor.getDefineables());

        ListDefinitionVisitor listDefVisitor = new ListDefinitionVisitor();
        stmt.accept(listDefVisitor);
        definitions.addAll(listDefVisitor.getDefineables());

        if (definitions.isEmpty()) {
            return false;
        }

        // Collect what the statement uses
        AttributeUseVisitor attrUseVisitor = new AttributeUseVisitor(getCurrentActor());
        stmt.accept(attrUseVisitor);
        Set<Defineable> uses = new HashSet<>(attrUseVisitor.getDefineables());

        VariableUseVisitor varUseVisitor = new VariableUseVisitor();
        stmt.accept(varUseVisitor);
        uses.addAll(varUseVisitor.getDefineables());

        ListUseVisitor listUseVisitor = new ListUseVisitor();
        stmt.accept(listUseVisitor);
        uses.addAll(listUseVisitor.getDefineables());

        // Check for overlap
        for (Defineable def : definitions) {
            if (uses.contains(def)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the statement at the given index is preceded by a halting statement.
     * The finder treats halting statements as making all variables live, so duplicating
     * after a halt won't be detected as redundant.
     */
    private boolean isPrecededByHaltingStatement(StmtList stmtList, int index) {
        if (index == 0) {
            return false;
        }
        Stmt prevStmt = stmtList.getStmts().get(index - 1);
        return AstNodeUtil.isHaltingStatement(prevStmt);
    }

    /**
     * Set of statement types that can be duplicated to create redundant assignments.
     */
    private static final Set<Class<? extends Stmt>> DUPLICATABLE_TYPES = Set.of(
            SetVariableTo.class, SetXTo.class, SetYTo.class,
            GoToPosXY.class, PointInDirection.class, SetSizeTo.class,
            SetGraphicEffectTo.class, SetSoundEffectTo.class, SetVolumeTo.class,
            SwitchCostumeTo.class, SwitchBackdrop.class, SwitchBackdropAndWait.class,
            GoToPos.class, PointTowards.class,
            Show.class, Hide.class
    );

    /**
     * Checks if a statement is one of the types we can duplicate.
     */
    private boolean isDuplicatable(Stmt stmt) {
        return DUPLICATABLE_TYPES.contains(stmt.getClass());
    }

    /**
     * Inserts a duplicate of the statement immediately BEFORE it.
     * This makes the duplicate redundant because it's immediately overwritten by the original.
     *
     * @param stmtList the statement list containing the statement
     * @param index    the index of the statement to duplicate
     */
    private void addDuplicateStatementMutation(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
            if (index < clonedList.getStmts().size()) {
                Stmt original = clonedList.getStmts().get(index);

                // Clone the statement to create a duplicate
                // Use freshBlockIds=true so the duplicate has a unique block ID
                // (otherwise JSON serialization overwrites blocks with the same ID)
                Stmt duplicate = (Stmt) original.accept(new CloneVisitor(true));
                duplicate.setParentNode(clonedList);

                // Link all nested parent references - required for JSON serialization
                // which checks parent node types (e.g., FixedExpressionJSONCreator.visit(WithExpr))
                ParentVisitor.apply(duplicate);

                // Insert the duplicate immediately BEFORE the original (at the same index)
                // This makes the duplicate redundant, not the original
                clonedList.getStmts().add(index, duplicate);
            }
        });
    }
}
