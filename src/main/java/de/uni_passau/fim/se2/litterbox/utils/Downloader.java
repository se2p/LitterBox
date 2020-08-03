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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Downloader {

    public static String downloadAndSaveProject(String projectid, String projectout) throws IOException {
        if (!isAlreadyDownloaded(projectid, projectout)) {
            String json = downloadProjectJSON(projectid);
            saveDownloadedProject(json, projectid, projectout);
            return json;
        } else {
            Path path = Paths.get(projectout, projectid + ".json");
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }
    }

    public static String downloadProjectJSON(String projectid) throws IOException {
        String url = "https://projects.scratch.mit.edu/" + projectid + "/all";

        try (InputStream is = new URL(url).openStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            cp = br.read();
            while (cp != -1) {
                sb.append(((char) cp));
                cp = br.read();
            }
            br.close();
            return sb.toString();
        }
    }

    public static boolean isAlreadyDownloaded(String projectid, String projectout) throws IOException {
        Path path = Paths.get(projectout, projectid + ".json");
        File file = new File(path.toString());
        return file.exists();
    }

    public static void saveDownloadedProject(String json, String projectid, String projectout) throws IOException {
        if (projectout == null) {
            return;
        }

        File folder = new File(projectout);
        if (folder.exists() && !folder.isDirectory()) {
            System.out.println("Projectout is not a folder but a file");
        }

        if (!folder.exists()) {
            boolean success = folder.mkdir();
            if (!success) {
                System.out.println("Could not create projectout");
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Path path = Paths.get(projectout, projectid + ".json");
        writer.writeValue(new File(path.toString()), jsonNode);
    }
}
