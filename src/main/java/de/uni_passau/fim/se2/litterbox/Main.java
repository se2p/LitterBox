package de.uni_passau.fim.se2.litterbox;/*
 * Copyright (C) 2019 LitterBox contributors
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


import java.io.File;
import java.util.logging.Logger;

import de.uni_passau.fim.se2.litterbox.analytics.Scratch3Analyzer;
import org.apache.commons.cli.*;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.ALL;

public class Main {

    private static final String PATH = "path";
    private static final String PROJECTID = "projectid";
    private static final String PROJECTLIST = "projectlist";
    private static final String PROJECTOUT = "projectout";
    private static final String OUTPUT = "output";
    private static final String DETECTORS = "detectors";
    private static final String GROUP = "detectors";
    private static final String HELP = "help";

    private static final Logger log = Logger.getLogger(Main.class.getName());

    private Main() {
    }

    /**
     * Entry point to Litterbox where the arguments are parsed and the selected functionality is called.
     *
     * @param args Arguments that are parsed as options.
     * @throws ParseException thrown when a Scratch Project cannot be parsed.
     */
    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption(PATH, true, "path to folder or file that should be analyzed (required)");
        options.addOption(PROJECTID, true,
                "id of the project that should be downloaded and analysed. Only works for Scratch 3");
        options.addOption(PROJECTLIST, true, "path to a file with a list of project ids of projects"
                + "which should be downloaded and analysed. Only works for Scratch 3");
        options.addOption(PROJECTOUT, true, "path where the downloaded project should be stored");
        options.addOption(OUTPUT, true, "path with name of the csv file you want to save (required if path argument"
                + " is a folder path)");
        options.addOption(DETECTORS, true, "name all detectors you want to run separated by ',' "
                + "\n(all detectors defined in the README)");
        options.addOption(GROUP, true, "choose a group of detectors to run smells, ctscore or bugs"
                + "\n(all detectors defined in the README)");
        options.addOption(HELP, false, "print this message");
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(PATH)) {
            File folder = new File(cmd.getOptionValue(PATH));
                if (cmd.hasOption(GROUP)) {
                    Scratch3Analyzer.analyze(cmd.getOptionValue(GROUP),
                            cmd.getOptionValue(OUTPUT), folder);
                } else {
                    Scratch3Analyzer.analyze(cmd.getOptionValue(DETECTORS, ALL),
                            cmd.getOptionValue(OUTPUT), folder);
                }
            return;
        } else if (cmd.hasOption(PROJECTID) || cmd.hasOption(PROJECTLIST)) {
            if (cmd.hasOption(PROJECTID)) {
                String projectid = cmd.getOptionValue(PROJECTID);
                if (cmd.hasOption(GROUP)) {
                    Scratch3Analyzer.downloadAndAnalyze(projectid, cmd.getOptionValue(PROJECTOUT),
                            cmd.getOptionValue(GROUP),
                            cmd.getOptionValue(OUTPUT));
                } else {
                    Scratch3Analyzer.downloadAndAnalyze(projectid, cmd.getOptionValue(PROJECTOUT),
                            cmd.getOptionValue(DETECTORS, ALL),
                            cmd.getOptionValue(OUTPUT));
                }
            }

            if (cmd.hasOption(PROJECTLIST)) {
                if (cmd.hasOption(GROUP)) {
                    Scratch3Analyzer.downloadAndAnalyzeMultiple(
                            cmd.getOptionValue(PROJECTLIST),
                            cmd.getOptionValue(PROJECTOUT),
                            cmd.getOptionValue(GROUP),
                            cmd.getOptionValue(OUTPUT));
                } else {
                    Scratch3Analyzer.downloadAndAnalyzeMultiple(
                            cmd.getOptionValue(PROJECTLIST),
                            cmd.getOptionValue(PROJECTOUT),
                            cmd.getOptionValue(DETECTORS, ALL),
                            cmd.getOptionValue(OUTPUT));
                }
            }
            return;
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LitterBox", options);
        System.out.println("Example: " + "java -cp C:\\ScratchAnalytics-1.0.jar de.uni_passau.fim.se2.litterbox.Main -path "
                + "C:\\scratchprojects\\files\\ -output C:\\scratchprojects\\files\\test.csv -detectors cnt,"
                + "glblstrt");
    }

}
