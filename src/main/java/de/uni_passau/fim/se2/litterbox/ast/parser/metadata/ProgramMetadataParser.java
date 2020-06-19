package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ProgramMetadataParser {
    public static ProgramMetadata parse(JsonNode program) {
        MonitorMetadataList monitorMetadataList = null;
        MetaMetadata meta = null;
        ExtensionMetadata extensionMetadata = null;
        if (program.has(META_KEY)) {
            meta = MetaMetadataParser.parse(program.get(META_KEY));
        }
        if (program.has(EXTENSIONS_KEY)) {
            extensionMetadata = ExtensionMetadataParser.parse(program.get(EXTENSIONS_KEY));
        }
        if (program.has(MONITORS_KEY)) {
            monitorMetadataList = MonitorMetadataListParser.parse(program.get(MONITORS_KEY));
        }
        return new ProgramMetadata(monitorMetadataList, extensionMetadata, meta);
    }
}
