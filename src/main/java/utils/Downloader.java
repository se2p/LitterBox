package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Downloader {

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
            return sb.toString();
        }
    }
}
