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
package de.uni_passau.fim.se2.litterbox;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

public class Main {

    private static final String CHECK = "check";
    private static final String CHECK_SHORT = "c";
    private static final String LEILA = "leila";
    private static final String LEILA_SHORT = "l";
    private static final String STATS = "stats";
    private static final String STATS_SHORT = "s";
    private static final String HELP = "help";
    private static final String HELP_SHORT = "h";

    private static final String PROJECTPATH = "path";
    private static final String PROJECTPATH_SHORT = "p";
    private static final String PROJECTID = "projectid";
    private static final String PROJECTID_SHORT = "i";
    private static final String PROJECTLIST = "projectlist";
    private static final String PROJECTLIST_SHORT = "t";

    private static final String OUTPUT_LANG = "lang";
    private static final String OUTPUT_LANG_SHORT = "k";
    private static final String PROJECTOUT = "projectout";
    private static final String PROJECTOUT_SHORT = "r";
    private static final String OUTPUT = "output";
    private static final String OUTPUT_SHORT = "o";
    private static final String ANNOTATE = "annotate";
    private static final String ANNOTATE_SHORT = "a";
    private static final String DETECTORS = "detectors";
    private static final String DETECTORS_SHORT = "d";

    public static ResourceBundle resourceBundle;

    private Main() {
    }

    public static Options getCommandLineOptions() {
        Options options = new Options();

        // Operation mode
        OptionGroup mainMode = new OptionGroup();
        mainMode.addOption(new Option(CHECK_SHORT, CHECK, false, "Check specified Scratch projects for issues"));
        mainMode.addOption(new Option(LEILA_SHORT, LEILA, false, "Translate specified Scratch projects to Leila"));
        mainMode.addOption(new Option(STATS_SHORT, STATS, false, "Extract metrics for Scratch projects"));
        mainMode.addOption(new Option(HELP_SHORT, HELP, false, "print this message"));
        options.addOptionGroup(mainMode);

        // Target project(s)
        OptionGroup targetProject = new OptionGroup();
        targetProject.addOption(new Option(PROJECTID_SHORT, PROJECTID, true,
                "id of the project that should be downloaded and analysed."));
        targetProject.addOption(new Option(PROJECTLIST_SHORT, PROJECTLIST, true, "path to a file with a list of project ids of projects"
                + " which should be downloaded and analysed."));
        options.addOptionGroup(targetProject);

        // Storage options
        options.addOption(new Option(PROJECTPATH_SHORT, PROJECTPATH, true, "path to folder or file that should be analyzed (required)"));

        // Output options
        options.addOption(PROJECTOUT_SHORT, PROJECTOUT, true, "path where the downloaded project(s) should be stored");
        options.addOption(OUTPUT_SHORT, OUTPUT, true, "path with name of the csv file you want to save (required if " +
                "path argument"
                + " is a folder path)\nusage with --leila: Path to file or folder for the resulting .sc file(s); "
                + "has to be a folder if multiple projects are analysed "
                + "(file will be created if not existing yet, path has to exist)");
        options.addOption(ANNOTATE_SHORT, ANNOTATE, true, "path where scratch files with hints to bug patterns should" +
                " be created");

        // Parameters
        options.addOption(DETECTORS_SHORT, DETECTORS, true, "name all detectors you want to run separated by ',' "
                + " (all detectors defined in the README)");
        options.addOption(OUTPUT_LANG_SHORT, OUTPUT_LANG, true, "language of hints in the output");

        return options;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LitterBox", getCommandLineOptions(), true);
        System.out.println("Example: " + "java -jar Litterbox.jar --check --path "
                + "C:\\scratchprojects\\files\\ --output C:\\scratchprojects\\files\\test.csv --detectors bugs\n");
        System.out.println("Example for metric output: "
                + "java -jar Litterbox-1.0.jar --stats -o ~/path/to/folder/or/file/for/the/output --path " +
                "~/path/to/json/project/or/folder/with/projects \n");
        System.out.println("Example for Leila intermediate language output: "
                + "java -jar Litterbox-1.0.jar --leila -o ~/path/to/folder/or/file/for/the/output --path " +
                "~/path/to/json/project/or/folder/with/projects \n");

        System.out.println("Detectors:");
        ResourceBundle messages = ResourceBundle.getBundle("IssueDescriptions", Locale.ENGLISH);
        IssueTool iT = new IssueTool();
        System.out.printf("\t%-20s %-30s%n", ALL, messages.getString(ALL));
        System.out.printf("\t%-20s %-30s%n", BUGS, messages.getString(BUGS));
        System.out.printf("\t%-20s %-30s%n", SMELLS, messages.getString(SMELLS));
        // System.out.printf("\t%-20s %-30s%n", CTSCORE, messages.getString(CTSCORE));
        iT.getAllFinder().keySet().forEach(finder -> System.out.printf(
                "\t%-20s %-30s%n",
                finder,
                messages.getString(finder)
        ));
    }

