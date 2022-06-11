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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Objects;

/*
If A:
  B
Else:
  C:

to

If A:
  B
if not A
  C
 */
public class IfElseToIfIfNot extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_ififnot";

    private final IfElseStmt ifElseStmt;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfElseToIfIfNot(IfElseStmt if1) {
        this.ifElseStmt = Preconditions.checkNotNull(if1);

        replacementIf1 = new IfThenStmt(apply(ifElseStmt.getBoolExpr()),
                apply(ifElseStmt.getThenStmts()),
                apply(ifElseStmt.getMetadata()));

        replacementIf2 = new IfThenStmt(new Not(apply(ifElseStmt.getBoolExpr()),
                apply(ifElseStmt.getMetadata())),
                apply(ifElseStmt.getElseStmts()),
                apply(ifElseStmt.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(ifElseStmt, Arrays.asList(replacementIf1, replacementIf2)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if:" + System.lineSeparator() + ifElseStmt.getScratchBlocks() + System.lineSeparator()
                + "Replacement if 1:" + System.lineSeparator() + replacementIf1.getScratchBlocks() +  System.lineSeparator()
                + "Replacement if 2:" + System.lineSeparator() + replacementIf2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfElseToIfIfNot)) return false;
        IfElseToIfIfNot that = (IfElseToIfIfNot) o;
        return Objects.equals(ifElseStmt, that.ifElseStmt) && Objects.equals(replacementIf1, that.replacementIf1) && Objects.equals(replacementIf2, that.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifElseStmt, replacementIf1, replacementIf2);
    }
}
