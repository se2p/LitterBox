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
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CommentMetadataParser {

    public static CommentMetadata parse(String commentId, JsonNode commentNode) {

        String blockId = null;
        if (!(commentNode.get(BLOCK_ID_KEY) instanceof NullNode)) {
            blockId = commentNode.get(BLOCK_ID_KEY).asText();
        }
        double xPos = commentNode.get(X_KEY).asDouble();
        double yPos = commentNode.get(Y_KEY).asDouble();
        double width = commentNode.get(WIDTH_KEY).asDouble();
        double height = commentNode.get(HEIGHT_KEY).asDouble();
        boolean minimized = commentNode.get(MINIMIZED_KEY).asBoolean();
        String text = commentNode.get(TEXT_KEY).asText();
        return new CommentMetadata(commentId, blockId, xPos, yPos, width, height, minimized, text);
    }
}
