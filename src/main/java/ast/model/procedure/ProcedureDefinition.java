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
package ast.model.procedure;

import ast.model.ASTNode;
import ast.model.AbstractNode;
import ast.model.StmtList;
import ast.model.variable.Identifier;
import ast.visitor.ScratchVisitor;

public class ProcedureDefinition extends AbstractNode implements ASTNode {

    private final Identifier ident;
    private final ParameterList parameterList;
    private final StmtList stmtList;

    public ProcedureDefinition(Identifier ident, ParameterList parameterList, StmtList stmtList) {
        super(ident, parameterList, stmtList);
        this.ident = ident;
        this.parameterList = parameterList;
        this.stmtList = stmtList;
    }

    public Identifier getIdent() {
        return ident;
    }

    public ParameterList getParameterList() {
        return parameterList;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
