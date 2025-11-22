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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ScratchClient {


    private static final String API_BASE_URL = "https://api.scratch.mit.edu";
    private final ObjectMapper mapper = new ObjectMapper();

    public void downloadProject(String projectId, Path outputDir) throws IOException {
        Downloader.downloadAndSaveProject(projectId, outputDir);
    }

    public void downloadProjectSb3(String projectId, Path outputDir) throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 1. Download project JSON
        String json = Downloader.downloadProjectJSON(projectId);
        
        // 2. Create a temporary directory for assets
        Path tempDir = Files.createTempDirectory("sb3_assets_" + projectId);
        try {
            // Save project.json
            saveJson(json, "project", tempDir);

            // 3. Parse JSON to find assets (md5ext)
            JsonNode rootNode = new ObjectMapper().readTree(json);
            Set<String> assets = new HashSet<>();
            
            // Helper to collect assets from a list of targets (stage + sprites)
            // Note: "targets" is used in SB3 format, but older API might return "objName" style.
            // The API response we saw earlier (10128125.json) has "costumes" and "sounds" at root (Stage) and in "children" (Sprites).
            
            // Collect from root (Stage)
            collectAssets(rootNode, assets);
            
            // Collect from children (Sprites)
            if (rootNode.has("children")) {
                for (JsonNode child : rootNode.get("children")) {
                    collectAssets(child, assets);
                }
            }

            // 4. Download assets
            for (String md5ext : assets) {
                downloadAsset(md5ext, tempDir);
            }

            // 5. Zip everything into .sb3
            Path sb3Path = outputDir.resolve(projectId + ".sb3");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(sb3Path.toFile()))) {
                Stream<Path> walk = Files.walk(tempDir);
                walk.filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(tempDir.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Failed to zip file: " + path);
                        }
                    });
                walk.close();
            }
            
        } finally {
            // Cleanup temp dir
            Stream<Path> walk = Files.walk(tempDir);
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            walk.close();
        }
    }

    private void collectAssets(JsonNode node, Set<String> assets) {
        if (node.has("costumes")) {
            for (JsonNode costume : node.get("costumes")) {
                if (costume.has("baseLayerMD5")) {
                    assets.add(costume.get("baseLayerMD5").asText());
                }
                 // Sometimes it might be just md5ext or similar in newer formats, but the example showed baseLayerMD5
                 // Also check for "md5ext" just in case
                if (costume.has("md5ext")) {
                    assets.add(costume.get("md5ext").asText());
                }
            }
        }
        if (node.has("sounds")) {
            for (JsonNode sound : node.get("sounds")) {
                if (sound.has("md5")) { // The example showed "md5" for sounds
                    assets.add(sound.get("md5").asText());
                }
                if (sound.has("md5ext")) {
                    assets.add(sound.get("md5ext").asText());
                }
            }
        }
    }

    private void downloadAsset(String md5ext, Path outputDir) {
        String url = "https://assets.scratch.mit.edu/internalapi/asset/" + md5ext + "/get/";
        try {
            Downloader.downloadBinary(url, outputDir.resolve(md5ext));
        } catch (IOException e) {
            System.err.println("Failed to download asset: " + md5ext);
        }
    }

    public void downloadMetadata(String projectId, Path outputDir) throws IOException {
        String url = API_BASE_URL + "/projects/" + projectId;
        String json = readFromUrl(url);
        saveJson(json, projectId + "_metadata", outputDir);
    }

    public List<String> getRecentProjects(int limit) throws IOException {
        // "recent" mode often returns empty results with wildcard query.
        // Using "trending" as a proxy for fresh content.
        String url = API_BASE_URL + "/explore/projects?mode=trending&q=*&limit=" + limit;
        return extractProjectIds(url);
    }

    public List<String> getPopularProjects(int limit) throws IOException {
        String url = API_BASE_URL + "/explore/projects?mode=popular&q=*&limit=" + limit;
        return extractProjectIds(url);
    }

    public List<String> getUserProjects(String username) throws IOException {
        List<String> projectIds = new ArrayList<>();
        int offset = 0;
        int limit = 40; // Max limit per request
        boolean more = true;

        while (more) {
            String url = API_BASE_URL + "/users/" + username + "/projects?limit=" + limit + "&offset=" + offset;
            List<String> batch = extractProjectIds(url);
            projectIds.addAll(batch);
            
            if (batch.size() < limit) {
                more = false;
            } else {
                offset += limit;
            }
        }
        return projectIds;
    }

    private List<String> extractProjectIds(String url) throws IOException {
        String json = readFromUrl(url);
        JsonNode root = mapper.readTree(json);
        List<String> ids = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                if (node.has("id")) {
                    ids.add(node.get("id").asText());
                }
            }
        }
        return ids;
    }

    private String readFromUrl(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();

        try (
                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)
        ) {
            final StringBuilder sb = new StringBuilder();
            int cp = br.read();
            while (cp != -1) {
                sb.append(((char) cp));
                cp = br.read();
            }
            return sb.toString();
        }
    }

    private void saveJson(String json, String filename, Path outputDir) throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        JsonNode jsonNode = mapper.readTree(json);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Path path = outputDir.resolve(filename + ".json");
        writer.writeValue(path.toFile(), jsonNode);
    }
}
