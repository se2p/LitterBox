/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.Code2SeqAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.Code2VecAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn.GgnnGraphAnalyzer;
import de.uni_passau.fim.se2.litterbox.utils.GroupConstants;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "LitterBox",
        mixinStandardHelpOptions = true,
        version = "LitterBox 1.9-SNAPSHOT",
        subcommands = {
                // general commands
                Main.CheckProgramsSubcommand.class,
                Main.DetectorsSubcommand.class,
                Main.FeatureSubcommand.class,
                Main.LeilaSubcommand.class,
                Main.RefactoringSubcommand.class,
                Main.StatsSubcommand.class,
                Main.DotSubcommand.class,
                Main.ExtractSubcommand.class,
                // machine learning preprocessors
                Main.Code2vecSubcommand.class,
                Main.GgnnSubcommand.class,
                Main.Code2SeqSubcommand.class,
        },
        footerHeading = "%nExamples:%n",
        footer = {
                "%nExample for searching for bugs:%n"
                        + "java -jar Litterbox.jar check%n"
                        + "    --path C:\\scratchprojects\\files\\%n"
                        + "    --output C:\\scratchprojects\\files\\test.csv%n"
                        + "    --detectors bugs",
                "%nExample for metrics output:%n"
                        + "java -jar Litterbox.jar stats%n"
                        + "    --path ~/path/to/json/project/or/folder/with/projects%n"
                        + "    -o ~/path/to/folder/or/file/for/the/output",
                "%nExample for Leila intermediate language output:%n"
                        + "java -jar Litterbox.jar leila%n"
                        + "    --path ~/path/to/json/project/or/folder/with/projects%n"
                        + "    -o ~/path/to/folder/or/file/for/the/output",
                "%nExample for refactoring:%n"
                        + "java -jar Litterbox.jar refactor%n"
                        + "    --path ~/path/to/project%n"
                        + "    -r ~/path/to/output/folder/for/refactored/project",
                "%nExample for code2vec preprocessing:%n"
                        + "java -jar Litterbox.jar code2vec%n"
                        + "    --path ~/path/to/json/project/or/folder/with/projects%n"
                        + "    -o ~/path/to/folder/or/file/for/the/output%n"
                        + "    --max-path-length 8",
        }
)
public class Main implements Callable<Integer> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        spec.commandLine().usage(System.out);
        return 0;
    }

    public static void main(String... args) {
        PropertyLoader.setDefaultSystemProperties("litterbox.properties");
        PropertyLoader.setGlobalLoggingLevelFromEnvironment();

        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(mixinStandardHelpOptions = true)
    abstract static class LitterBoxSubcommand implements Callable<Integer> {
        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

        @CommandLine.Option(
                names = {"-p", "--path"},
                description = "Path to the folder or file that should be analysed, "
                        + "or path in which to store downloaded projects."
        )
        Path projectPath;

        @CommandLine.Option(
                names = {"--project-id"},
                description = "ID of the project that should be downloaded and analysed."
        )
        String projectId;

        @CommandLine.Option(
                names = {"--project-list"},
                description = "Path to a file with a list of project ids which should be downloaded and analysed."
        )
        Path projectList;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Path to the file or folder for the analyser results. "
                        + "Has to be a folder if multiple projects are analysed."
        )
        Path outputPath;

        @CommandLine.Option(
                names = {"--delete"},
                description = "Delete the project files after analysing them."
        )
        boolean deleteProject;

        protected abstract Analyzer getAnalyzer() throws Exception;

        /**
         * Override to implement custom parameter validation before the analyzer is run.
         *
         * @throws Exception Thrown when an invalid parameter configuration was passed.
         */
        protected void validateParams() throws Exception {
            // intentionally empty here, to be implemented by subclasses when needed
        }

        @Override
        public final Integer call() throws Exception {
            IssueTranslator.getInstance().setLanguage(language);

            validateParams();

            final Analyzer analyzer = getAnalyzer();
            return runAnalysis(analyzer);
        }

        private int runAnalysis(final Analyzer analyzer) throws IOException {
            if (projectId != null) {
                analyzer.analyzeSingle(projectId);
            } else if (projectList != null) {
                analyzer.analyzeMultiple(projectList);
            } else {
                analyzer.analyzeFile();
            }

            return 0;
        }

        protected void requireProjectPath() throws CommandLine.ParameterException {
            if (projectPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Input path option '--path' required.");
            }
        }

        protected void requireOutputPath() throws CommandLine.ParameterException {
            if (outputPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Output path option '--output' required.");
            }
        }
    }

    @CommandLine.Command(
            name = "check",
            description = "Check Scratch projects for issues."
    )
    static class CheckProgramsSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-d", "--detectors"},
                description = "All detectors you want to run, separated by commas.",
                split = ",",
                defaultValue = "default",
                paramLabel = "<detector>"
        )
        List<String> detectors;

        @CommandLine.Option(
                names = {"-i", "--ignore-loose"},
                description = "Ignore loose blocks when checking bug patterns."
        )
        boolean ignoreLooseBlocks;

        @CommandLine.Option(
                names = {"-a", "--annotate"},
                description = "Path where Scratch files with hints to bug patterns should be created."
        )
        Path annotationPath;

        @CommandLine.Option(
                names = {"-s", "--scripts"},
                description = "Get the bug patterns per script."
        )
        boolean outputPerScript;

        @Override
        protected BugAnalyzer getAnalyzer() throws IOException {
            if (projectPath == null) {
                projectPath = Files.createTempDirectory("litterbox-bug");
            }

            final String detector = String.join(",", detectors);

            final BugAnalyzer analyzer = new BugAnalyzer(
                    projectPath,
                    outputPath,
                    detector,
                    ignoreLooseBlocks,
                    deleteProject,
                    outputPerScript
            );
            if (annotationPath != null) {
                analyzer.setAnnotationOutput(annotationPath);
            }

            return analyzer;
        }
    }

    @CommandLine.Command(
            name = "detectors",
            description = "Print a list of all detectors implemented in LitterBox.",
            mixinStandardHelpOptions = true
    )
    static class DetectorsSubcommand implements Callable<Integer> {

        private final IssueTranslator messages = IssueTranslator.getInstance();

        @CommandLine.Option(
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

        @Override
        public Integer call() throws Exception {
            IssueTranslator.getInstance().setLanguage(language);
            printDetectorList();
            return 0;
        }

        private void printDetectorList() {
            final String detectorFormat = "\t%-20s %-30s%n";

            System.out.println("Detectors:");
            System.out.printf(detectorFormat, GroupConstants.ALL, messages.getInfo(GroupConstants.ALL));
            System.out.printf(detectorFormat, GroupConstants.BUGS, messages.getInfo(GroupConstants.BUGS));
            System.out.printf(detectorFormat, GroupConstants.SMELLS, messages.getInfo(GroupConstants.SMELLS));
            System.out.printf(detectorFormat, GroupConstants.PERFUMES, messages.getInfo(GroupConstants.PERFUMES));

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Bugpatterns:", "");
            printDetectorGroup(IssueTool.getBugFinderNames());

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Smells:", "");
            printDetectorGroup(IssueTool.getSmellFinderNames());

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Perfumes:", "");
            printDetectorGroup(IssueTool.getPerfumeFinderNames());
        }

        private void printDetectorGroup(final Collection<String> detectors) {
            detectors.forEach(finder -> System.out.printf("\t%-20s %-30s%n", finder, messages.getName(finder)));
        }
    }

    @CommandLine.Command(
            name = "features",
            description = "Extract scripts features from Scratch projects. See also 'stats' subcommand."
    )
    static class FeatureSubcommand extends LitterBoxSubcommand {
        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected FeatureAnalyzer getAnalyzer() {
            return new FeatureAnalyzer(projectPath, outputPath, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "leila",
            description = "Translate Scratch projects to Leila."
    )
    static class LeilaSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-n", "--nondet"},
                description = "Allow non deterministic (i.e., non-initialised) attributes in the intermediate language."
        )
        boolean nonDeterministic;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected LeilaAnalyzer getAnalyzer() {
            return new LeilaAnalyzer(projectPath, outputPath, nonDeterministic, false, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "refactoring",
            description = "Refactor specified Scratch projects."
    )
    static class RefactoringSubcommand extends LitterBoxSubcommand {

        @CommandLine.Option(
                names = {"-r", "--refactored-projects"},
                description = "Path where the refactored Scratch projects should be created."
        )
        Path refactoredPath;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
        }

        @Override
        protected RefactoringAnalyzer getAnalyzer() {
            return new RefactoringAnalyzer(
                    projectPath,
                    outputPath,
                    refactoredPath,
                    deleteProject
            );
        }
    }

    @CommandLine.Command(
            name = "stats",
            description = "Extract project metrics. See also 'features' subcommand."
    )
    static class StatsSubcommand extends LitterBoxSubcommand {

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected MetricAnalyzer getAnalyzer() {
            return new MetricAnalyzer(projectPath, outputPath, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "extract",
            description = "Extract names of various things in Scratch projects."
    )
    static class ExtractSubcommand extends LitterBoxSubcommand {

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected ExtractionAnalyzer getAnalyzer() {
            return new ExtractionAnalyzer(projectPath, outputPath, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "dot",
            description = "Convert the project into a .dot file."
    )
    static class DotSubcommand extends LitterBoxSubcommand {

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected DotAnalyzer getAnalyzer() {
            return new DotAnalyzer(projectPath, outputPath, deleteProject);
        }
    }

    abstract static class MLPreprocessorSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-s", "--include-stage"},
                description = "Include the stage like a regular sprite into the analysis."
        )
        boolean includeStage;

        @CommandLine.Option(
                names = {"-w", "--whole-program"},
                description = "Treat the program as a single big sprite."
        )
        boolean wholeProgram;

        @CommandLine.Option(
                names = {"--include-default-sprites"},
                description = "Include sprites that have the default name in any language, e.g. ‘Sprite1’, ‘Actor3’."
        )
        boolean includeDefaultSprites;

        protected final MLOutputPath getOutputPath() throws CommandLine.ParameterException {
            if (outputPath != null) {
                final File outputDirectory = outputPath.toFile();
                if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
                    throw new CommandLine.ParameterException(
                            spec.commandLine(),
                            "The output path for a machine learning preprocessor must be a directory."
                    );
                }
                return MLOutputPath.directory(outputDirectory.toPath());
            } else {
                return MLOutputPath.console();
            }
        }

        protected final MLPreprocessorCommonOptions getCommonOptions() {
            requireProjectPath();

            final MLOutputPath outputPath = getOutputPath();
            return new MLPreprocessorCommonOptions(projectPath, outputPath, deleteProject, includeStage, wholeProgram,
                    includeDefaultSprites);
        }
    }

    private abstract static class Code2Subcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
                names = {"--max-path-length"},
                description = "The maximum length for connecting two AST leaves. "
                        + "Zero means there is no max path length. "
                        + "Default: 8."
        )
        int maxPathLength = 8;

        @CommandLine.Option(
                names = {"--scripts"},
                description = "Generate token per script."
        )
        boolean isPerScript = false;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            if (maxPathLength < 0) {
                throw new CommandLine.ParameterException(spec.commandLine(), "The path length can’t be negative.");
            }

            if (wholeProgram && isPerScript) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        "The analysis must be done either per script or for whole program"
                );
            }
        }
    }

    @CommandLine.Command(
            name = "code2vec",
            description = "Transform Scratch projects into the code2vec input format."
    )
    static class Code2vecSubcommand extends Code2Subcommand {

        @Override
        protected Code2VecAnalyzer getAnalyzer() {
            return new Code2VecAnalyzer(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
            name = "code2seq",
            description = "Transform Scratch projects into the code2seq input format."
    )
    static class Code2SeqSubcommand extends Code2Subcommand {

        @Override
        protected Code2SeqAnalyzer getAnalyzer() {
            return new Code2SeqAnalyzer(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
            name = "ggnn",
            description = "Transform Scratch projects into the Gated Graph Neural Network input format."
    )
    static class GgnnSubcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
                names = {"--label"},
                description = "Use a specific label for the graph instead of the file name."
        )
        String label;

        @CommandLine.Option(
                names = {"--dotgraph"},
                description = "Generate a dot-graph representation of the GGNN graph."
        )
        boolean dotGraph;

        @Override
        protected GgnnGraphAnalyzer getAnalyzer() {
            return new GgnnGraphAnalyzer(getCommonOptions(), dotGraph, label);
        }
    }
}
