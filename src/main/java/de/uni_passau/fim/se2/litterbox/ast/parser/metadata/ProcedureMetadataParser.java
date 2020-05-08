package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;


public class ProcedureMetadataParser {

    public static ProcedureMetadata parse(String definitionId, String prototypeId, JsonNode blocksNode) {
        BlockMetadata def = BlockMetadataParser.parse(definitionId, blocksNode.get(definitionId));
        BlockMetadata proto = BlockMetadataParser.parse(prototypeId, blocksNode.get(prototypeId));
        return new ProcedureMetadata(def, proto);
    }
}
