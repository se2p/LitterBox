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
if not A
  C

to

If A:
  B
Else:
  C:
 */
public class IfIfNotToIfElse extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "ififnot_to_ifelse";

    private final IfThenStmt ifThen1;
    private final IfThenStmt ifThen2;
    private final IfElseStmt replacementIf;

    public IfIfNotToIfElse(IfThenStmt if1, IfThenStmt if2) {
        this.ifThen1 = Preconditions.checkNotNull(if1);
        this.ifThen2 = Preconditions.checkNotNull(if2);

        replacementIf = new IfElseStmt(apply(if1.getBoolExpr()),
                apply(ifThen1.getThenStmts()),
                apply(ifThen2.getThenStmts()),
                apply(ifThen1.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(ifThen1, Arrays.asList(ifThen2), Arrays.asList(replacementIf)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if1:" + System.lineSeparator() + ifThen1.getScratchBlocks() + System.lineSeparator()
                + "Replaced if 2:" + System.lineSeparator() + ifThen2.getScratchBlocks() +  System.lineSeparator()
                + "Replacement:" + System.lineSeparator() + replacementIf.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfIfNotToIfElse that)) return false;
        return Objects.equals(ifThen1, that.ifThen1)
                && Objects.equals(ifThen2, that.ifThen2)
                && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThen1, ifThen2, replacementIf);
    }
}
