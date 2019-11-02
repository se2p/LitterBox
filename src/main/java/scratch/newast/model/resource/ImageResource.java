package scratch.newast.model.resource;

import scratch.newast.model.variable.Identifier;

public class ImageResource implements Resource {
    private Identifier ident;
    private String uri;

    public ImageResource(Identifier ident, String uri) {
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