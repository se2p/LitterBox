package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;

public class MetaMetadataParser {

    public static MetaMetadata parse(JsonNode metaNode) {
        String vm = "";
        String semver = "";
        String agent = "";
        if (metaNode.has("semver")){
            semver = metaNode.get("semver").asText();
        }
        if (metaNode.has("vm")){
            vm = metaNode.get("vm").asText();
        }
        if (metaNode.has("agent")){
            agent = metaNode.get("agent").asText();
        }
        return new MetaMetadata(semver, vm, agent);
    }
}
