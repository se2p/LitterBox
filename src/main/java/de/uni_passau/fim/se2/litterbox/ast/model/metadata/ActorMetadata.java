package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public abstract class ActorMetadata extends AbstractNode implements Metadata {
    private List<CommentMetadata> commentsMetadata;
    private boolean isStage; // TODO do we need this or is it stored in the Actor anyways -> in actor
    private String name; // TODO do we need this or is it stored in the Actor anyways -> in actor
    private List<VariableMetadata> variables; // TODO do we need this or is it stored in the Actor anyways -> we need
    // it because here the current value is stored
    private List <ListMetadata> lists; // TODO do we need this or is it stored in the Actor anyways -> we need this,
    // because here the values are stores
    private List <BroadcastMetadata> broadcasts;
    private int currentCostume;
    private List<ImageMetadata> costumes;
    private List<SoundMetadata> sounds;
    private int volume;
    private int layerOrder;


    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
