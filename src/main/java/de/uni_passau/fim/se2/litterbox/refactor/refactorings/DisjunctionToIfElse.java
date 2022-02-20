/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class DisjunctionToIfElse extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "disjunction_to_ifelse";

    private final IfThenStmt ifStatement;
    private final IfElseStmt replacementIf;

    public DisjunctionToIfElse(IfThenStmt ifStatement) {
        this.ifStatement = Preconditions.checkNotNull(ifStatement);
        Or disjunction = (Or) ifStatement.getBoolExpr();

        IfThenStmt innerIf = new IfThenStmt(apply(disjunction.getOperand2()),
                apply(ifStatement.getThenStmts()), apply(ifStatement.getMetadata()));

        replacementIf = new IfElseStmt(apply(disjunction.getOperand1()),
                apply(ifStatement.getThenStmts()),
                new StmtList(innerIf),
                apply(ifStatement.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        NodeReplacementVisitor replacementVisitor =  new NodeReplacementVisitor(ifStatement, replacementIf);
        return (Program) program.accept(replacementVisitor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if:" + System.lineSeparator() + ifStatement.getScratchBlocks() + System.lineSeparator()
                + "Replacement if:" + System.lineSeparator() + replacementIf.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisjunctionToIfElse)) return false;
        DisjunctionToIfElse that = (DisjunctionToIfElse) o;
        return Objects.equals(ifStatement, that.ifStatement) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifStatement, replacementIf);
    }
}
