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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtractLoopCondition extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "extract_loop_condition";

    private final RepeatForeverStmt foreverLoop;
    private final IfThenStmt ifThenStmt;
    private final List<Stmt> replacement = new ArrayList<>();

    public ExtractLoopCondition(RepeatForeverStmt foreverLoop, IfThenStmt ifThenStmt) {
        this.foreverLoop = Preconditions.checkNotNull(foreverLoop);
        this.ifThenStmt = Preconditions.checkNotNull(ifThenStmt);
        TerminationStmt stopStmt = (TerminationStmt) ifThenStmt.getThenStmts().getStatement(0);

        List<Stmt> remainingStmts = foreverLoop.getStmtList().getStmts().stream().filter(s -> s != ifThenStmt).collect(Collectors.toList());

        UntilStmt replacementLoop = new UntilStmt(apply(ifThenStmt.getBoolExpr()), new StmtList(applyList(remainingStmts)), apply(foreverLoop.getMetadata()));
        replacement.add(replacementLoop);
        if (!(stopStmt instanceof StopThisScript)) {
            replacement.add(apply(stopStmt));
        }
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(foreverLoop, replacement));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced loop:" + System.lineSeparator() + foreverLoop.getScratchBlocks() + System.lineSeparator()
                + "with until loop:" + System.lineSeparator() + replacement.get(0).getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractLoopCondition that = (ExtractLoopCondition) o;
        return Objects.equals(foreverLoop, that.foreverLoop) && Objects.equals(ifThenStmt, that.ifThenStmt) && Objects.equals(replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreverLoop, ifThenStmt, replacement);
    }
}
