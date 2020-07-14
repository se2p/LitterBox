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

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class StageMetadata extends ActorMetadata {
    private double tempo;
    private double videoTransparency;
    private String videoState;
    private String textToSpeechLanguage;

    public StageMetadata(CommentMetadataList commentsMetadata, VariableMetadataList variables, ListMetadataList lists
            , BroadcastMetadataList broadcasts, int currentCostume, ImageMetadataList costumes,
                         SoundMetadataList sounds, double volume, int layerOrder, double tempo,
                         double videoTransparency,
                         String videoState, String textToSpeechLanguage) {
        super(commentsMetadata, variables, lists, broadcasts, currentCostume, costumes, sounds, volume, layerOrder);
        this.tempo = tempo;
        this.videoTransparency = videoTransparency;
        this.videoState = videoState;
        this.textToSpeechLanguage = textToSpeechLanguage;
    }

    public double getTempo() {
        return tempo;
    }

    public double getVideoTransparency() {
        return videoTransparency;
    }

    public String getVideoState() {
        return videoState;
    }

    public String getTextToSpeechLanguage() {
        return textToSpeechLanguage;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}