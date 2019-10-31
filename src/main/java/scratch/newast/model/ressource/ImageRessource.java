package scratch.newast.model.ressource;

import scratch.newast.model.variable.Identifier;

public class ImageRessource extends Ressource {
    private Identifier ident;
    private String uri;

    public ImageRessource(Identifier ident, String uri) {
        this.ident = ident;
        this.uri = uri;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}