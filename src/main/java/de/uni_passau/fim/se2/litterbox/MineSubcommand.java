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
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@CommandLine.Command(
        name = "mine",
        description = "Download projects from Scratch.",
        mixinStandardHelpOptions = true
)
class MineSubcommand implements Callable<Integer> {

    private static final Logger log = Logger.getLogger(MineSubcommand.class.getName());

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output directory for downloaded projects.",
            required = true
    )
    Path outputPath;

    @CommandLine.Option(names = {"--with-metadata"}, description = "Additionally download project metadata.")
    boolean withMetadata;

    @CommandLine.Option(
            names = {"--with-assets"},
            description = "Download as .sb/.sb2/.sb3 file (zipped JSON + assets)."
    )
    boolean withAssets;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1..*")
    DownloadKind downloadKind;

    static class DownloadKind {
        @CommandLine.Option(names = {"--project-id"}, description = "ID of the project to download.")
        String projectId;

        @CommandLine.Option(names = {"--project-list"}, description = "File containing a list of project IDs.")
        Path projectList;

        @CommandLine.ArgGroup(exclusive = false)
        RangeDownload rangeDownload;

        @CommandLine.Option(names = {"--recent"}, description = "Download X most recent projects.")
        Integer recent;

        @CommandLine.Option(names = {"--popular"}, description = "Download X most popular projects.")
        Integer popular;

        @CommandLine.Option(names = {"--user"}, description = "Download all projects of a user.")
        String user;

        @CommandLine.ArgGroup(exclusive = false)
        RemixDownload remixDownload;
    }

    // all options within group required, but the rangeDownload group is optional above: if one of the two options
    // from/to in here is specified, then the other one is also required. This is enforced by the picocli library.
    static class RangeDownload {
        @CommandLine.Option(
                names = {"--from"},
                description = "Start project ID for range download.",
                required = true
        )
        Integer fromId;

        @CommandLine.Option(
                names = {"--to"},
                description = "End project ID for range download.",
                required = true
        )
        Integer toId;
    }

    static class RemixDownload {
        @CommandLine.Option(
                names = {"--remixes"},
                description = "Download all remixes of project with the given project ID.",
                required = true
        )
        String remixes;

        @CommandLine.Option(
                names = {"--transitive-remixes"},
                description = "Also download transitive remixes (i.e. remixes of remixes)."
        )
        boolean transitive;
    }

    private final ScratchClient client;

    public MineSubcommand() {
        this.client = new ScratchClient();
    }

    public MineSubcommand(ScratchClient client) {
        this.client = client;
    }

    @Override
    public Integer call() throws Exception {
        if (downloadKind.projectId != null) {
            processId(downloadKind.projectId);
        }

        if (downloadKind.projectList != null) {
            if (Files.exists(downloadKind.projectList)) {
                List<String> ids = Files.readAllLines(downloadKind.projectList);
                for (String id : ids) {
                    processId(id.trim());
                }
            } else {
                log.warning("Project list file not found: " + downloadKind.projectList);
            }
        }

        if (downloadKind.rangeDownload != null) {
            for (int i = downloadKind.rangeDownload.fromId; i <= downloadKind.rangeDownload.toId; i++) {
                processId(String.valueOf(i));
            }
        }

        if (downloadKind.recent != null) {
            List<String> ids = client.getRecentProjects(downloadKind.recent);
            for (String id : ids) {
                processId(id);
            }
        }

        if (downloadKind.popular != null) {
            List<String> ids = client.getPopularProjects(downloadKind.popular);
            for (String id : ids) {
                processId(id);
            }
        }

        if (downloadKind.user != null) {
            List<String> ids = client.getUserProjects(downloadKind.user);
            for (String id : ids) {
                processId(id);
            }
        }

        if (downloadKind.remixDownload != null) {
            final Queue<String> ids = new ArrayDeque<>(client.getRemixes(downloadKind.remixDownload.remixes));
            log.info("Downloading " + ids.size() + " remixes...");

            while (!ids.isEmpty()) {
                final String id = ids.remove();
                processId(id);

                if (downloadKind.remixDownload.transitive) {
                    final List<String> remixes = client.getRemixes(id);
                    log.info("Project " + id + " has " + remixes.size() + " remixes.");
                    ids.addAll(remixes);
                }
            }
        }

        return 0;
    }

    private void processId(String id) {
        try {
            log.info("Downloading project " + id + "...");
            client.downloadProject(id, outputPath, withMetadata, withAssets);
        } catch (IOException e) {
            log.severe("Failed to download project " + id + ": " + e.getMessage());
        }
    }
}
