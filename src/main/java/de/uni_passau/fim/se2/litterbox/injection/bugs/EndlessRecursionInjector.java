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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.builder.ProcedureBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

/**
 * Injector for the {@link EndlessRecursion} bug.
 *
 * <p>
 * This injector inserts a recursive call at the start of a procedure's body,
 * creating an endless recursion (infinite loop through self-call).
 *
 * <p>
 * Only procedures called in non-tail position are targeted, ensuring the
 * endless recursion has observable effects during execution.
 */
public class EndlessRecursionInjector extends AbstractCustomBlockInjector {

    private final IssueFinder issueFinder = new EndlessRecursion();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    protected void applyMutation(ProcedureDefinition clonedDef, String proccode) {
        CallStmt recursiveCall = ProcedureBuilder.createCallTo(clonedDef, proccode);
        clonedDef.getStmtList().getStmts().addFirst(recursiveCall);
        recursiveCall.setParentNode(clonedDef.getStmtList());
    }
}
