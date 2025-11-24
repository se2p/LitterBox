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

import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScratchClientTest {

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Test
    public void testRateLimiterIsAcquired() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        
        // downloadBinary uses HttpURLConnection, so httpClient is not used.
        // We just verify rateLimiter is acquired.

        try {
            client.downloadBinary("http://example.com", Path.of("dummy"));
        } catch (Exception e) {
            // Ignore exceptions
        }
        verify(rateLimiter).acquire();
    }

    @Test
    public void testDownloadProjectScratch1() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        String projectId = "20000";
        String tokenJson = "{\"project_token\":\"abcde\"}";
        byte[] projectData = "ScratchV02...binary data...".getBytes(StandardCharsets.UTF_8);

        // Mock token response
        HttpResponse<byte[]> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn(tokenJson.getBytes(StandardCharsets.UTF_8));

        // Mock project response
        HttpResponse<byte[]> projectResponse = mock(HttpResponse.class);
        when(projectResponse.body()).thenReturn(projectData);

        doReturn(tokenResponse).doReturn(projectResponse).when(httpClient).send(any(), any());

        Path outputDir = Files.createTempDirectory("scratch_test_output");
        try {
            client.downloadProject(projectId, outputDir, true);
            Path sbPath = outputDir.resolve(projectId + ".sb");
            assert Files.exists(sbPath);
            assert Files.readString(sbPath).equals("ScratchV02...binary data...");
        } finally {
            deleteDirectory(outputDir);
        }
    }

    @Test
    public void testDownloadProjectScratch2() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        String projectId = "1231230";
        String tokenJson = "{\"project_token\":\"abcde\"}";
        String projectJson = "{\"objName\":\"Stage\", \"costumes\":[{\"baseLayerMD5\":\"costume1.png\"}]}";
        byte[] projectData = projectJson.getBytes(StandardCharsets.UTF_8);

        // Mock token response
        HttpResponse<byte[]> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn(tokenJson.getBytes(StandardCharsets.UTF_8));

        // Mock project response
        HttpResponse<byte[]> projectResponse = mock(HttpResponse.class);
        when(projectResponse.body()).thenReturn(projectData);

        doReturn(tokenResponse).doReturn(projectResponse).when(httpClient).send(any(), any());

        Path outputDir = Files.createTempDirectory("scratch_test_output");
        try {
            client.downloadProject(projectId, outputDir, true);
            Path sb2Path = outputDir.resolve(projectId + ".sb2");
            assert Files.exists(sb2Path);
        } finally {
            deleteDirectory(outputDir);
        }
    }

    @Test
    public void testDownloadProjectScratch3() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        String projectId = "1190759830";
        String tokenJson = "{\"project_token\":\"abcde\"}";
        String projectJson = "{\"targets\":[{\"costumes\":[{\"md5ext\":\"costume1.png\"}]}]}";
        byte[] projectData = projectJson.getBytes(StandardCharsets.UTF_8);

        // Mock token response
        HttpResponse<byte[]> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn(tokenJson.getBytes(StandardCharsets.UTF_8));

        // Mock project response
        HttpResponse<byte[]> projectResponse = mock(HttpResponse.class);
        when(projectResponse.body()).thenReturn(projectData);

        doReturn(tokenResponse).doReturn(projectResponse).when(httpClient).send(any(), any());

        Path outputDir = Files.createTempDirectory("scratch_test_output");
        try {
            client.downloadProject(projectId, outputDir, true);
            Path sb3Path = outputDir.resolve(projectId + ".sb3");
            assert Files.exists(sb3Path);
        } finally {
            deleteDirectory(outputDir);
        }
    }

    @Test
    public void testDownloadProjectScratch2NoAssets() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        String projectId = "1231230";
        String tokenJson = "{\"project_token\":\"abcde\"}";
        String projectJson = "{\"objName\":\"Stage\", \"costumes\":[{\"baseLayerMD5\":\"costume1.png\"}]}";
        byte[] projectData = projectJson.getBytes(StandardCharsets.UTF_8);

        // Mock token response
        HttpResponse<byte[]> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn(tokenJson.getBytes(StandardCharsets.UTF_8));

        // Mock project response
        HttpResponse<byte[]> projectResponse = mock(HttpResponse.class);
        when(projectResponse.body()).thenReturn(projectData);

        doReturn(tokenResponse).doReturn(projectResponse).when(httpClient).send(any(), any());

        Path outputDir = Files.createTempDirectory("scratch_test_output");
        try {
            // downloadAssets = false
            client.downloadProject(projectId, outputDir, false);
            
            // Should have .json file
            Path jsonPath = outputDir.resolve(projectId + ".json");
            assert Files.exists(jsonPath);
            
            // Should NOT have .sb2 file
            Path sb2Path = outputDir.resolve(projectId + ".sb2");
            assert !Files.exists(sb2Path);
        } finally {
            deleteDirectory(outputDir);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        try (java.util.stream.Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }
}
