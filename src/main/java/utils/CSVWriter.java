package utils;

import analytics.Issue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import scratch2.structure.Project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class for writing and saving the csv
 */
public class CSVWriter {

    /**
     * Adds data to an existing CSVPrinter
     *
     * TODO: [Improvement] Create a check name ENUM with all check names for easy expandability when creating new checks instead of extending the csv heads manually
     * @param csvPrinter the CSVPrinter to add the information
     * @param project    the project with the information
     * @param issues     all the issues found in the project
     * @throws IOException corrupt file path
     */
    public static void addData(CSVPrinter csvPrinter, Project project, List<Issue> issues, String name) throws IOException {
        if (issues.size() == 26) {
            if (csvPrinter == null) {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(name));
                csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("project", "blockCount", "has_global_start", "sprite_starting_point", "laggy_movement",
                                "double_condition", "missing_forever_loop", "clone_initialization", "missing_termination",
                                "loose_blocks", "multiple_attribute_modification", "empty_body", "sequential_actions",
                                "sprite_naming", "long_script", "broadcast_sync", "nested_loops", "duplicated_script",
                                "race_condition", "empty_script", "middle_man", "no_op", "variable_scope", "unused_variable",
                                "duplicated_sprite", "inappropriate_intimacy", "noop_project"));
            }
            List<String> data = new ArrayList<>();
            data.add(project.getName());
            for (Issue is : issues) {
                data.add(Integer.toString(is.getCount()));
            }
            csvPrinter.printRecord(data);
        }
    }

    /**
     * Creates a new CSVPrinter with the correct head of all implemented issue names
     * TODO: [Improvement] Create a check name ENUM with all check names for easy expandability when creating new checks instead of extending the csv heads manually
     * @return a new CSVPrinter
     * @throws IOException corrupt file path
     */
    public static CSVPrinter getNewPrinter(String name) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(name));
        return new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("project", "blockCount", "has_global_start", "sprite_starting_point", "laggy_movement",
                        "double_condition", "missing_forever_loop", "clone_initialization", "missing_termination",
                        "loose_blocks", "multiple_attribute_modification", "empty_body", "sequential_actions",
                        "sprite_naming", "long_script", "broadcast_sync", "nested_loops", "duplicated_script",
                        "race_condition", "empty_script", "middle_man", "no_op", "variable_scope", "unused_variable",
                        "duplicated_sprite", "inappropriate_intimacy", "noop_project"));
    }


    /**
     * Saves the file
     *
     * @param csvPrinter the CSVPrinter to save the data
     * @throws IOException corrupt file path
     */
    public static void flushCSV(CSVPrinter csvPrinter) throws IOException {
        csvPrinter.flush();
    }
}