    public static void checkPrograms(CommandLine cmd) throws ParseException, IOException {
        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String detectors = cmd.getOptionValue(DETECTORS, ALL);
        String path = cmd.getOptionValue(PROJECTPATH);
        BugAnalyzer analyzer = new BugAnalyzer(path, outputPath);
        analyzer.setDetectorNames(detectors);

        if (cmd.hasOption(ANNOTATE)) {
            String annotationPath = cmd.getOptionValue(ANNOTATE);
            analyzer.setAnnotationOutput(annotationPath);
        }

        runAnalysis(cmd, analyzer);
    }

    public static void translatePrograms(CommandLine cmd) throws ParseException, IOException {
        if (!cmd.hasOption(OUTPUT)) {
            throw new ParseException("Output path option '" + OUTPUT + "' required");
        }

        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String input = cmd.getOptionValue(PROJECTPATH);

        PrintAnalyzer analyzer = new PrintAnalyzer(input, outputPath);
        runAnalysis(cmd, analyzer);
    }

    public static void statsPrograms(CommandLine cmd) throws ParseException, IOException, ParsingException {
        if (!cmd.hasOption(OUTPUT)) {
            throw new ParseException("Output path option '" + OUTPUT + "' required");
        }

        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String input = cmd.getOptionValue(PROJECTPATH);
        MetricAnalyzer analyzer = new MetricAnalyzer(input, outputPath);
        runAnalysis(cmd, analyzer);
    }

    public static void runAnalysis(CommandLine cmd, Analyzer analyzer) {
        if (cmd.hasOption(PROJECTID)) {
            String projectId = cmd.getOptionValue(PROJECTID);
            analyzer.analyzeSingle(projectId);
        } else if (cmd.hasOption(PROJECTLIST)) {
            String projectList = cmd.getOptionValue(PROJECTLIST);
            analyzer.analyzeMultiple(projectList);
        } else {
            analyzer.analyzeFile();
        }
    }

    public static void parseCommandLine(String[] args) {
        Options options = getCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String lang = cmd.getOptionValue(OUTPUT_LANG, "en");
            Locale locale = Locale.forLanguageTag(lang);
            try {
                resourceBundle = ResourceBundle.getBundle("IssueDescriptions", locale);
            } catch (MissingResourceException e) {
                resourceBundle = ResourceBundle.getBundle("IssueDescriptions", Locale.ENGLISH);
                System.err.println("Could not load resource bundle for language " + lang + "; Defaulting to english");
            }

            if (cmd.hasOption(CHECK)) {
                checkPrograms(cmd);
            } else if (cmd.hasOption(STATS)) {
                statsPrograms(cmd);
            } else if (cmd.hasOption(LEILA)) {
                translatePrograms(cmd);
            } else {
                printHelp();
            }
        } catch (ParseException parseException) {
            System.err.println("Invalid option: " + parseException.getMessage());
            printHelp();
        } catch (IOException ioException) {
            System.err.println("Error while trying to read project: " + ioException.getMessage());
        } catch (ParsingException parseException) {
            System.err.println("Error while trying to parse project: " + parseException.getMessage());
        }
    }

    /**
     * Entry point to Litterbox where the arguments are parsed and the selected functionality is called.
     *
     * @param args Arguments that are parsed as options.
     */
    public static void main(String[] args) {
        parseCommandLine(args);
    }
}
