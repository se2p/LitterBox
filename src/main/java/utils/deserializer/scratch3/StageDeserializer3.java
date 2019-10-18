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
import scratch.structure.Stage;

import java.util.List;

public class StageDeserializer3 {

    /**
     * Deserialize the JSON String and creating a Stage object
     * @param rootNode the JsonNode to deserialize
     * @return a Stage object
     */
    public static Stage deserialize(JsonNode rootNode) {
        String name = rootNode.get("name").asText();
        List<Script> scripts = ScriptDeserializer3.deserialize(rootNode);
        List<Comment> comments = CommentDeserializer3.deserialize(rootNode);
        List<ScVariable> variables = VariableListDeserializer3.deserialize(rootNode);
        List<ScList> lists = ListDeserializer3.deserialize(rootNode);
        List<Costume> costumes = CostumeDeserializer3.deserialize(rootNode);
        List<Sound> sounds = SoundDeserializer3.deserialize(rootNode);
        int initCostume = rootNode.get("currentCostume").asInt();

        return new Stage(name, scripts, comments, variables, lists, costumes, sounds, initCostume, rootNode.get("blocks"));
    }
}
