package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;

public class MetaMetadataParser {

    public static MetaMetadata parseMeta(JsonNode program) {
        String vm = "";
        String semver = "";
        String agent = "";
        JsonNode meta = program.get("meta");
        if (meta.has("semver")){
            semver = meta.get("semver").asText();
        }
        if (meta.has("vm")){
            vm = meta.get("vm").asText();
        }
        if (meta.has("agent")){
            agent = meta.get("agent").asText();
        }
        return new MetaMetadata(semver, vm, agent);
    }
}
