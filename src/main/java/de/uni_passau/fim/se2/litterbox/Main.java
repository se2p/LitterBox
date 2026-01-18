/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.export.ExportAnalyzer;
import de.uni_passau.fim.se2.litterbox.export.ExportFormat;
import de.uni_passau.fim.se2.litterbox.injection.InjectorAnalyzer;
import de.uni_passau.fim.se2.litterbox.injection.InjectorTool;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "LitterBox",
        mixinStandardHelpOptions = true,
        version = "LitterBox 1.12-SNAPSHOT",
        subcommands = {
                // general commands
                Main.CheckProgramsSubcommand.class,
                Main.InjectSubcommand.class,
                LlmSubcommand.class,
                DiffSubcommand.class,
                Main.DetectorsSubcommand.class,
                Main.FeatureSubcommand.class,
                Main.ExportSubcommand.class,
                Main.RefactoringSubcommand.class,
                Main.StatsSubcommand.class,
                Main.DotSubcommand.class,
                Main.ExtractSubcommand.class,
                MineSubcommand.class,
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
                "%nExample for exporting to different formats:%n"
                        + "java -jar Litterbox.jar export%n"
                        + "    --path ~/path/to/project%n"
                        + "    --format goboscript%n"
                        + "    -o ~/path/to/output/folder",
                "%nExample for refactoring:%n"
                        + "java -jar Litterbox.jar refactoring%n"
                        + "    --path ~/path/to/project%n"
                        + "    -r ~/path/to/output/folder/for/refactored/project"
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

        int exitCode = new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }

    static class ProjectInputOptions {
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

        static ProjectInputOptions getDefault() {
            return new ProjectInputOptions();
        }
    }

    static class CommonOptions {

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

        static CommonOptions getDefault() {
            return new CommonOptions();
        }
    }

    @CommandLine.Command(mixinStandardHelpOptions = true)
    abstract static class LitterBoxSubcommand implements Callable<Integer> {
        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @CommandLine.ArgGroup(exclusive = false)
        CommonOptions commonOptions = CommonOptions.getDefault();

        @CommandLine.ArgGroup(exclusive = false)
        ProjectInputOptions projectInput = ProjectInputOptions.getDefault();

        protected abstract FileAnalyzer<?> getAnalyzer() throws Exception;

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

            validateParams();

            final FileAnalyzer<?> analyzer = getAnalyzer();
            return runAnalysis(analyzer);
        }

        private int runAnalysis(final FileAnalyzer<?> analyzer) throws IOException {
            if (projectInput.projectId != null) {
                final var projectIdAnalyzer = new ProjectIdAnalyzer<>(
                        analyzer, projectInput.projectPath, commonOptions.deleteProject
                );
                projectIdAnalyzer.analyzeSingle(projectInput.projectId);
            } else if (projectInput.projectList != null) {
                final var projectIdAnalyzer = new ProjectIdAnalyzer<>(
                        analyzer, projectInput.projectPath, commonOptions.deleteProject
                );
                projectIdAnalyzer.analyzeMultiple(projectInput.projectList);
            } else {
                analyzer.analyzeFile(projectInput.projectPath);
            }

            return 0;
        }

        protected void requireProjectPath() throws CommandLine.ParameterException {
            if (projectInput.projectPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Input path option '--path' required.");
            }
        }

        protected void requireOutputPath() throws CommandLine.ParameterException {
            if (commonOptions.outputPath == null) {
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
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

        private IssueTranslator translator;

        @CommandLine.Option(
                names = {"-d", "--detectors"},
                description = "All detectors or detector groups you want to run, separated by commas. "
                        + "Prefix group names or names of individual detectors with '-' to exclude them.",
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
            translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(language));

            if (projectInput.projectPath == null) {
                projectInput.projectPath = Files.createTempDirectory("litterbox-bug");
            }

            final String detector = String.join(",", detectors);
            final BugAnalyzer analyzer = new BugAnalyzer(
                    translator,
                    commonOptions.outputPath,
                    detector,
                    ignoreLooseBlocks,
                    commonOptions.deleteProject,
                    outputPerScript
            );
            if (annotationPath != null) {
                analyzer.setAnnotationOutput(annotationPath);
            }

            return analyzer;
        }
    }

    @CommandLine.Command(
            name = "inject",
            description = "Inject bugs into Scratch projects."
    )
    static class InjectSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-i", "--injectors"},
                description = "Comma-separated list of injectors to run (or 'all').",
                defaultValue = "all"
        )
        String injectors;

        @CommandLine.Option(
                names = {"--csv"},
                description = "Path to a CSV file for logging injection metadata. "
                        + "If the file exists, rows are appended; otherwise, a new file is created with headers."
        )
        Path csvPath;

        @CommandLine.Option(
                names = {"--skip-analyzed"},
                description = "Skip projects that already have output files in the output directory."
        )
        boolean skipAnalyzed;

        @CommandLine.Option(
                names = {"-l", "--language"},
                description = "Language for hint text in CSV output (e.g., 'en', 'de').",
                defaultValue = "en"
        )
        String language;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected InjectorAnalyzer getAnalyzer() {
            IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(language));
            return new InjectorAnalyzer(
                    commonOptions.outputPath,
                    commonOptions.deleteProject,
                    InjectorTool.getInjectors(injectors),
                    csvPath,
                    skipAnalyzed,
                    translator
            );
        }
    }

    @CommandLine.Command(
            name = "detectors",
            description = "Print a list of all detectors implemented in LitterBox.",
            mixinStandardHelpOptions = true
    )
    static class DetectorsSubcommand implements Callable<Integer> {

        private IssueTranslator translator;

        @CommandLine.Option(
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

        @Override
        public Integer call() throws Exception {
            translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(language));
            printDetectorList();
            return 0;
        }

        private void printDetectorList() {
            final String detectorFormat = "\t%-20s %-30s%n";

            System.out.println("Detectors:");
            System.out.printf(detectorFormat, FinderGroup.ALL, translator.getInfo(FinderGroup.ALL));
            System.out.printf(detectorFormat, FinderGroup.BUGS, translator.getInfo(FinderGroup.BUGS));
            System.out.printf(detectorFormat, FinderGroup.SMELLS, translator.getInfo(FinderGroup.SMELLS));
            System.out.printf(detectorFormat, FinderGroup.PERFUMES, translator.getInfo(FinderGroup.PERFUMES));
            System.out.printf(detectorFormat, FinderGroup.QUESTIONS, translator.getInfo(FinderGroup.QUESTIONS));
            System.out.printf(detectorFormat, FinderGroup.FLAWS, translator.getInfo(FinderGroup.FLAWS));

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Bugpatterns:", "");
            printDetectorGroup(IssueTool.getBugFinderNames());

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Smells:", "");
            printDetectorGroup(IssueTool.getSmellFinderNames());

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Perfumes:", "");
            printDetectorGroup(IssueTool.getPerfumeFinderNames());

            System.out.println(System.lineSeparator());
            System.out.printf(detectorFormat, "Questions:", "");
            printDetectorGroup(IssueTool.getQuestionFinderNames());
        }

        private void printDetectorGroup(final Collection<String> detectors) {
            detectors.forEach(finder -> System.out.printf("\t%-20s %-30s%n", finder, translator.getName(finder)));
        }
    }

    @CommandLine.Command(
            name = "features",
            description = "Extract scripts features from Scratch projects. See also 'stats' subcommand.",
            mixinStandardHelpOptions = true
    )
    static class FeatureSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-a", "--actor"},
                description = "Extract features from this actor name only."
        )
        String actor;

        @CommandLine.Option(
                names = {"--per-actor"},
                description = "Compute metrics per actor instead of per script/procedure."
        )
        boolean perActor;

        @CommandLine.Option(
                names = {"--collect-average-blocktype-count"},
                description = "Compute block-type metrics as proportion to all blocks rather than in absolute numbers. "
                        + "This proportion is usually called 'average' in literature."
        )
        boolean collectAverageBlocktypeCount;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
        }

        @Override
        protected FeatureAnalyzer getAnalyzer() {
            return new FeatureAnalyzer(
                    commonOptions.outputPath, commonOptions.deleteProject, actor, perActor, collectAverageBlocktypeCount
            );
        }
    }

    @CommandLine.Command(
            name = "export",
            description = "Export Scratch projects to various text-based formats."
    )
    static class ExportSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-f", "--format"},
                description = "Export format. Values: ${COMPLETION-CANDIDATES}",
                required = true
        )
        ExportFormat format;

        @CommandLine.Option(
                names = {"-n", "--nondet"},
                description = "For Leila: allow non-deterministic (non-initialised) attributes."
        )
        boolean nonDeterministic;

        @CommandLine.Option(
                names = {"--full-project"},
                description = "For Goboscript: create a full project structure with separate files and assets."
        )
        boolean fullProject;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
        }

        @Override
        protected ExportAnalyzer getAnalyzer() {
            return new ExportAnalyzer(
                    format,
                    commonOptions.outputPath,
                    commonOptions.deleteProject,
                    nonDeterministic,
                    fullProject
            );
        }
    }

    @CommandLine.Command(
            name = "refactoring",
            description = "Refactor specified Scratch projects."
    )
    static class RefactoringSubcommand extends LitterBoxSubcommand {

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected RefactoringAnalyzer getAnalyzer() {
            return new RefactoringAnalyzer(commonOptions.outputPath, commonOptions.deleteProject);
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
        }

        @Override
        protected MetricAnalyzer getAnalyzer() {
            return new MetricAnalyzer(commonOptions.outputPath, commonOptions.deleteProject);
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
        }

        @Override
        protected ExtractionAnalyzer getAnalyzer() {
            return new ExtractionAnalyzer(commonOptions.outputPath, commonOptions.deleteProject);
        }
    }

    @CommandLine.Command(
            name = "dot",
            description = "Convert the project into a .dot file."
    )
    static class DotSubcommand extends LitterBoxSubcommand {

        @CommandLine.Option(
                names = {"-g", "--graph"},
                description = "The type of graph to generate. Default: AST. Values: ${COMPLETION-CANDIDATES}",
                defaultValue = "AST"
        )
        GraphType graphType;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected DotAnalyzer getAnalyzer() {
            return new DotAnalyzer(commonOptions.outputPath, commonOptions.deleteProject, graphType);
        }
    }


}
