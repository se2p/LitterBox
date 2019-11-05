package scratch.newast.model.resource;

import scratch.newast.model.URI;
import scratch.newast.model.variable.Identifier;

public class ImageResource implements Resource {
    private Identifier ident;
    private URI uri;

    public ImageResource(Identifier ident, URI uri) {
        this.ident = ident;
        this.uri = uri;
    }

    public Identifier getIdent() {
        return ident;
    }

    public URI getUri() {
        return uri;
    }

}