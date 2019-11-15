package scratch.newast.model.resource;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.URI;
import scratch.newast.model.variable.Identifier;

public class ImageResource implements Resource {
    private Identifier ident;
    private URI uri;
    private final ImmutableList<ASTNode> children;

    public ImageResource(Identifier ident, URI uri) {
        this.ident = ident;
        this.uri = uri;
        children = ImmutableList.<ASTNode>builder().add(ident).add(uri).build();
    }

    public Identifier getIdent() {
        return ident;
    }

    public URI getUri() {
        return uri;
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