package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CallMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PrototypeMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.StopMutation;

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
            throw new IllegalArgumentException("THIS MUTATION HAS CHILDREN!");
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
        boolean hasNext = false;
        if (mutationNode.has(HAS_NEXT_KEY)) {
            hasNext = mutationNode.get(HAS_NEXT_KEY).asBoolean();
        }
        boolean warp = false;
        if (mutationNode.has(WARP_KEY)) {
            warp = mutationNode.get(WARP_KEY).asBoolean();
        }
        if (mutationNode.has(ARGUMENTNAMES_KEY) && mutationNode.has(ARGUMENTNAMES_KEY)) {
            return new PrototypeMutationMetadata(tagName, children, procCode, argumentIds, warp,
                    argumentNames, argumentDefaults);
        } else if (mutationNode.has(HAS_NEXT_KEY)) {
            return new StopMutation(tagName, children, hasNext);
        } else {
            return new CallMutationMetadata(tagName, children, procCode, argumentIds, warp);
        }
    }
}
