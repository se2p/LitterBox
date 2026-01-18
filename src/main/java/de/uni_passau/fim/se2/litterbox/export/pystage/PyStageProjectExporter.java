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
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.export.ProjectExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

/**
 * Exports a Scratch project to a PyStage project structure.
 *
 * <p>Creates a directory containing:
 * <ul>
 *   <li>A single .py file with all PyStage code</li>
 *   <li>All costume and sound assets extracted from the sb3 file</li>
 * </ul>
 *
 * <p>This exporter requires an .sb3 input file since JSON files do not
 * contain the binary assets.
 */
public class PyStageProjectExporter extends ProjectExporter {

    private static final String IMAGES_DIR = "images";
    private static final String SOUNDS_DIR = "sounds";

    /**
     * Exports a program to a PyStage project directory.
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

        // Create images and sounds directories
        Path imagesDir = outputDir.resolve(IMAGES_DIR);
        Files.createDirectories(imagesDir);
        Path soundsDir = outputDir.resolve(SOUNDS_DIR);
        Files.createDirectories(soundsDir);

        // Extract assets from sb3 using AST for naming
        extractAssets(sb3File, program, imagesDir, soundsDir);

        // Export Python code to single file
        String projectName = getBaseName(sb3File.getFileName().toString());
        Path pyFile = outputDir.resolve(sanitizeFilename(projectName) + ".py");

        String code = PyStageVisitor.of(program);
        Files.writeString(pyFile, code, StandardCharsets.UTF_8);

        logExportSummary(outputDir);
    }

    private void extractAssets(Path sb3File, Program program, Path imagesDir, Path soundsDir) throws IOException {
        try (ZipFile zip = new ZipFile(sb3File.toFile())) {
            for (var actor : program.getActorDefinitionList().getDefinitions()) {
                if (actor.getActorMetadata() == null) {
                    continue;
                }

                if (actor.getActorMetadata().getCostumes() != null) {
                    for (ImageMetadata costume : actor.getActorMetadata().getCostumes().getList()) {
                        extractAsset(zip, costume.getMd5ext(), sanitizeFilename(costume.getName()), imagesDir);
                    }
                }

                if (actor.getActorMetadata().getSounds() != null) {
                    for (SoundMetadata sound : actor.getActorMetadata().getSounds().getList()) {
                        extractAsset(zip, sound.getMd5ext(), sanitizeFilename(sound.getName()), soundsDir);
                    }
                }
            }
        }
    }
}
