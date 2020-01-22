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
package ast.model;

import ast.model.procedure.ProcedureDefinitionList;
import ast.model.resource.ResourceList;
import ast.model.statement.declaration.DeclarationStmtList;
import ast.model.variable.Identifier;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class ActorDefinition extends AbstractNode {

    private final ActorType actorType;
    private final Identifier ident;
    private final ResourceList resources;
    private final DeclarationStmtList decls;
    private final SetStmtList setStmtList;
    private final ProcedureDefinitionList procedureDefinitionList;
    private final ScriptList scripts;

    public ActorDefinition(ActorType actorType, Identifier ident, ResourceList resources, DeclarationStmtList decls,
        SetStmtList setStmtList, ProcedureDefinitionList procedureDefinitionList, ScriptList scripts) {

        super(actorType, ident, resources, decls, setStmtList, procedureDefinitionList, scripts);

        this.actorType = Preconditions.checkNotNull(actorType);
        this.ident = Preconditions.checkNotNull(ident);
        this.resources = Preconditions.checkNotNull(resources);
        this.decls = Preconditions.checkNotNull(decls);
        this.setStmtList = Preconditions.checkNotNull(setStmtList);
        this.procedureDefinitionList = Preconditions.checkNotNull(procedureDefinitionList);
        this.scripts = Preconditions.checkNotNull(scripts);
    }

    public ActorType getActorType() {
        return actorType;
    }

    public Identifier getIdent() {
        return ident;
    }

    public ResourceList getResources() {
        return resources;
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

}
