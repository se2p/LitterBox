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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoopUnrolling extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "loop_unrolling";

    private final RepeatTimesStmt loop;
    private final int value;
    private final List<Stmt> unrolledStmts;

    public LoopUnrolling(RepeatTimesStmt loop, int value) {
        this.loop = Preconditions.checkNotNull(loop);
        this.value = value;
        unrolledStmts = new ArrayList<>();

        for (int i = 0; i < value; i++) {
            unrolledStmts.addAll(apply(loop.getStmtList()).getStmts());
        }
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(loop, unrolledStmts));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Unrolled loop:" + System.lineSeparator() + loop.getScratchBlocks() + System.lineSeparator()
                + this.value + " times" +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoopUnrolling)) return false;
        LoopUnrolling that = (LoopUnrolling) o;
        return value == that.value && Objects.equals(loop, that.loop) && Objects.equals(unrolledStmts, that.unrolledStmts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, value, unrolledStmts);
    }
}
