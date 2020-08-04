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

import java.util.Objects;

public class ProcedureNode extends CFGNode {

    private String procedureName;

    private String actorName;

    public ProcedureNode(String name, String actorName) {
        this.procedureName = name;
        this.actorName = actorName;
    }

    @Override
    public ASTNode getASTNode() {
        return null;
    }

    @Override
    public String toString() {
        return "Custom Block: " + actorName + "." + procedureName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcedureNode)) {
            return false;
        }
        ProcedureNode that = (ProcedureNode) o;
        return Objects.equals(procedureName, that.procedureName)
                && Objects.equals(actorName, that.actorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(procedureName, actorName);
    }
}
