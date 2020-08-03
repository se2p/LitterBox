/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.Collections;
import java.util.Set;

public class CallReturnNode extends CFGNode {

    private CallStmt originalCall;

    public CallReturnNode(CallStmt callNode) {
        this.originalCall = callNode;
    }

    @Override
    public ASTNode getASTNode() {
        return originalCall;
    }

    @Override
    public Set<Definition> getDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public Set<Use> getUses() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Return " + originalCall.getIdent().getName() + "@" + System.identityHashCode(originalCall);
    }
}
