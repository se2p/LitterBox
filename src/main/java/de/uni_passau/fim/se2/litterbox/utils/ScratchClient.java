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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.util.concurrent.RateLimiter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ScratchClient {

    private static final Logger log = Logger.getLogger(ScratchClient.class.getName());

    private static final String API_BASE_URL = "https://api.scratch.mit.edu";

    private static final Pattern PROJECT_TOKEN_PATTERN = Pattern.compile("\"project_token\":\"([^\"]+)\"");

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
        downloadProject(projectId, outputDir, false, false);
    }

    public void downloadProject(
            String projectId, Path outputDir, boolean saveMetadata, boolean downloadAssets
    ) throws IOException {
        if (isAlreadyDownloaded(projectId, outputDir, downloadAssets)) {
            return;
        }

        Files.createDirectories(outputDir);

        String metadataJson = downloadMetadata(projectId);
        String downloadToken = getProjectToken(metadataJson);
        if (saveMetadata) {
            saveJson(outputDir, projectId + "_metadata.json", metadataJson);
        }

        // Fetch raw data first to determine type
        byte[] projectData = downloadProjectBytes(projectId, downloadToken);

        if (isBinaryScratchProject(projectData)) {
            // Scratch 1.x - Binary Blob
            saveBinaryProject(projectData, projectId, outputDir);
        } else {
            // JSON-based (Scratch 2 or 3)
            String json = new String(projectData, StandardCharsets.UTF_8);

            if (!downloadAssets) {
                saveDownloadedProject(json, projectId, outputDir);
            } else {
                JsonNode rootNode = mapper.readTree(json);

                if (isScratch2(rootNode)) {
                    downloadProjectSb2(json, rootNode, projectId, outputDir);
                } else {
                    // Assume Scratch 3 by default or if it matches SB3 structure
                    downloadProjectSb3(json, rootNode, projectId, outputDir);
                }
            }
        }
    }

    /**
     * Checks the file header to determine if this is a binary Scratch 1.x project.
     *
     * <p>Scratch 1.3 projects use {@code ScratchV01} at the start. Scratch 1.4 projects use {@code ScratchV02}.
     *
     * @param data The downloaded project data.
     * @return True, if this is a binary Scratch 1.x project.
     */
    private boolean isBinaryScratchProject(byte[] data) {
        if (data.length < 10) {
            return false;
        }

        String header = new String(data, 0, 10, StandardCharsets.US_ASCII);
        return header.startsWith("ScratchV");
    }

    private boolean isScratch2(JsonNode root) {
        // Scratch 2 usually has "objName" at root or "info" with "flashVersion"
        return root.has("objName") || (root.has("info") && root.get("info").has("flashVersion"));
    }

    private void saveBinaryProject(byte[] data, String projectId, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Path sbPath = outputDir.resolve(projectId + ".sb");
        Files.write(sbPath, data);
    }

    private byte[] downloadProjectBytes(String projectId, String projectAccessToken) throws IOException {
        final String url = "https://projects.scratch.mit.edu/" + projectId + "?token=" + projectAccessToken;
        return readBytesFromUrl(url);
    }

    public void downloadProjectSb2(
            String json, JsonNode rootNode, String projectId, Path outputDir
    ) throws IOException {
        Files.createDirectories(outputDir);
        Path tempDir = Files.createTempDirectory("sb2_assets_" + projectId);
        try {
            saveJson(tempDir, "project.json", json);
            Set<String> assets = new HashSet<>();
            collectAssetsSb2(rootNode, assets);

            for (String asset : assets) {
                downloadAsset(asset, tempDir);
            }

            Path sb2Path = outputDir.resolve(projectId + ".sb2");
            zipDirectory(tempDir, sb2Path);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void downloadProjectSb3(
            String json, JsonNode projectJsonRoot, String projectId, Path outputDir
    ) throws IOException {
        Files.createDirectories(outputDir);
        Path tempDir = Files.createTempDirectory("sb3_assets_" + projectId);
        try {
            saveJson(tempDir, "project.json", json);
            Set<String> assets = new HashSet<>();
            collectAssetsSb3(projectJsonRoot, assets);

            for (String asset : assets) {
                downloadAsset(asset, tempDir);
            }

            Path sb3Path = outputDir.resolve(projectId + ".sb3");
            zipDirectory(tempDir, sb3Path);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void collectAssetsSb2(JsonNode projectJsonRoot, Set<String> assets) {
        // Scratch 2: costumes have baseLayerMD5, textLayerMD5; sounds have md5; penLayerMD5
        if (projectJsonRoot.has("costumes")) {
            for (JsonNode costume : projectJsonRoot.get("costumes")) {
                if (costume.has("baseLayerMD5")) {
                    assets.add(costume.get("baseLayerMD5").asText());
                }
                if (costume.has("textLayerMD5")) {
                    assets.add(costume.get("textLayerMD5").asText());
                }
            }
        }
        if (projectJsonRoot.has("sounds")) {
            for (JsonNode sound : projectJsonRoot.get("sounds")) {
                if (sound.has("md5")) {
                    assets.add(sound.get("md5").asText());
                }
            }
        }
        if (projectJsonRoot.has("penLayerMD5")) {
            assets.add(projectJsonRoot.get("penLayerMD5").asText());
        }

        if (projectJsonRoot.has("children")) {
            for (JsonNode child : projectJsonRoot.get("children")) {
                collectAssetsSb2(child, assets);
            }
        }
    }

    private void collectAssetsSb3(JsonNode projectJsonRoot, Set<String> assets) {
        // Scratch 3: targets -> costumes/sounds -> md5ext
        if (projectJsonRoot.has("targets")) {
            for (JsonNode target : projectJsonRoot.get("targets")) {
                if (target.has("costumes")) {
                    for (JsonNode costume : target.get("costumes")) {
                        if (costume.has("md5ext")) {
                            assets.add(costume.get("md5ext").asText());
                        }
                    }
                }
                if (target.has("sounds")) {
                    for (JsonNode sound : target.get("sounds")) {
                        if (sound.has("md5ext")) {
                            assets.add(sound.get("md5ext").asText());
                        }
                    }
                }
            }
        }
    }

    private void downloadAsset(String filename, Path outputDir) {
        String url = "https://assets.scratch.mit.edu/internalapi/asset/" + filename + "/get/";
        try {
            downloadBinary(url, outputDir.resolve(filename));
        } catch (IOException e) {
            log.warning("Failed to download asset: " + filename);
            log.warning(e.getMessage());
        }
    }

    private String downloadMetadata(final String projectId) throws IOException {
        String url = API_BASE_URL + "/projects/" + projectId;
        return readFromUrl(url);
    }

    public List<String> getRecentProjects(int limit) throws IOException {
        String url = API_BASE_URL + "/explore/projects?mode=trending&q=*&limit=" + limit;
        return extractProjectIds(url);
    }

    public List<String> getPopularProjects(int limit) throws IOException {
        String url = API_BASE_URL + "/explore/projects?mode=popular&q=*&limit=" + limit;
        return extractProjectIds(url);
    }

    public List<String> getUserProjects(String username) throws IOException {
        final List<String> projectIds = new ArrayList<>();
        final int limit = 40;
        int offset = 0;
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
        byte[] bytes = readBytesFromUrl(url);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] readBytesFromUrl(String url) throws IOException {
        rateLimiter.acquire();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (InterruptedException e) {
            throw new IOException("Network connection interruption.", e);
        }
    }

    private String getProjectToken(String projectInfoJson) throws IOException {
        final Matcher matcher = PROJECT_TOKEN_PATTERN.matcher(projectInfoJson);
        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Cannot extract download token from project metadata.");
        }
    }

    public void downloadBinary(String url, Path destination) throws IOException {
        rateLimiter.acquire();

        final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            final HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream()
            );
            try (InputStream is = response.body()) {
                Files.copy(is, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (InterruptedException e) {
            throw new IOException("Network connection interruption.", e);
        }
    }

    public boolean isAlreadyDownloaded(String projectId, Path projectOut, boolean downloadAssets) {
        if (downloadAssets) {
            return projectOut.resolve(projectId + ".sb").toFile().exists()
                    || projectOut.resolve(projectId + ".sb2").toFile().exists()
                    || projectOut.resolve(projectId + ".sb3").toFile().exists();
        } else {
            return projectOut.resolve(projectId + ".sb").toFile().exists()
                    || projectOut.resolve(projectId + ".json").toFile().exists();
        }
    }

    public void saveDownloadedProject(String json, String projectId, Path projectOut) throws IOException {
        Files.createDirectories(projectOut);
        saveJson(projectOut, projectId + ".json", json);
    }

    private void saveJson(Path outputDir, String filename, String json) throws IOException {
        Files.createDirectories(outputDir);
        JsonNode jsonNode = mapper.readTree(json);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Path path = outputDir.resolve(filename);
        writer.writeValue(path.toFile(), jsonNode);
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()));
                Stream<Path> walk = Files.walk(sourceDir)
        ) {
            walk.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    log.severe("Failed to zip file: " + path);
                }
            });
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
