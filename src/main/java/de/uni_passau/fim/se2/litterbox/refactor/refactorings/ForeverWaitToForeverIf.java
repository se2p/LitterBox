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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ForeverWaitToForeverIf extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "forever_wait_to_forever_if";

    private final RepeatForeverStmt loop;
    private final RepeatForeverStmt replacementLoop;

    public ForeverWaitToForeverIf(RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        Preconditions.checkArgument(loop.getStmtList().getStatement(0) instanceof WaitUntil);

        WaitUntil waitUntil = (WaitUntil) loop.getStmtList().getStatement(0);
        List<Stmt> statements = apply(loop.getStmtList()).getStmts();
        statements.remove(0);

        IfThenStmt ifThenStmt = new IfThenStmt(
                apply(waitUntil.getUntil()),
                new StmtList(statements),
                apply(waitUntil.getMetadata())
        );
        replacementLoop = new RepeatForeverStmt(new StmtList(Arrays.asList(ifThenStmt)), apply(loop.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(loop, replacementLoop));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Replaced forever loop with wait:
                %s
                with forever loop with if:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(loop),
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
        ForeverWaitToForeverIf that = (ForeverWaitToForeverIf) o;
        return Objects.equals(loop, that.loop) && Objects.equals(replacementLoop, that.replacementLoop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, replacementLoop);
    }
}
