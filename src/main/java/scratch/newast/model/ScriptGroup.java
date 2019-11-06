package scratch.newast.model;

import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.variable.Identifier;

public class ScriptGroup {
    private EntityType entityType;
    private Identifier ident;
    private ResourceList resources;
    private DeclarationList decls;
    private ProcedureDeclarationList procedureDeclarationList;
    private ScriptList scripts;

    public ScriptGroup(EntityType entityType, Identifier ident, ResourceList resources, DeclarationList decls, ProcedureDeclarationList procedureDeclarationList, ScriptList scripts) {
        this.entityType = entityType;
        this.ident = ident;
        this.resources = resources;
        this.decls = decls;
        this.procedureDeclarationList = procedureDeclarationList;
        this.scripts = scripts;
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

    public ScriptList getScripts() {
        return scripts;
    }

    public void setScripts(ScriptList scripts) {
        this.scripts = scripts;
    }
}
