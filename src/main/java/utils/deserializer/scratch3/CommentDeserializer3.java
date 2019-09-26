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
