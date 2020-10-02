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
package de.uni_passau.fim.se2.litterbox.ast.model.procedure;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ProcedureDefinition extends AbstractNode implements ASTNode {
    private final ProcedureMetadata metadata;
    private final LocalIdentifier ident;
    private final ParameterDefinitionList parameterDefinitionList;
    private final StmtList stmtList;

    public ProcedureDefinition(LocalIdentifier ident, ParameterDefinitionList parameterDefinitionList,
                               StmtList stmtList, ProcedureMetadata metadata) {
        super(ident, parameterDefinitionList, stmtList, metadata);
        this.ident = ident;
        this.parameterDefinitionList = parameterDefinitionList;
        this.stmtList = stmtList;
        this.metadata = metadata;
    }

    public ProcedureMetadata getMetadata() {
        return metadata;
    }

    public LocalIdentifier getIdent() {
        return ident;
    }

    public ParameterDefinitionList getParameterDefinitionList() {
        return parameterDefinitionList;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
