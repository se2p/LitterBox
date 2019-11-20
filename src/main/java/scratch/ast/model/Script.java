/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.ast.model;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import scratch.ast.model.event.Event;
import scratch.ast.visitor.ScratchVisitor;

public class Script implements ASTNode {

    private final Event event;
    private final StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public Script(Event event, StmtList stmtList) {
        this.event = Preconditions.checkNotNull(event);
        this.stmtList = Preconditions.checkNotNull(stmtList);
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        this.children = builder.add(this.event, this.stmtList).build();
    }

    public Event getEvent() {
        return event;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}