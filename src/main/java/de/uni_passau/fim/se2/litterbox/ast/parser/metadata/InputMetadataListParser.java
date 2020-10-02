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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.InputMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class InputMetadataListParser {
    public static InputMetadataList parse(JsonNode inputsNode) {
        List<InputMetadata> inputMetadataList = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> entries = inputsNode.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> current = entries.next();
            inputMetadataList.add(InputMetadataParser.parse(current.getKey(), current.getValue()));
        }
        return new InputMetadataList(inputMetadataList);
    }
}
