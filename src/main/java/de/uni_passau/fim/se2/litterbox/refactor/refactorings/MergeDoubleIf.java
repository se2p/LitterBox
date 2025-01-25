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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
if A:
  B
if A:
  C

to

if A:
  B
  C
 */
public class MergeDoubleIf extends OnlyCodeCloneVisitor implements Refactoring {

    private final IfStmt if1;
    private final IfStmt if2;
    private final IfStmt replacement;
    public static final String NAME = "merge_double_if";

    public MergeDoubleIf(IfStmt if1, IfStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        List<Stmt> mergedListOfStmts = apply(if1.getThenStmts()).getStmts();
        mergedListOfStmts.addAll(apply(if2.getThenStmts()).getStmts());
        StmtList mergedThenStmts = new StmtList(mergedListOfStmts);

        List<Stmt> mergedElseStmt = new ArrayList<>();
        if (if1 instanceof IfElseStmt ifElseStmt) {
            mergedElseStmt.addAll(apply(ifElseStmt.getElseStmts()).getStmts());
        }
        if (if2 instanceof IfElseStmt ifElseStmt) {
            mergedElseStmt.addAll(apply(ifElseStmt.getElseStmts()).getStmts());
        }
        StmtList mergedElseStmts = new StmtList(mergedElseStmt);

        if (mergedElseStmt.isEmpty()) {
            replacement = new IfThenStmt(apply(if1.getBoolExpr()), mergedThenStmts, apply(if1.getMetadata()));
        } else {
            replacement = new IfElseStmt(apply(if1.getBoolExpr()), mergedThenStmts, mergedElseStmts, apply(if1.getMetadata()));
        }
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(if1, Arrays.asList(if2), Arrays.asList(replacement)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Replaced if 1:
                %s
                Replaced if 2:
                %s
                Replacement:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(if1),
                ScratchBlocksVisitor.of(if2),
                ScratchBlocksVisitor.of(replacement)
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MergeDoubleIf)) {
            return false;
        }
        return if1.equals(((MergeDoubleIf) other).if1)
                && if2.equals(((MergeDoubleIf) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
