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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.List;
import java.util.Objects;

public class InlineLoopCondition extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "inline_loop_condition";

    private final UntilStmt untilLoop;
    private final TerminationStmt terminationStmt;
    private final RepeatForeverStmt replacementLoop;

    public InlineLoopCondition(UntilStmt untilLoop) {
        this(untilLoop, null);
    }

    public InlineLoopCondition(UntilStmt untilLoop, TerminationStmt terminationStmt) {
        this.untilLoop = Preconditions.checkNotNull(untilLoop);
        if (terminationStmt == null) {
            // TODO: Find a way to do this without all the metadata handling
            BlockMetadata blockMetadata = new NonDataBlockMetadata(
                    null, CloneVisitor.generateUID(), false, new NoMutationMetadata()
            );
            this.terminationStmt = new StopThisScript(blockMetadata);
        } else {
            this.terminationStmt = apply(terminationStmt);
        }

        IfThenStmt ifThenStmt = new IfThenStmt(
                apply(untilLoop.getBoolExpr()),
                new StmtList(this.terminationStmt),
                apply(untilLoop.getMetadata())
        );
        List<Stmt> loopBody = apply(untilLoop.getStmtList()).getStmts();
        loopBody.add(ifThenStmt);

        replacementLoop = new RepeatForeverStmt(new StmtList(loopBody), apply(untilLoop.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(untilLoop, replacementLoop));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Replaced until loop:
                %s
                with forever loop:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(untilLoop),
                ScratchBlocksVisitor.of(replacementLoop)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InlineLoopCondition that = (InlineLoopCondition) o;
        return Objects.equals(untilLoop, that.untilLoop)
                && Objects.equals(terminationStmt, that.terminationStmt)
                && Objects.equals(replacementLoop, that.replacementLoop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(untilLoop, terminationStmt, replacementLoop);
    }
}
