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
package de.uni_passau.fim.se2.litterbox;

import de.uni_passau.fim.se2.litterbox.utils.ScratchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MineSubcommandTest {

    @Mock
    private ScratchClient client;

    private Main.MineSubcommand mineSubcommand;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        mineSubcommand = new Main.MineSubcommand(client);
        cmd = new CommandLine(mineSubcommand);
    }

    @Test
    void testDownloadSingleProject() throws IOException {
        int exitCode = cmd.execute("--project-id", "12345", "--output", "out");
        
        verify(client).downloadProject(eq("12345"), any(Path.class), eq(false));
        assert exitCode == 0;
    }

    @Test
    void testDownloadSb3() throws IOException {
        int exitCode = cmd.execute("--project-id", "12345", "--sb3", "--output", "out");

        verify(client).downloadProject(eq("12345"), any(Path.class), eq(true));
        assert exitCode == 0;
    }

    @Test
    void testDownloadMetadata() throws IOException {
        int exitCode = cmd.execute("--project-id", "12345", "--metadata", "--output", "out");

        verify(client).downloadProject(eq("12345"), any(Path.class), eq(false));
        verify(client).downloadMetadata(eq("12345"), any(Path.class));
        assert exitCode == 0;
    }

    @Test
    void testRecentProjects() throws IOException {
        when(client.getRecentProjects(anyInt())).thenReturn(Arrays.asList("1", "2"));
        
        int exitCode = cmd.execute("--recent", "2", "--output", "out");

        verify(client).getRecentProjects(2);
        verify(client, times(2)).downloadProject(anyString(), any(Path.class), eq(false));
        assert exitCode == 0;
    }

    @Test
    void testPopularProjects() throws IOException {
        when(client.getPopularProjects(anyInt())).thenReturn(Arrays.asList("10", "20"));

        int exitCode = cmd.execute("--popular", "2", "--output", "out");

        verify(client).getPopularProjects(2);
        verify(client, times(2)).downloadProject(anyString(), any(Path.class), eq(false));
        assert exitCode == 0;
    }

    @Test
    void testUserProjects() throws IOException {
        when(client.getUserProjects(anyString())).thenReturn(Arrays.asList("100", "200"));

        int exitCode = cmd.execute("--user", "testuser", "--output", "out");

        verify(client).getUserProjects("testuser");
        verify(client, times(2)).downloadProject(anyString(), any(Path.class), eq(false));
        assert exitCode == 0;
    }

    @Test
    void testProjectList() throws IOException {
        Path tempFile = Files.createTempFile("projects", ".txt");
        Files.write(tempFile, Arrays.asList("111", "222"));
        tempFile.toFile().deleteOnExit();

        int exitCode = cmd.execute("--project-list", tempFile.toString(), "--output", "out");

        verify(client).downloadProject(eq("111"), any(Path.class), eq(false));
        verify(client).downloadProject(eq("222"), any(Path.class), eq(false));
        assert exitCode == 0;
    }
    
    @Test
    void testRangeProjects() throws IOException {
        int exitCode = cmd.execute("--from", "10", "--to", "12", "--output", "out");

        verify(client).downloadProject(eq("10"), any(Path.class), eq(false));
        verify(client).downloadProject(eq("11"), any(Path.class), eq(false));
        verify(client).downloadProject(eq("12"), any(Path.class), eq(false));
        assert exitCode == 0;
    }
}
