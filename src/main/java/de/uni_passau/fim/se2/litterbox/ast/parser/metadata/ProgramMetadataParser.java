package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;

public class ProgramMetadataParser {
    public static ProgramMetadata parse(JsonNode program) {
        MetaMetadata meta = MetaMetadataParser.parse(program.get("meta"));
        ExtensionMetadata extensionMetadata = ExtensionMetadataParser.parse(program.get("extensions"));
        MonitorMetadataList monitorMetadataList = MonitorMetadataListParser.parse(program.get("monitors"));
        return new ProgramMetadata(monitorMetadataList, extensionMetadata, meta);
    }
}
