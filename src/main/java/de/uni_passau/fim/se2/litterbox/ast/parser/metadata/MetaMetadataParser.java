package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MetaMetadataParser {

    public static MetaMetadata parse(JsonNode metaNode) {
        String vm = "";
        String semver = "";
        String agent = "";
        if (metaNode.has(SEMVER_KEY)){
            semver = metaNode.get(SEMVER_KEY).asText();
        }
        if (metaNode.has(VM_KEY)){
            vm = metaNode.get(VM_KEY).asText();
        }
        if (metaNode.has(AGENT_KEY)){
            agent = metaNode.get(AGENT_KEY).asText();
        }
        return new MetaMetadata(semver, vm, agent);
    }
}
