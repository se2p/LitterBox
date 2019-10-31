package scratch.newast.model;

import scratch.newast.model.variable.Identifier;

import java.util.List;

public class Program {
    private Identifier ident;
    private ScriptGroup stage;
    private List<Script> sprites;

    public Program(Identifier ident, ScriptGroup stage, List<Script> sprites) {
        this.ident = ident;
        this.stage = stage;
        this.sprites = sprites;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public ScriptGroup getStage() {
        return stage;
    }

    public void setStage(ScriptGroup stage) {
        this.stage = stage;
    }

    public List<Script> getSprites() {
        return sprites;
    }

    public void setSprites(List<Script> sprites) {
        this.sprites = sprites;
    }
}