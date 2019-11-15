package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.statement.declaration.DeclarationStmtList;
import scratch.newast.model.procedure.ProcedureDefinitionList;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.variable.Identifier;

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
