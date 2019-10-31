package scratch.newast.model;

import scratch.newast.model.ressource.Ressource;
import scratch.newast.model.variable.Identifier;

import java.util.List;

public class ScriptGroup {
    private Identifier ident;
    private List<Ressource> ressources;
    private List<Declaration> decls;
    private List<Script> scripts;

    public ScriptGroup(Identifier ident, List<Ressource> ressources, List<Declaration> decls, List<Script> scripts) {
        this.ident = ident;
        this.ressources = ressources;
        this.decls = decls;
        this.scripts = scripts;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public List<Ressource> getRessources() {
        return ressources;
    }

    public void setRessources(List<Ressource> ressources) {
        this.ressources = ressources;
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