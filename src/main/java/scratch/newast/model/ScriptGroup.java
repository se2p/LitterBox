package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.variable.Identifier;

public class ScriptGroup implements ASTNode {

    private final ImmutableList<ASTNode> children;
    private EntityType entityType;
    private Identifier ident;
    private ResourceList resources;
    private DeclarationList decls;
    private ProcedureDeclarationList procedureDeclarationList;
    private ScriptList scripts;
    private SetStmtList setStmtList;

    public ScriptGroup(EntityType entityType, Identifier ident, ResourceList resources, DeclarationList decls,
        SetStmtList setStmtList,
        ProcedureDeclarationList procedureDeclarationList, ScriptList scripts) {
        this.entityType = entityType;
        this.ident = ident;
        this.resources = resources;
        this.decls = decls;
        this.setStmtList = setStmtList;
        this.procedureDeclarationList = procedureDeclarationList;
        this.scripts = scripts;
        children = ImmutableList.<ASTNode>builder()
            .add(entityType)
            .add(ident)
            .add(resources)
            .add(decls)
            .add(setStmtList)
            .add(procedureDeclarationList)
            .add(scripts)
            .build();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
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

    public DeclarationList getDecls() {
        return decls;
    }

    public void setDecls(DeclarationList decls) {
        this.decls = decls;
    }

    public ProcedureDeclarationList getProcedureDeclarationList() {
        return procedureDeclarationList;
    }

    public void setProcedureDeclarationList(ProcedureDeclarationList procedureDeclarationList) {
        this.procedureDeclarationList = procedureDeclarationList;
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
