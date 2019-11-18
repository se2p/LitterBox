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
import scratch.ast.model.procedure.ProcedureDefinitionList;
import scratch.ast.model.resource.ResourceList;
import scratch.ast.model.statement.declaration.DeclarationStmtList;
import scratch.ast.model.variable.Identifier;
import scratch.ast.visitor.ScratchVisitor;

public class ActorDefinition implements ASTNode {

    private final ImmutableList<ASTNode> children;
    private ActorType actorType;
    private Identifier ident;
    private ResourceList resources;
    private DeclarationStmtList decls;
    private SetStmtList setStmtList;
    private ProcedureDefinitionList procedureDefinitionList;
    private ScriptList scripts;

    public ActorDefinition(ActorType actorType, Identifier ident, ResourceList resources, DeclarationStmtList decls,
        SetStmtList setStmtList,
        ProcedureDefinitionList procedureDefinitionList, ScriptList scripts) {
        this.actorType = actorType;
        this.ident = ident;
        this.resources = resources;
        this.decls = decls;
        this.setStmtList = setStmtList;
        this.procedureDefinitionList = procedureDefinitionList;
        this.scripts = scripts;
        children = ImmutableList.<ASTNode>builder()
            .add(actorType)
            .add(ident)
            .add(resources)
            .add(decls)
            .add(setStmtList)
            .add(procedureDefinitionList)
            .add(scripts)
            .build();
    }

    public ActorType getActorType() {
        return actorType;
    }

    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public ResourceList getResources() {
        return resources;
    }

    public void setResources(ResourceList resources) {
        this.resources = resources;
    }

    public DeclarationStmtList getDecls() {
        return decls;
    }

    public void setDecls(DeclarationStmtList decls) {
        this.decls = decls;
    }

    public ProcedureDefinitionList getProcedureDefinitionList() {
        return procedureDefinitionList;
    }

    public void setProcedureDefinitionList(ProcedureDefinitionList procedureDefinitionList) {
        this.procedureDefinitionList = procedureDefinitionList;
    }

    public SetStmtList getSetStmtList() {
        return setStmtList;
    }

    public ScriptList getScripts() {
        return scripts;
    }

    public void setScripts(ScriptList scripts) {
        this.scripts = scripts;
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
