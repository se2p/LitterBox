package de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class SoundMetadataList extends AbstractNode {
    private List<SoundMetadata> list;

    public SoundMetadataList(List<SoundMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<SoundMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
