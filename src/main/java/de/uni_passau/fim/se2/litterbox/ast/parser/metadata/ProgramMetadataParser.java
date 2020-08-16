/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;

import java.util.ArrayList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ProgramMetadataParser {

    /**
     * The parser needs the whole program node for extracting the meta, extension and monitors data.
     *
     * @param program the whole json node of the project
     * @return the Metadata of the program including meta, extension and monitors metadata.
     */
    public static ProgramMetadata parse(JsonNode program) {
        MonitorMetadataList monitorMetadataList = null;
        MetaMetadata meta = null;
        ExtensionMetadata extensionMetadata = null;
        if (program.has(META_KEY)) {
            meta = MetaMetadataParser.parse(program.get(META_KEY));
        } else {
            meta = new MetaMetadata("3.0.0", "0.2.0-prerelease.20200402182733",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0");
        }
        if (program.has(EXTENSIONS_KEY)) {
            extensionMetadata = ExtensionMetadataParser.parse(program.get(EXTENSIONS_KEY));
        } else {
            extensionMetadata = new ExtensionMetadata(new ArrayList<>());
        }
        if (program.has(MONITORS_KEY)) {
            monitorMetadataList = MonitorMetadataListParser.parse(program.get(MONITORS_KEY));
        } else {
            monitorMetadataList = new MonitorMetadataList(new ArrayList<>());
        }
        return new ProgramMetadata(monitorMetadataList, extensionMetadata, meta);
    }
}
