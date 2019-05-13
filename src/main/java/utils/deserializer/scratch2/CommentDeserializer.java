package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Comment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class CommentDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Comment> with Comment objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Comment> with Comment objects
     */
    static List<Comment> deserialize(JsonNode rootNode) {
        JsonNode globalComments = rootNode.path("scriptComments");
        Iterator<JsonNode> elements = globalComments.elements();
        List<Comment> comments = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode c = elements.next();
            List<String> comm = new ArrayList<>();
            if (c.isArray()) {
                for (final JsonNode objNode : c) {
                    comm.add(objNode.asText());
                }
            }
            Comment comment = new Comment();
            comment.setContent(comm.get(6));
            double[] pos = {Double.valueOf(comm.get(0)), Double.valueOf(comm.get(1))};
            comment.setPosition(pos);
            comments.add(comment);
        }
        return comments;
    }

}
