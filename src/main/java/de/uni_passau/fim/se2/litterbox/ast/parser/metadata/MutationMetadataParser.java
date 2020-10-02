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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CallMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PrototypeMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.StopMutationMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MutationMetadataParser {
    public static MutationMetadata parse(JsonNode mutationNode) throws ParsingException {
        String tagName = null;
        if (mutationNode.has(TAG_NAME_KEY)) {
            tagName = mutationNode.get(TAG_NAME_KEY).asText();
        }
        List<String> children = new ArrayList<>();
        if (mutationNode.get(CHILDREN_KEY).size() > 0) {
            throw new IllegalArgumentException("THIS MUTATION HAS CHILDREN!");
        }
        String procCode = null;
        if (mutationNode.has(PROCCODE_KEY)) {
            procCode = mutationNode.get(PROCCODE_KEY).asText();
        }

        List<String> argumentIdsList = new ArrayList<>();
        if (mutationNode.has(ARGUMENTIDS_KEY)) {
            String argumentIds = mutationNode.get(ARGUMENTIDS_KEY).asText();
            final JsonNode argumentsNode;
            ObjectMapper mapper = new ObjectMapper();
            try {
                argumentsNode = mapper.readTree(argumentIds);
            } catch (IOException e) {
                throw new ParsingException("Could not read argument ids of a procedure");
            }

            Preconditions.checkArgument(argumentsNode.isArray());
            ArrayNode argumentsArray = (ArrayNode) argumentsNode;
            for (int i = 0; i < argumentsArray.size(); i++) {
                argumentIdsList.add(argumentsArray.get(i).asText());
            }
        }

        String argumentNames = null;
        if (mutationNode.has(ARGUMENTNAMES_KEY)) {
            argumentNames = mutationNode.get(ARGUMENTNAMES_KEY).asText();
        }
        String argumentDefaults = null;
        if (mutationNode.has(ARGUMENT_DEFAULTS_KEY)) {
            argumentDefaults = mutationNode.get(ARGUMENT_DEFAULTS_KEY).asText();
        }
        boolean hasNext = false;
        if (mutationNode.has(HAS_NEXT_KEY)) {
            hasNext = mutationNode.get(HAS_NEXT_KEY).asBoolean();
        }
        boolean warp = false;
        if (mutationNode.has(WARP_KEY)) {
            warp = mutationNode.get(WARP_KEY).asBoolean();
        }
        if (mutationNode.has(ARGUMENTNAMES_KEY) && mutationNode.has(ARGUMENTNAMES_KEY)) {
            return new PrototypeMutationMetadata(tagName, children, procCode, argumentIdsList, warp,
                    argumentNames, argumentDefaults);
        } else if (mutationNode.has(HAS_NEXT_KEY)) {
            return new StopMutationMetadata(tagName, children, hasNext);
        } else {
            return new CallMutationMetadata(tagName, children, procCode, argumentIdsList, warp);
        }
    }
}
