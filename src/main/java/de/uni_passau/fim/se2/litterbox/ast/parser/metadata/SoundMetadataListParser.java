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
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class SoundMetadataListParser {
    public static SoundMetadataList parse(JsonNode soundsNode) {
        List<SoundMetadata> soundMetadataList = new ArrayList<>();
        Preconditions.checkArgument(soundsNode instanceof ArrayNode);
        ArrayNode soundsArray = (ArrayNode) soundsNode;
        for (int i = 0; i < soundsArray.size(); i++) {
            soundMetadataList.add(SoundMetadataParser.parse(soundsArray.get(i)));
        }

        return new SoundMetadataList(soundMetadataList);
    }
}
