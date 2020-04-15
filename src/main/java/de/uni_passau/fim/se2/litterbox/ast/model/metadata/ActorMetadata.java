package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class ActorMetadata extends AbstractNode implements Metadata {
    private CommentMetadataList commentsMetadata;
    private VariableMetadataList variables;
    private ListMetadataList lists;
    private BroadcastMetadataList broadcasts;
    private int currentCostume;
    private ImageMetadataList costumes;
    private SoundMetadataList sounds;
    private int volume;
    private int layerOrder;

    public ActorMetadata(CommentMetadataList commentsMetadata,
                         VariableMetadataList variables, ListMetadataList lists, BroadcastMetadataList broadcasts,
                         int currentCostume, ImageMetadataList costumes, SoundMetadataList sounds, int volume,
                         int layerOrder) {
        super(commentsMetadata, variables, lists, broadcasts, costumes, sounds);
        this.commentsMetadata = commentsMetadata;
        this.variables = variables;
        this.lists = lists;
        this.broadcasts = broadcasts;
        this.currentCostume = currentCostume;
        this.costumes = costumes;
        this.sounds = sounds;
        this.volume = volume;
        this.layerOrder = layerOrder;
    }

    public CommentMetadataList getCommentsMetadata() {
        return commentsMetadata;
    }

    public VariableMetadataList getVariables() {
        return variables;
    }

    public ListMetadataList getLists() {
        return lists;
    }

    public BroadcastMetadataList getBroadcasts() {
        return broadcasts;
    }

    public int getCurrentCostume() {
        return currentCostume;
    }

    public ImageMetadataList getCostumes() {
        return costumes;
    }

    public SoundMetadataList getSounds() {
        return sounds;
    }

    public int getVolume() {
        return volume;
    }

    public int getLayerOrder() {
        return layerOrder;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
