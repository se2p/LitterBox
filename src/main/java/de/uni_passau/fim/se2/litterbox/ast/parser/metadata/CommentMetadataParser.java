package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CommentMetadataParser {

    public static CommentMetadata parse(String commentId, JsonNode commentNode) {

        String blockId = null;
        if (!(commentNode.get("blockId") instanceof NullNode)) {
            blockId = commentNode.get("blockId").asText();
        }
        int x = commentNode.get(X).asInt();
        int y = commentNode.get(Y).asInt();
        int width = commentNode.get(WIDTH_KEY).asInt();
        int height = commentNode.get(HEIGHT_KEY).asInt();
        boolean minimized = commentNode.get("minimized").asBoolean();
        String text = commentNode.get("text").asText();
        return new CommentMetadata(commentId, blockId, x, y, width, height, minimized, text);
    }
}
