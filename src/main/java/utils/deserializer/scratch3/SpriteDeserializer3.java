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
import scratch.data.*;
import scratch.structure.Sprite;

import java.util.List;

public class SpriteDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Sprite> with Sprite objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sprite> with Sprite objects
     */
    public static Sprite deserialize(JsonNode rootNode) {
        String name = rootNode.get("name").asText();
        //System.out.println(name);
        List<Script> scripts = ScriptDeserializer3.deserialize(rootNode);
        //System.out.println(scripts);
        List<Comment> comments = CommentDeserializer3.deserialize(rootNode);
        List<ScVariable> variables = VariableListDeserializer3.deserialize(rootNode);
        List<ScList> lists = ListDeserializer3.deserialize(rootNode);
        List<Costume> costumes = CostumeDeserializer3.deserialize(rootNode);
        List<Sound> sounds = SoundDeserializer3.deserialize(rootNode);
        int initCostume = rootNode.get("currentCostume").asInt();
        double[] position = {rootNode.get("x").asInt(), rootNode.get("y").asInt()};
        double rotation = rootNode.get("direction").asDouble();
        String rotationStyle = rootNode.get("rotationStyle").asText();
        int size = rootNode.get("size").asInt();
        return new Sprite(name, scripts, comments, variables, lists, costumes, sounds,
                initCostume, rootNode.get("blocks"),  position, rotation, rotationStyle, size);

    }

}
