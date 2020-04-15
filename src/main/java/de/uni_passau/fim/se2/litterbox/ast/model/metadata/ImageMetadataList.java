package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ImageMetadataList extends AbstractNode {
    private List<ImageMetadata> list;

    public ImageMetadataList(List<ImageMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<ImageMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
