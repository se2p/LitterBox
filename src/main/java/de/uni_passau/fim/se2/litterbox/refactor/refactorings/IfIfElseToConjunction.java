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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

/*
If A:
  If B:
    C
  Else:
    D

to

If A && B:
  C
If A:
  D
 */
public class IfIfElseToConjunction extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "ififelse_to_conjunction";

    private final IfThenStmt if1;
    private final IfElseStmt if2;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfIfElseToConjunction(IfThenStmt if1) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = (IfElseStmt) if1.getThenStmts().getStatement(0);

        And conjunction = new And(
                apply(if1.getBoolExpr()),
                apply(if2.getBoolExpr()),
                apply(if2.getMetadata()));

        replacementIf1 = new IfThenStmt(conjunction,
                apply(if2.getThenStmts()),
                apply(if1.getMetadata()));

        replacementIf2 = new IfThenStmt(apply(if1.getBoolExpr()),
                apply(if2.getElseStmts()),
                apply(if2.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(if1, replacementIf1, replacementIf2));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if 1:" + System.lineSeparator() + if1.getScratchBlocks() + System.lineSeparator()
                + "Replaced if 2:" + System.lineSeparator() + if2.getScratchBlocks() +  System.lineSeparator()
                + "Replacement if 1:" + System.lineSeparator() + replacementIf1.getScratchBlocks() +  System.lineSeparator()
                + "Replacement if 2:" + System.lineSeparator() + replacementIf2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfIfElseToConjunction)) return false;
        IfIfElseToConjunction that = (IfIfElseToConjunction) o;
        return Objects.equals(if1, that.if1) && Objects.equals(if2, that.if2) && Objects.equals(replacementIf1, that.replacementIf1) && Objects.equals(replacementIf2, that.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(if1, if2, replacementIf1, replacementIf2);
    }
}
