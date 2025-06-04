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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeEventsIntoForever extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "merge_event_handler";
    private List<Script> scriptList;

    private Script replacement;

    public MergeEventsIntoForever(List<Script> eventList) {
        Preconditions.checkNotNull(eventList);
        Preconditions.checkArgument(!eventList.isEmpty());
        this.scriptList = eventList;

        // Create statement list with if then blocks for each event script.
        ArrayList<Stmt> ifThenArrayList = new ArrayList<>();
        for (Script script : scriptList) {
            ifThenArrayList.add(getIfStmtFromEventScript(script));
        }

        // Create forever loop.
        // TODO: Reuse stmts.get(0).getMetadata()?
        StmtList foreverStmt = new StmtList(new RepeatForeverStmt(new StmtList(ifThenArrayList),
                NonDataBlockMetadata.emptyNonBlockMetadata()));

        GreenFlag greenFlag = new GreenFlag(apply(scriptList.get(0).getEvent().getMetadata()));
        replacement = new Script(greenFlag, foreverStmt);
    }

    private Stmt getIfStmtFromEventScript(Script script) {
        Preconditions.checkArgument(script.getEvent() instanceof KeyPressed);
        Preconditions.checkArgument(script.getStmtList().getNumberOfStatements() > 0);

        Key pressed = ((KeyPressed) script.getEvent()).getKey();
        NonDataBlockMetadata keyMetaData = NonDataBlockMetadata.emptyNonBlockMetadata();
        IsKeyPressed expression = new IsKeyPressed(apply(pressed), keyMetaData);
        List<Stmt> stmts = apply(script.getStmtList()).getStmts();

        // TODO: Reuse stmts.get(0).getMetadata()?
        return new IfThenStmt(expression, new StmtList(stmts), NonDataBlockMetadata.emptyNonBlockMetadata());
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        boolean inserted = false;
        for (Script currentScript : node.getScriptList()) {
            if (scriptList.contains(currentScript)) {
                if (!inserted) {
                    scripts.add(replacement);
                    inserted = true;
                }
            } else {
                scripts.add(apply(currentScript));
            }
        }
        return new ScriptList(scripts);
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MergeEventsIntoForever that)) {
            return false;
        }
        boolean equals = true;

        if (this.scriptList.size() != that.scriptList.size()) {
            return false;
        }

        for (int i = 0; i < this.scriptList.size(); i++) {
            if (!this.scriptList.get(i).equals(that.scriptList.get(i))) {
                equals = false;
                break;
            }
        }
        return equals && Objects.equals(this.replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scriptList, replacement);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        for (Script script : scriptList) {
            sb.append(System.lineSeparator());
            sb.append(ScratchBlocksVisitor.of(script));
            sb.append(" and ");
        }
        sb.delete(sb.length() - 6, sb.length() - 1);

        return String.format("""
                %s
                Merging %s
                to:
                %s
                """,
                NAME,
                sb,
                ScratchBlocksVisitor.of(replacement)
        );
    }

}
