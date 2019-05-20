import analytics.IssueFinder;
import analytics.IssueTool;
import org.apache.commons.csv.CSVPrinter;
import scratch.structure.Project;
import utils.CSVWriter;
import utils.JsonParser;
import utils.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    private final static File folder = new File("C:\\scratchprojects\\files3\\");
    private static Version version = Version.SCRATCH3;

    public static void main(String[] args) {
        //checkSingle(folder + "\\z.sb3");
        checkMultiple(folder.getPath());
    }

    /**
     * The method for analyzing one Scratch project file (ZIP).
     * It will produce only console output.
     *
     * @param path the file path
     */
    private static void checkSingle(String path) {
        try {
            File fileEntry = new File(path);
            if (!fileEntry.isDirectory()) {
                Project project = getProject(fileEntry);
                //System.out.println(project.toString());
                IssueTool iT = new IssueTool();
                iT.checkRaw(project);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method for analyzing all Scratch project files (ZIP) in the given folder location.
     * It will produce a .csv file with all entries.
     */
    private static void checkMultiple(String path) {
        File folder = new File(path);
        CSVPrinter printer = null;
        try {
            String name = "./test.csv";
            IssueTool iT = new IssueTool();
            List<String> heads = new ArrayList<>();
            heads.add("project");
            for (IssueFinder iF : iT.getFinder()) {
                heads.add(iF.getName());
            }
            printer = CSVWriter.getNewPrinter(name, heads);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    Project project = getProject(fileEntry);
                    //System.out.println(project.toString());
                    iT.check(project, printer);
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

    private static Project getProject(File fileEntry) {
        System.out.println(fileEntry);
        System.out.println(fileEntry.getName());
        Project project = null;
        try {
            if (version == Version.SCRATCH2) {
                project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
            } else if (version == Version.SCRATCH3) {
                project = JsonParser.parse3(fileEntry.getName(), fileEntry.getPath());
            }
            assert project != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * This main method was used for analyzing a repository with 250.000 projects, that only contained the JSON files.
     * A Scratch project is normally zipped and contains pictures, sounds and the JSON.
     * This method uses a different JsonParser method which takes a raw JSON file and not the ZIP file.
     * Also it creates .csv files with 10.000 entries and 25 files in total.
     */
    public static void mainJson() {
        CSVPrinter printer;
        try {
            String name = "./dataset0.csv";
            Project project = null;
            IssueTool iT = new IssueTool();
            List<String> heads = new ArrayList<>();
            heads.add("project");
            for (IssueFinder iF : iT.getFinder()) {
                heads.add(iF.getName());
            }
            printer = CSVWriter.getNewPrinter(name, heads);
            int count = 0;
            int datacount = 0;
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (project != null) {
                        //System.out.println(project.toString());2
                        iT.check(project, printer);
                        count++;
                    }
                }
                if (count == 10000) {
                    if (count == Objects.requireNonNull(folder.listFiles()).length - 1 || datacount == 24) {
                        CSVWriter.flushCSV(printer);
                        return;
                    }
                    CSVWriter.flushCSV(printer);
                    count = 0;
                    System.out.println("Finished: " + name);
                    datacount++;
                    name = "./dataset" + datacount + ".csv";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
