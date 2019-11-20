package analytics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import scratch.structure.Project;
import utils.CSVWriter;
import utils.JsonParser;

public class Scratch2Analyzer {

    public static void analyze(String detectors, String output, File folder) {
        if (folder.exists() && folder.isDirectory()) {
            checkMultipleScratch2(folder,
                detectors, output);
        } else if (folder.exists() && !folder.isDirectory()) {
            checkSingleScratch2(folder,
                detectors, output);

        } else {
            System.err.println("[Error] given path does not exist");
        }
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     * @param detectors
     */
    private static void checkSingleScratch2(File fileEntry, String detectors, String csv) {
        Project project;
        try {
            if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
                project = JsonParser.parseRaw(fileEntry);
            } else {
                project = getProjectScratch2(fileEntry);
            }

            IssueTool iT = new IssueTool();
            if (csv == null || csv.equals("")) {
                iT.checkRaw(project, detectors);
            } else {
                CSVPrinter printer = prepareCSVPrinter(detectors, iT, csv);
                iT.check(project, printer, detectors);
                System.out.println("Finished: " + fileEntry.getName());
                try {
                    assert printer != null;
                    CSVWriter.flushCSV(printer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Project getProjectScratch2(File fileEntry) {
        System.out.println("Starting: " + fileEntry);
        Project project = null;
        try {
            project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
            assert project != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * The main method for analyzing all Scratch project files (ZIP) in the given folder location. It will produce a
     * .csv file with all entries.
     */
    private static void checkMultipleScratch2(File folder, String dtctrs, String csv) {

        CSVPrinter printer = null;
        try {
            IssueTool iT = new IssueTool();
            printer = prepareCSVPrinter(dtctrs, iT, csv);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    Project project;
                    if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
                        project = JsonParser.parseRaw(fileEntry);
                    } else {
                        project = getProjectScratch2(fileEntry);
                    }
                    //System.out.println(project.toString());
                    iT.check(project, printer, dtctrs);
                    System.out.println("Finished: " + fileEntry.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert printer != null;
                CSVWriter.flushCSV(printer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static CSVPrinter prepareCSVPrinter(String dtctrs, IssueTool iT, String name) {
        List<String> heads = new ArrayList<>();
        heads.add("project");
        String[] detectors;
        if (dtctrs.equals("all")) {
            detectors = iT.getFinder().keySet().toArray(new String[0]);
        } else {
            detectors = dtctrs.split(",");
        }
        for (String s : detectors) {
            if (iT.getFinder().containsKey(s)) {
                IssueFinder iF = iT.getFinder().get(s);
                heads.add(iF.getName());
            }
        }
        try {
            return CSVWriter.getNewPrinter(name, heads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
