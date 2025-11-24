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
import com.google.common.util.concurrent.RateLimiter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ScratchClient {


    private static final String API_BASE_URL = "https://api.scratch.mit.edu";
    private final ObjectMapper mapper = new ObjectMapper();

    // 10 requests per second according to https://github.com/scratchfoundation/scratch-rest-api/wiki#rate-limits
    private static final RateLimiter DEFAULT_RATE_LIMITER = RateLimiter.create(10.0);
    private final RateLimiter rateLimiter;
    private final HttpClient httpClient;

    public ScratchClient() {
        this(DEFAULT_RATE_LIMITER, HttpClient.newHttpClient());
    }

    public ScratchClient(RateLimiter rateLimiter) {
        this(rateLimiter, HttpClient.newHttpClient());
    }

    public ScratchClient(RateLimiter rateLimiter, HttpClient httpClient) {
        this.rateLimiter = rateLimiter;
        this.httpClient = httpClient;
    }

    public void downloadProject(String projectId, Path outputDir) throws IOException {
        if (!isAlreadyDownloaded(projectId, outputDir)) {
            String json = downloadProjectJSON(projectId);
            saveDownloadedProject(json, projectId, outputDir);
        }
    }

    public void downloadProjectSb3(String projectId, Path outputDir) throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 1. Download project JSON
        String json = downloadProjectJSON(projectId);
        
        // 2. Create a temporary directory for assets
        Path tempDir = Files.createTempDirectory("sb3_assets_" + projectId);
        try {
            // Save project.json
            saveJson(json, "project", tempDir);

            // 3. Parse JSON to find assets (md5ext)
            JsonNode rootNode = new ObjectMapper().readTree(json);
            Set<String> assets = new HashSet<>();
            
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
            downloadBinary(url, outputDir.resolve(md5ext));
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
        rateLimiter.acquire();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
        } catch (InterruptedException e) {
            throw new IOException("Network connection interruption.", e);
        }
    }

    /**
     * The first capturing group contains the actual project token value.
     */
    private static final Pattern PROJECT_TOKEN_PATTERN = Pattern.compile("\"project_token\":\"([^\"]+)\"");

    public String downloadProjectJSON(String projectId) throws IOException {
        final String projectAccessToken = getProjectToken(projectId);
        final String url = "https://projects.scratch.mit.edu/" + projectId + "?token=" + projectAccessToken;

        return readFromUrl(url);
    }

    private String getProjectToken(String projectId) throws IOException {
        final String url = "https://api.scratch.mit.edu/projects/" + projectId;
        final String projectInfo = readFromUrl(url);

        final Matcher matcher = PROJECT_TOKEN_PATTERN.matcher(projectInfo);
        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Cannot extract download token from project metadata.");
        }
    }

    public void downloadBinary(String url, Path destination) throws IOException {
        rateLimiter.acquire();
        HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();

        try (InputStream is = con.getInputStream()) {
            Files.copy(is, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean isAlreadyDownloaded(String projectid, Path projectout) {
        Path path = projectout.resolve(projectid + ".json");
        return path.toFile().exists();
    }

    public void saveDownloadedProject(String json, String projectid, Path projectout) throws IOException {
        if (projectout == null) {
            return;
        }

        File folder = projectout.toFile();
        if (folder.exists() && !folder.isDirectory()) {
            System.out.println("Projectout is not a folder but a file");
        }

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            if (!success) {
                System.out.println("Could not create projectout");
            }
        }

        JsonNode jsonNode = mapper.readTree(json);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Path path = projectout.resolve(projectid + ".json");
        writer.writeValue(path.toFile(), jsonNode);
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
