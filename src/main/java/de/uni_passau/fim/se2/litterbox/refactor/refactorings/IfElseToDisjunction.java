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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/*
If A:
  B
Else:
  If C:
    B

to

If A || C:
  B
 */
public class IfElseToDisjunction extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_disjunction";

    private final IfStmt if1;
    private final IfThenStmt if2;
    private final IfThenStmt replacement;

    /*
    TODO: This refactoring could also handle:
      if A:
        foo
      if B:
        foo

      to

      if A or B:
        foo

      but it is currently only applied to:

      if A:
        foo
      else:
        if B:
          foo
     */
    public IfElseToDisjunction(IfStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        Preconditions.checkArgument(if1.getThenStmts().equals(if2.getThenStmts()));

        if (if1.getBoolExpr().equals(if2.getBoolExpr())) {
            // Silly, but actually observed in practice
            replacement = new IfThenStmt(apply(if1.getBoolExpr()),
                    apply(if1.getThenStmts()),
                    apply(if1.getMetadata()));
        } else {
            Or disjunction = new Or(
                    apply(if1.getBoolExpr()),
                    apply(if2.getBoolExpr()),
                    apply(if2.getMetadata()));

            replacement = new IfThenStmt(disjunction,
                    apply(if1.getThenStmts()),
                    apply(if1.getMetadata()));
        }
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(if1, replacement));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if 1:" + System.lineSeparator() + if1.getScratchBlocks() + System.lineSeparator()
                + "Replaced if 2:" + System.lineSeparator() + if2.getScratchBlocks() +  System.lineSeparator()
                + "Replacement if:" + System.lineSeparator() + replacement.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IfElseToDisjunction)) {
            return false;
        }
        return if1.equals(((IfElseToDisjunction) other).if1)
                && if2.equals(((IfElseToDisjunction) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
