package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ExistingMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MutationMetadataParser {
    public static MutationMetadata parse(JsonNode mutationNode) {
        String tagName = null;
        if (mutationNode.has(TAG_NAME_KEY)) {
            tagName = mutationNode.get(TAG_NAME_KEY).asText();
        }
        List<String> children = new ArrayList<>();
        if (mutationNode.get(CHILDREN_KEY).size() > 0) {
            System.err.println("THIS MUTATION HAS CHILDREN!");
        }
        String procCode = null;
        if (mutationNode.has(PROCCODE_KEY)) {
            procCode = mutationNode.get(PROCCODE_KEY).asText();
        }
        String argumentIds = null;
        if (mutationNode.has(ARGUMENTIDS_KEY)) {
            argumentIds = mutationNode.get(ARGUMENTIDS_KEY).asText();
        }
        String argumentNames = null;
        if (mutationNode.has(ARGUMENTNAMES_KEY)) {
            argumentNames = mutationNode.get(ARGUMENTNAMES_KEY).asText();
        }
        String argumentDefaults = null;
        if (mutationNode.has(ARGUMENT_DEFAULTS_KEY)) {
            argumentDefaults = mutationNode.get(ARGUMENT_DEFAULTS_KEY).asText();
        }
        boolean warp = false;
        if (mutationNode.has(WARP_KEY)) {
            warp = mutationNode.get(WARP_KEY).asBoolean();
        }
        return new ExistingMutationMetadata(tagName, children, procCode, argumentIds, argumentNames, argumentDefaults, warp);
    }
}
