package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class BroadcastMetadata extends AbstractNode implements Metadata {
    private String broadcastID;
    private String broadcastName;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
