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
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    public void testDownloadProjectJSON() throws IOException, InterruptedException {
        ScratchClient client = new ScratchClient(rateLimiter, httpClient);
        String projectId = "12345";
        String tokenJson = "{\"project_token\":\"abcde\"}";
        String projectJson = "{\"targets\":[]}";

        // Mock first call for token
        HttpResponse<String> tokenResponse = mock(HttpResponse.class);
        when(tokenResponse.body()).thenReturn(tokenJson);

        // Mock second call for project JSON
        HttpResponse<String> projectResponse = mock(HttpResponse.class);
        when(projectResponse.body()).thenReturn(projectJson);

        doReturn(tokenResponse).doReturn(projectResponse).when(httpClient).send(any(), any());

        String result = client.downloadProjectJSON(projectId);
        
        verify(rateLimiter, times(2)).acquire();
        verify(httpClient, times(2)).send(any(), any());
        assert result.equals(projectJson);
    }
}
