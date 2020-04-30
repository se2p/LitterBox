package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.CommentMetadataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommentMetadataListParser {

    public static CommentMetadataList parse(JsonNode commentsNode) {
        List<CommentMetadata> commentMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = commentsNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            commentMetadataList.add(CommentMetadataParser.parse(current.getKey(), current.getValue()));
        }
        return new CommentMetadataList(commentMetadataList);
    }
}
