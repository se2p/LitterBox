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
package de.uni_passau.fim.se2.litterbox.ast.model;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ActorDefinition extends AbstractNode {

    private final ActorType actorType;
    private final LocalIdentifier ident;
    private final DeclarationStmtList decls;
    private final SetStmtList setStmtList;
    private final ProcedureDefinitionList procedureDefinitionList;
    private final ScriptList scripts;
    private final ActorMetadata metadata;

    public ActorDefinition(ActorType actorType, LocalIdentifier ident, DeclarationStmtList decls,
                           SetStmtList setStmtList, ProcedureDefinitionList procedureDefinitionList,
                           ScriptList scripts, ActorMetadata metadata) {

        super(actorType, ident, decls, setStmtList, procedureDefinitionList, scripts, metadata);

        this.actorType = Preconditions.checkNotNull(actorType);
        this.ident = Preconditions.checkNotNull(ident);
        this.decls = Preconditions.checkNotNull(decls);
        this.setStmtList = Preconditions.checkNotNull(setStmtList);
        this.procedureDefinitionList = Preconditions.checkNotNull(procedureDefinitionList);
        this.scripts = Preconditions.checkNotNull(scripts);
        this.metadata = metadata;
    }

    public ActorMetadata getActorMetadata() {
        return metadata;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public LocalIdentifier getIdent() {
        return ident;
    }

    public DeclarationStmtList getDecls() {
        return decls;
    }

    public ProcedureDefinitionList getProcedureDefinitionList() {
        return procedureDefinitionList;
    }

    public SetStmtList getSetStmtList() {
        return setStmtList;
    }

    public ScriptList getScripts() {
        return scripts;
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
