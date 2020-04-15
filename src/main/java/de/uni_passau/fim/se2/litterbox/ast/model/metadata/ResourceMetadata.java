package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class ResourceMetadata extends AbstractNode implements Metadata {

    private String assetId;
    private String name;
    private String md5ext;
    private String dataFormat;

    public ResourceMetadata(String assetId, String name, String md5ext, String dataFormat) {
        super();
        this.assetId = assetId;
        this.name = name;
        this.md5ext = md5ext;
        this.dataFormat = dataFormat;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getName() {
        return name;
    }

    public String getMd5ext() {
        return md5ext;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
