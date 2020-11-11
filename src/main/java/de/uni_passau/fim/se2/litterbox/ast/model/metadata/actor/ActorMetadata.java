/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class ActorMetadata extends AbstractNode implements Metadata {
    private CommentMetadataList commentsMetadata;
    private VariableMetadataList variables;
    private ListMetadataList lists;
    private BroadcastMetadataList broadcasts;
    private int currentCostume;
    private ImageMetadataList costumes;
    private SoundMetadataList sounds;
    private double volume;
    private int layerOrder;

    public ActorMetadata(CommentMetadataList commentsMetadata,
                         VariableMetadataList variables, ListMetadataList lists, BroadcastMetadataList broadcasts,
                         int currentCostume, ImageMetadataList costumes, SoundMetadataList sounds, double volume,
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

    public double getVolume() {
        return volume;
    }

    public int getLayerOrder() {
        return layerOrder;
    }

    public void addComment(CommentMetadata comment) {
        commentsMetadata.getList().add(comment);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

