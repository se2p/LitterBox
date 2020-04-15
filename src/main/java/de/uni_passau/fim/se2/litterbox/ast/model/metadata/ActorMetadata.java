package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public abstract class ActorMetadata extends AbstractNode implements Metadata {
    private List<CommentMetadata> commentsMetadata;
    private List<VariableMetadata> variables;
    private List <ListMetadata> lists;
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
