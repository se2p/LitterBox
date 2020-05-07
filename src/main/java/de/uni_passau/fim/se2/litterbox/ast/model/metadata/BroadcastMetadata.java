package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class BroadcastMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String broadcastID;
    private String broadcastName;

    public BroadcastMetadata(String broadcastID, String broadcastName, ASTNode... children) {
        super(children);
        this.broadcastID = broadcastID;
        this.broadcastName = broadcastName;
    }

    public BroadcastMetadata( String broadcastID, String broadcastName) {
        super();
        this.broadcastID = broadcastID;
        this.broadcastName = broadcastName;
    }

    public String getBroadcastID() {
        return broadcastID;
    }

    public String getBroadcastName() {
        return broadcastName;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
