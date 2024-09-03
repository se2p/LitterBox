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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Util class for getting the project JSON file out of the Scratch project ZIP file.
 */
public final class ZipReader {

    private ZipReader() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Extracts the project.json file from a Scratch project (ZIP file).
     *
     * @param path the file path
     * @return the JSON as a raw String
     * @throws IOException when given an invalid file or corrupted ZIP file
     */
    public static String getJsonString(final Path path) throws IOException {
        try (ZipFile file = new ZipFile(path.toFile())) {
            final ZipEntry projectJson = file.getEntry("project.json");
            if (projectJson != null) {
                return readZipEntry(file, projectJson);
            } else {
                return null;
            }
        }
    }

    private static String readZipEntry(final ZipFile file, final ZipEntry entry) throws IOException {
        try (
                InputStream is = file.getInputStream(entry);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)
        ) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
