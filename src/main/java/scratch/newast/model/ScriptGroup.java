package scratch.newast.model;

import scratch.newast.model.resource.Resource;
import scratch.newast.model.variable.Identifier;

import java.util.List;

public class ScriptGroup {
    private Identifier ident;
    private List<Resource> resources;
    private List<Declaration> decls;
    private List<Script> scripts;

    public ScriptGroup(Identifier ident, List<Resource> resources, List<Declaration> decls, List<Script> scripts) {
        this.ident = ident;
        this.resources = resources;
        this.decls = decls;
        this.scripts = scripts;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Declaration> getDecls() {
        return decls;
    }

    public void setDecls(List<Declaration> decls) {
        this.decls = decls;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }
}