/*
 * Copyright (C) 2019 LitterBox contributors
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
package ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import ast.ParsingException;
import ast.model.URI;
import ast.model.literals.StringLiteral;
import ast.model.resource.ImageResource;
import ast.model.resource.Resource;
import ast.model.resource.SoundResource;
import ast.model.variable.StrId;
import utils.Preconditions;

public class ResourceParser {

    private final static String NAME = "name";
    private final static String MD5EXT = "md5ext";

    public static List<Resource> parseSound(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);

        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter = resourceNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            SoundResource res = new SoundResource(new StrId(node.get(NAME).asText()),
                    new URI(new StringLiteral(node.get(MD5EXT).asText())));
            parsedRessources.add(res);
        }
        return parsedRessources;
    }

    public static List<Resource> parseCostume(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);

        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter = resourceNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            ImageResource res = null;
            try {
                res = new ImageResource(new StrId(node.get(NAME).asText()),
                    getURI(node));
            } catch (ParsingException e) {
                Logger.getGlobal().warning(e.getMessage());
                continue;
            }
            parsedRessources.add(res);
        }
        return parsedRessources;
    }

    private static URI getURI(JsonNode node) throws ParsingException {
        if (node.has(MD5EXT)) {
            return new URI(new StringLiteral(node.get(MD5EXT).asText()));
        } else if (node.has("assetId") && node.has("dataFormat")) {
            String assetId = node.get("assetId").asText();
            String dataFormat = node.get("dataFormat").asText();
            String fileName = assetId + "." + dataFormat;
            return new URI(new StringLiteral(fileName));
        } else {
            throw new ParsingException("Cannot parse URI of resource node " + node.asText());
        }
    }
}
