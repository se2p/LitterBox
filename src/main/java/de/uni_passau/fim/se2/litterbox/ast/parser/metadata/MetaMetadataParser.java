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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MetaMetadataParser {

    public static MetaMetadata parse(JsonNode metaNode) {
        String vm = "";
        String semver = "";
        String agent = "";
        if (metaNode.has(SEMVER_KEY)) {
            semver = metaNode.get(SEMVER_KEY).asText();
        }
        if (metaNode.has(VM_KEY)) {
            vm = metaNode.get(VM_KEY).asText();
        }
        if (metaNode.has(AGENT_KEY)) {
            agent = metaNode.get(AGENT_KEY).asText();
        }
        return new MetaMetadata(semver, vm, agent);
    }
}
