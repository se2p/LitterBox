/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.utils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Util class for parsing the JSON files.
 */
public class JsonParser {

    public static JsonNode getBlocksNodeFromJSON(Path path) throws IOException {
        JsonNode script;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            script = buildScriptFromJSONString(sb.toString());
        }
        return script;
    }

    private static JsonNode buildScriptFromJSONString(String json) {
        JsonMapper mapper = JsonMapper.shared();
        JsonNode script = null;
        JsonNode rootNode = mapper.readTree(json);
        for (JsonNode node : rootNode.get("targets")) {
            if (node.has("isStage") && !node.get("isStage").asBoolean() && node.has("blocks")) {
                script = node.get("blocks");
                break;
            }
        }
        return script;
    }

    public static JsonNode getTargetsNodeFromJSONString(String json) {
        JsonMapper mapper = JsonMapper.shared();
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
