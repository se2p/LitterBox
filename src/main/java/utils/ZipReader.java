package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Util class for getting the project JSON file out of the Scratch project ZIP file
 */
public class ZipReader {

    /**
     * A method to extract the project.json file from a Scratch project (ZIP file)
     * @param path the file path
     * @return the JSON as a raw String
     * @throws IOException when given a invalid file or corrupted ZIP file
     */
    static String getJsonString(String path) throws IOException {
        final ZipFile file = new ZipFile(path);
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("project.json")) {
                    InputStream is = file.getInputStream(entry);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);        //.append('\n');
                    }
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
     * @param path the file path
     * @return the filename
     * @throws IOException
     */
    public static String getName(String path) throws IOException {
        final ZipFile file = new ZipFile(path);
        return file.getName();
    }

}
