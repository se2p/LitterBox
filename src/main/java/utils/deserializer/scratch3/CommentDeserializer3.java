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
import scratch.data.Comment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CommentDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Comment> with Comment objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Comment> with Comment objects
     */
    static List<Comment> deserialize(JsonNode rootNode) {
        JsonNode globalComments = rootNode.path("comments");
        Iterator<String> elements = globalComments.fieldNames();
        List<Comment> comments = new ArrayList<>();
        while (elements.hasNext()) {
            String id = elements.next();
            JsonNode n = globalComments.get(id);
            Comment comment = new Comment();
            comment.setId(id);
            comment.setContent(n.get("text").textValue());
            double[] pos = new double[2];
            pos[0] = n.get("x").asDouble();
            pos[1] = n.get("y").asDouble();
            comment.setPosition(pos);
            comments.add(comment);
        }
        return comments;
    }

}
