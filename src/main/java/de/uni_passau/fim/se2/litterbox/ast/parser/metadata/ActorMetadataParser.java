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
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorMetadataParser {

    public static ActorMetadata parse(JsonNode actorNode) {
        CommentMetadataList commentsMetadata = null;
        if (actorNode.has(COMMENTS_KEY)) {
            commentsMetadata = CommentMetadataListParser.parse(actorNode.get(COMMENTS_KEY));
        }
        VariableMetadataList variables = null;
        if (actorNode.has(VARIABLES_KEY)) {
            variables = VariableMetadataListParser.parse(actorNode.get(VARIABLES_KEY));
        }
        ListMetadataList lists = null;
        if (actorNode.has(LISTS_KEY)) {
            lists = ListMetadataListParser.parse(actorNode.get(LISTS_KEY));
        }
        BroadcastMetadataList broadcasts = null;
        if (actorNode.has(BROADCASTS_KEY)) {
            broadcasts = BroadcastMetadataListParser.parse(actorNode.get(BROADCASTS_KEY));
        }
        int currentCostume = 0;
        if (actorNode.has(CURRENT_COSTUME_KEY)) {
            currentCostume = actorNode.get(CURRENT_COSTUME_KEY).asInt();
        }
        ImageMetadataList costumes = null;
        if (actorNode.has(COSTUMES_KEY)) {
            costumes = ImageMetadataListParser.parse(actorNode.get(COSTUMES_KEY));
        }
        SoundMetadataList sounds = null;
        if (actorNode.has(SOUNDS_KEY)) {
            sounds = SoundMetadataListParser.parse(actorNode.get(SOUNDS_KEY));
        }
        double volume = 0;
        if (actorNode.has(VOLUME_KEY)) {
            volume = actorNode.get(VOLUME_KEY).asDouble();
        }
        int layerOrder = 0;
        if (actorNode.has(LAYERORDER_KEY)) {
            layerOrder = actorNode.get(LAYERORDER_KEY).asInt();
        }
        if (actorNode.get(IS_STAGE_KEY).asBoolean()) {
            double tempo = 0;
            if (actorNode.has(TEMPO_KEY)) {
                tempo = actorNode.get(TEMPO_KEY).asDouble();
            }
            double videoTransparency = 0;
            if (actorNode.has(VIDTRANSPARENCY_KEY)) {
                videoTransparency = actorNode.get(VIDTRANSPARENCY_KEY).asDouble();
            }
            String videoState = null;
            if (actorNode.has(VIDSTATE_KEY)) {
                videoState = actorNode.get(VIDSTATE_KEY).asText();
            }
            String textToSpeechLanguage = null;
            if (actorNode.has(TEXT_TO_SPEECH_KEY) && !(actorNode.get(TEXT_TO_SPEECH_KEY) instanceof NullNode)) {
                textToSpeechLanguage = actorNode.get(TEXT_TO_SPEECH_KEY).asText();
            }
            return new StageMetadata(commentsMetadata, variables, lists, broadcasts, currentCostume, costumes, sounds,
                    volume, layerOrder, tempo, videoTransparency, videoState, textToSpeechLanguage);
        } else {
            boolean visible = false;
            if (actorNode.has(VISIBLE_KEY)) {
                visible = actorNode.get(VISIBLE_KEY).asBoolean();
            }
            double x = 0;
            if (actorNode.has(X_KEY)) {
                x = actorNode.get(X_KEY).asDouble();
            }
            double y = 0;
            if (actorNode.has(Y_KEY)) {
                y = actorNode.get(Y_KEY).asDouble();
            }
            double size = 0;
            if (actorNode.has(SIZE_KEY)) {
                size = actorNode.get(SIZE_KEY).asDouble();
            }
            double direction = 0;
            if (actorNode.has(DIRECTION_KEY)) {
                direction = actorNode.get(DIRECTION_KEY).asDouble();
            }
            boolean draggable = false;
            if (actorNode.has(DRAG_KEY)) {
                draggable = actorNode.get(DRAG_KEY).asBoolean();
            }
            String rotationStyle = null;
            if (actorNode.has(ROTATIONSTYLE_KEY)) {
                rotationStyle = actorNode.get(ROTATIONSTYLE_KEY).asText();
            }
            return new SpriteMetadata(commentsMetadata, variables, lists, broadcasts, currentCostume, costumes, sounds,
                    volume, layerOrder, visible, x, y, size, direction, draggable, rotationStyle);
        }
    }
}
