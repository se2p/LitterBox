package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class ResourceMetadata extends AbstractNode implements Metadata {

    private String assetId;
    private String name;
    private String md5ext;
    private String dataFormat;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
