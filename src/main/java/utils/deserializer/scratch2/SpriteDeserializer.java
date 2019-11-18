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
package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import scratch.data.Comment;
import scratch.data.Costume;
import scratch.data.ScList;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.data.Sound;
import scratch.structure.Sprite;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
public class SpriteDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Sprite> with Sprite objects
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sprite> with Sprite objects
     */
    public static List<Sprite> deserialize(JsonNode rootNode) {
        List<Sprite> sprites = new ArrayList<>();
        JsonNode globalSprites = rootNode.path("children");
        Iterator<JsonNode> elements = globalSprites.elements();
        while (elements.hasNext()) {
            JsonNode c = elements.next();
            if(c.has("objName")) {
                String name = c.get("objName").asText();
                List<Script> scripts = ScriptDeserializer.deserialize(c);
                List<Comment> comments = CommentDeserializer.deserialize(c);
                List<ScVariable> variables = VariableListDeserializer.deserialize(c);
                List<ScList> lists = ListDeserializer.deserialize(c);
                List<Costume> costumes = CostumeDeserializer.deserialize(c);
                List<Sound> sounds = SoundDeserializer.deserialize(c);
                int initCostume = c.get("currentCostumeIndex").asInt();
                double[] position = {c.get("scratchX").asInt(), c.get("scratchY").asInt()};
                double rotation = c.get("direction").asDouble();
                String rotationStyle = c.get("rotationStyle").asText();
                int size = c.get("scale").asInt();
                sprites.add(new Sprite(name, scripts, comments, variables, lists, costumes, sounds,
                        initCostume, rootNode, position, rotation, rotationStyle, size));
            }
        }

        return sprites;
    }

}
