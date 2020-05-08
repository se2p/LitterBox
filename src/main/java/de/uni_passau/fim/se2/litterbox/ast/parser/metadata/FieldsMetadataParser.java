package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.FieldsMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELD_REFERENCE;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELD_VALUE;

public class FieldsMetadataParser {

    public static FieldsMetadata parse(String key, JsonNode fieldNode) {
        Preconditions.checkArgument(fieldNode instanceof ArrayNode, "The field is not an ArrayNode.");
        String value = fieldNode.get(FIELD_VALUE).asText();
        String reference = null;
        if (fieldNode.has(FIELD_REFERENCE) && !(fieldNode.get(FIELD_REFERENCE) instanceof NullNode)) {
            reference = fieldNode.get(FIELD_REFERENCE).asText();
        }
        return new FieldsMetadata(key, value, reference);
    }
}
