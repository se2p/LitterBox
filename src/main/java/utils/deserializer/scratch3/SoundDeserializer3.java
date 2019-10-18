/*
 * Copyright (C) 2019 LitterBox contributors
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
package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class SoundDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Sound> with Sound objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sound> with Sound objects
     */

    static List<Sound> deserialize(JsonNode rootNode) {
        JsonNode globalSounds = rootNode.path("sounds");
        Iterator<JsonNode> elements = globalSounds.elements();
        List<Sound> sounds = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode sound = elements.next();
            Sound scSound = new Sound();
            scSound.setName(sound.get("name").asText());
            scSound.setAssetId(sound.get("assetId").asText());
            scSound.setDataFormat(sound.get("dataFormat").asText());
            sounds.add(scSound);
        }
        return sounds;
    }
}
