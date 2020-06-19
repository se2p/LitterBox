/*
 * Copyright (C) 2020 LitterBox contributors
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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Util class for getting the project JSON file out of the Scratch project ZIP file
 */
public class ZipReader {

    /**
     * A method to extract the project.json file from a Scratch project (ZIP file)
     *
     * @param path the file path
     * @return the JSON as a raw String
     * @throws IOException when given a invalid file or corrupted ZIP file
     */
    public static String getJsonString(String path) throws IOException {
        final ZipFile file = new ZipFile(path);
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("project.json")) {
                    InputStream is = file.getInputStream(entry);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);        //.append('\n');
                    }
                    br.close();
                    return sb.toString();
                }
            }
        } finally {
            file.close();
        }
        return null;
    }

    /**
     * A method returning the filename for a given filepath
     *
     * @param path the file path
     * @return the filename
     * @throws IOException
     */
    public static String getName(String path) throws IOException {
        final ZipFile file = new ZipFile(path);
        String name = file.getName();
        file.close();
        return name;
    }
}
