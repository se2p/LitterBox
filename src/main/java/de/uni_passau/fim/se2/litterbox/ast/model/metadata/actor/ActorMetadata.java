/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ActorMetadata extends AbstractNode implements Metadata {
    private CommentMetadataList commentsMetadata;
    private int currentCostume;
    private ImageMetadataList costumes;
    private SoundMetadataList sounds;

    public ActorMetadata(CommentMetadataList commentsMetadata, int currentCostume, ImageMetadataList costumes,
                         SoundMetadataList sounds) {
        super(commentsMetadata, costumes, sounds);
        this.commentsMetadata = commentsMetadata;
        this.currentCostume = currentCostume;
        this.costumes = costumes;
        this.sounds = sounds;
    }

    public CommentMetadataList getCommentsMetadata() {
        return commentsMetadata;
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

    public void addComment(CommentMetadata comment) {
        commentsMetadata.getList().add(comment);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}

