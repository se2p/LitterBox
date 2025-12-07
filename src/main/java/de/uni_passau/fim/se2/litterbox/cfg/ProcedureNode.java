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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.Objects;

public class ProcedureNode extends CFGNode {

    private String procedureName;

    private String actorName;

    private ProcedureDefinition procedureDefinition;

    public ProcedureNode(String name, String actorName) {
        this(name, actorName, null);
    }

    public ProcedureNode(String name, String actorName, ProcedureDefinition procedureDefinition) {
        this.procedureName = name;
        this.actorName = actorName;
        this.procedureDefinition = procedureDefinition;
    }

    public void setProcedureDefinition(ProcedureDefinition procedureDefinition) {
        this.procedureDefinition = procedureDefinition;
    }

    @Override
    public ASTNode getASTNode() {
        return procedureDefinition;
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
        if (!(o instanceof ProcedureNode that)) {
            return false;
        }
        return Objects.equals(procedureName, that.procedureName)
                && Objects.equals(actorName, that.actorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(procedureName, actorName);
    }
}
