/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.export.ProjectExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * Exports a Scratch project to a full Goboscript project structure.
 *
 * <p>Creates a directory containing:
 * <ul>
 *   <li>One .gs file per actor (stage and sprites)</li>
 *   <li>All costume and sound assets extracted from the sb3 file</li>
 * </ul>
 *
 * <p>This exporter requires an .sb3 input file since JSON files do not
 * contain the binary assets needed for a compilable Goboscript project.
 */
public class GoboscriptProjectExporter extends ProjectExporter {

    private static final Logger log = Logger.getLogger(GoboscriptProjectExporter.class.getName());
    private static final String ASSETS_DIR = "assets";

    private Set<String> availableAssets;

    /**
     * Exports a program to a full Goboscript project directory.
     *
     * @param sb3File   Path to the source .sb3 file (required for asset extraction)
     * @param program   The parsed program AST
     * @param outputDir The directory to create the project in
     * @throws IOException If file operations fail
     */
    @Override
    public void export(Path sb3File, Program program, Path outputDir) throws IOException {
        clearErrors();

        // Create output directory
        Files.createDirectories(outputDir);

        // Extract assets from sb3 and collect available asset names
        availableAssets = extractAssets(sb3File, outputDir);

        // Export each actor to separate .gs file
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            exportActor(actor, program, outputDir);
        }

        logExportSummary(outputDir);
    }

    protected Set<String> extractAssets(Path sb3File, Path outputDir) throws IOException {
        Set<String> assets = new HashSet<>();
        Path assetsDir = outputDir.resolve(ASSETS_DIR);
        Files.createDirectories(assetsDir);
        try (ZipFile zip = new ZipFile(sb3File.toFile())) {
            zip.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> !entry.getName().equals("project.json"))
                    .forEach(entry -> {
                        extractEntry(zip, entry, assetsDir);
                        assets.add(entry.getName());
                    });
        }
        return assets;
    }

    private void exportActor(ActorDefinition actor, Program program, Path outputDir) throws IOException {
        // Skip actors that have no valid costumes (Goboscript requires at least one costume)
        // However, we MUST export the Stage regardless, otherwise the project structure is invalid.
        if (!hasValidCostumes(actor) && !actor.isStage()) {
            log.warning("Skipping actor '" + actor.getIdent().getName()
                    + "' because it has no costumes available in the sb3 file");
            return;
        }

        // Goboscript requires the Stage to be in a file named "stage.gs" (lowercase)
        String filename;
        if (actor.isStage()) {
            filename = "stage.gs";
        } else {
            filename = sanitizeFilename(actor.getIdent().getName()) + ".gs";
        }
        Path actorFile = outputDir.resolve(filename);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.setCurrentActor(actor);
        visitor.setAssetPrefix(ASSETS_DIR + "/");
        visitor.setAvailableAssets(availableAssets);
        actor.accept(visitor);

        String code = visitor.getOutput();
        Files.writeString(actorFile, code, StandardCharsets.UTF_8);

        log.fine("Exported actor: " + actor.getIdent().getName() + " -> " + filename);
    }

    private boolean hasValidCostumes(ActorDefinition actor) {
        if (actor.getActorMetadata() == null) {
            return false;
        }
        for (ImageMetadata costume : actor.getActorMetadata().getCostumes().getList()) {
            if (availableAssets == null || availableAssets.contains(costume.getMd5ext())) {
                return true;
            }
        }
        return false;
    }
}
