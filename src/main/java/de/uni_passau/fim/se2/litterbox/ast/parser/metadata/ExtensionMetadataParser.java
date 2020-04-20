package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ExtensionMetadataParser {

    public static ExtensionMetadata parse(JsonNode program) {
        List<String> extensions = new ArrayList<>();
        JsonNode extension = program.get("extensions");
        Preconditions.checkArgument(extension instanceof ArrayNode);
        ArrayNode extensionsArray = (ArrayNode) extension;
        for (int i = 0; i < extensionsArray.size(); i++) {
            extensions.add(extensionsArray.get(i).asText());
        }
        return new ExtensionMetadata(extensions);
    }
}
