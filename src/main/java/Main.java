import analytics.IssueTool;
import org.apache.commons.csv.CSVPrinter;
import scratch.structure.Project;
import utils.CSVWriter;
import utils.JsonParser;
import utils.Version;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {

    private final static File folder = new File("C:\\scratchprojects\\files3\\");
    private static Version version = Version.SCRATCH3;

    /**
     * The main method for analyzing one Scratch project file (ZIP).
     * It will produce only console output.
     */
    public static void main(String[] args) {
        checkScratch(folder + "\\z.sb3");
    }

    /**
     *  Check method, uses the version
      * @param path the file path
     */
    private static void checkScratch(String path) {
        try {
            Project project = null;
            File fileEntry = new File(path);
            if (!fileEntry.isDirectory()) {
                System.out.println(fileEntry);
                System.out.println(fileEntry.getName());
                try {
                    if (version == Version.SCRATCH2) {
                        project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
                    } else if (version == Version.SCRATCH3) {
                        project = JsonParser.parse3(fileEntry.getName(), fileEntry.getPath());
                    }
                    assert project != null;
                    //System.out.println(project.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assert project != null;
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
    public static void mainMultiple() {
        CSVPrinter printer = null;
        try {
            String name = "./test.csv";
            Project project = null;
            printer = CSVWriter.getNewPrinter(name);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    System.out.println(fileEntry.getName());
                    try {
                        project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert project != null;
                    //System.out.println(project.toString());
                    IssueTool iT = new IssueTool();
                    iT.check(project, printer, name);
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
            int count = 0;
            int datacount = 0;
            printer = CSVWriter.getNewPrinter(name);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (project != null) {
                        //System.out.println(project.toString());
                        IssueTool iT = new IssueTool();
                        iT.check(project, printer, name);
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
                    printer = CSVWriter.getNewPrinter(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
