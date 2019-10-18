/**
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
package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class SoundDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Sound> with Sound objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sound> with Sound objects
     */

    static List<Sound> deserialize(JsonNode rootNode) {
        JsonNode globalVariables = rootNode.path("sounds");
        Iterator<JsonNode> elements = globalVariables.elements();
        List<Sound> sounds = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode variable = elements.next();
            Sound scSound = new Sound();
            scSound.setName(variable.get("soundName").asText());
            scSound.setAssetId(variable.get("soundID").asText());
            sounds.add(scSound);
        }
        return sounds;
    }
}
