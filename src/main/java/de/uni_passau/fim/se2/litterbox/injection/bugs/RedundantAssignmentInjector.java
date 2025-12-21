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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the {@link RedundantAssignment} bug.
 * <p>
 * This injector inserts a duplicate assignment/set statement immediately after
 * an existing one. This makes the original assignment redundant since it's
 * immediately overwritten.
 * <p>
 * Supports: SetVariableTo, SetXTo, SetYTo, GoToPosXY, PointInDirection,
 * SetSizeTo, SetGraphicEffectTo, SetSoundEffectTo, SetVolumeTo, SwitchCostumeTo,
 * SwitchBackdrop, SwitchBackdropAndWait, GoToPos, PointTowards.
 * <p>
 * Transformation:
 * <pre>
 * Before: [set x to (100)]
 * After:  [set x to (100), set x to (100)]  &lt;-- first is now redundant!
 * </pre>
 */
public class RedundantAssignmentInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new RedundantAssignment();
    }

    @Override
    public void visit(StmtList stmtList) {
        for (int i = 0; i < stmtList.getStmts().size(); i++) {
            Stmt stmt = stmtList.getStmts().get(i);
            if (isDuplicatable(stmt)) {
                addDuplicateStatementMutation(stmtList, i);
            }
        }
        super.visit(stmtList);
    }

    /**
     * Set of statement types that can be duplicated to create redundant assignments.
     */
    private static final java.util.Set<Class<? extends Stmt>> DUPLICATABLE_TYPES = java.util.Set.of(
            SetVariableTo.class, SetXTo.class, SetYTo.class,
            GoToPosXY.class, PointInDirection.class, SetSizeTo.class,
            SetGraphicEffectTo.class, SetSoundEffectTo.class, SetVolumeTo.class,
            SwitchCostumeTo.class, SwitchBackdrop.class, SwitchBackdropAndWait.class,
            GoToPos.class, PointTowards.class
    );

    /**
     * Checks if a statement is one of the types we can duplicate.
     */
    private boolean isDuplicatable(Stmt stmt) {
        return DUPLICATABLE_TYPES.contains(stmt.getClass());
    }

    /**
     * Inserts a duplicate of the statement immediately after it.
     *
     * @param stmtList the statement list containing the statement
     * @param index    the index of the statement to duplicate
     */
    private void addDuplicateStatementMutation(StmtList stmtList, int index) {
        mutateClone(stmtList, clonedList -> {
            if (index < clonedList.getStmts().size()) {
                Stmt original = clonedList.getStmts().get(index);

                // Clone the statement to create a duplicate
                Stmt duplicate = (Stmt) original.accept(new CloneVisitor());
                duplicate.setParentNode(clonedList);

                // Insert the duplicate immediately after the original
                clonedList.getStmts().add(index + 1, duplicate);
            }
        });
    }
}
