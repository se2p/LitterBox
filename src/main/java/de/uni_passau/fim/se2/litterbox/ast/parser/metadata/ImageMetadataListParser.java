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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ImageMetadataListParser {

    public static ImageMetadataList parse(JsonNode imagesNode) {
        List<ImageMetadata> imageMetadataList = new ArrayList<>();
        Preconditions.checkArgument(imagesNode instanceof ArrayNode);
        ArrayNode imagesArray = (ArrayNode) imagesNode;
        for (int i = 0; i < imagesArray.size(); i++) {
            imageMetadataList.add(ImageMetadataParser.parse(imagesArray.get(i)));
        }

        return new ImageMetadataList(imageMetadataList);
    }
}
