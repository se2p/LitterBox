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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ConjunctionToIfs;

/*
If A && B:
  C

to

If A:
  If B:
    C
 */
public class ConjunctionToIfsFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {

        if (ifThenStmt.getBoolExpr() instanceof And) {
            refactorings.add(new ConjunctionToIfs(ifThenStmt));
        }

        visitChildren(ifThenStmt);
    }

    @Override
    public String getName() {
        return ConjunctionToIfs.NAME;
    }
}
