/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.LoopUnrolling;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

public class LoopUnrollingFinder extends AbstractRefactoringFinder {

    private static final int MAX_UNROLLING = PropertyLoader.getSystemIntProperty("refactoring.loop_unrolling.max_loopunrolling");

    @Override
    public void visit(RepeatTimesStmt loop) {
        NumExpr expr = loop.getTimes();
        if (expr instanceof NumberLiteral) {
            // The Scratch UI prevents decimal numbers so this cast is safe
            int value = (int)((NumberLiteral) expr).getValue();
            if (value <= MAX_UNROLLING) {
                refactorings.add(new LoopUnrolling(loop, value));
            }
        }

        visitChildren(loop);
    }


    @Override
    public String getName() {
        return LoopUnrolling.NAME;
    }
}
