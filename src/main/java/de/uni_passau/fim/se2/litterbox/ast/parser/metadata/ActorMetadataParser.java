/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorMetadataParser {

    public static ActorMetadata parse(JsonNode actorNode) {
        CommentMetadataList commentsMetadata = null;
        if (actorNode.has(COMMENTS_KEY)) {
            commentsMetadata = CommentMetadataListParser.parse(actorNode.get(COMMENTS_KEY));
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
        if (actorNode.get(IS_STAGE_KEY).asBoolean()) {
            String textToSpeechLanguage = null;
            if (actorNode.has(TEXT_TO_SPEECH_KEY) && !(actorNode.get(TEXT_TO_SPEECH_KEY) instanceof NullNode)) {
                textToSpeechLanguage = actorNode.get(TEXT_TO_SPEECH_KEY).asText();
            }
            return new StageMetadata(commentsMetadata, currentCostume, costumes, sounds, textToSpeechLanguage);
        } else {
            return new ActorMetadata(commentsMetadata, currentCostume, costumes, sounds);
        }
    }
}
