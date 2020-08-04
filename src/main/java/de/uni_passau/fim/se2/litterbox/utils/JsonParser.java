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
package de.uni_passau.fim.se2.litterbox.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Util class for parsing the JSON files.
 */
public class JsonParser {

    public static JsonNode getBlocksNodeFromJSON(String path) {
        JsonNode script = null;
        Path currentRelativePath = Paths.get("");
        String sPath = currentRelativePath.toAbsolutePath().toString(); // Todo unused variable?

        try {
            BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            script = buildScriptFromJSONString(sb.toString());
            br.close();
        } catch (Exception e) {
            // FIXME Empty Catch block?
        }
        return script;
    }

    private static JsonNode buildScriptFromJSONString(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode script = null;
        JsonNode rootNode = mapper.readTree(json);
        Iterator<JsonNode> elements = rootNode.get("targets").elements();
        while (elements.hasNext()) {
            JsonNode node = elements.next();
            if (node.has("isStage") && !node.get("isStage").asBoolean() && node.has("blocks")) {
                script = node.get("blocks");
                break;
            }
        }
        return script;
    }

    public static JsonNode getTargetsNodeFromJSONString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            return rootNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
