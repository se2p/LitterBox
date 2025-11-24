/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.ScratchClient;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@CommandLine.Command(
        name = "LitterBox",
        mixinStandardHelpOptions = true,
        version = "LitterBox 1.11-SNAPSHOT",
        subcommands = {
                // general commands
                Main.CheckProgramsSubcommand.class,
                LlmSubcommand.class,
                Main.DetectorsSubcommand.class,
                Main.FeatureSubcommand.class,
                Main.LeilaSubcommand.class,
                Main.RefactoringSubcommand.class,
                Main.StatsSubcommand.class,
                Main.DotSubcommand.class,
                Main.ScratchBlocksSubcommand.class,
                Main.ExtractSubcommand.class,
                Main.MineSubcommand.class,
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
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

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

        protected IssueTranslator translator;

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
            final Locale locale = Locale.of(commonOptions.language);
            translator = IssueTranslatorFactory.getIssueTranslator(locale);

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

        @CommandLine.Option(
                names = {"-r", "--report_prior"},
                description = "Path to prior analyser results, which should be compared to new analysis."
        )
        Path priorResultPath;

        @Override
        protected BugAnalyzer getAnalyzer() throws IOException {
            if (projectInput.projectPath == null) {
                projectInput.projectPath = Files.createTempDirectory("litterbox-bug");
            }

            final String detector = String.join(",", detectors);
            final BugAnalyzer analyzer;
            if (priorResultPath == null) {
                analyzer = new BugAnalyzer(
                        translator,
                        commonOptions.outputPath,
                        detector,
                        ignoreLooseBlocks,
                        commonOptions.deleteProject,
                        outputPerScript
                );
            } else {
                analyzer = new BugAnalyzer(
                        translator,
                        commonOptions.outputPath,
                        detector,
                        ignoreLooseBlocks,
                        commonOptions.deleteProject,
                        outputPerScript,
                        priorResultPath
                );
            }
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
            description = "Extract scripts features from Scratch projects. See also 'stats' subcommand."
    )
    static class FeatureSubcommand extends LitterBoxSubcommand {
        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
        }

        @Override
        protected FeatureAnalyzer getAnalyzer() {
            return new FeatureAnalyzer(commonOptions.outputPath, commonOptions.deleteProject);
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
            return new LeilaAnalyzer(commonOptions.outputPath, nonDeterministic, false, commonOptions.deleteProject);
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
            return new RefactoringAnalyzer(commonOptions.outputPath, refactoredPath, commonOptions.deleteProject);
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

    @CommandLine.Command(
            name = "scratchblocks",
            description = "Convert the project into scratchblocks notation."
    )
    static class ScratchBlocksSubcommand extends LitterBoxSubcommand {

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            requireProjectPath();
        }

        @Override
        protected ScratchBlocksAnalyzer getAnalyzer() {
            return new ScratchBlocksAnalyzer(commonOptions.outputPath, commonOptions.deleteProject);
        }
    }

    @CommandLine.Command(
            name = "mine",
            description = "Download projects from Scratch.",
            mixinStandardHelpOptions = true
    )
    static class MineSubcommand implements Callable<Integer> {

        private static final Logger log = Logger.getLogger(MineSubcommand.class.getName());

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Output directory for downloaded projects.",
                required = true
        )
        Path outputPath;

        @CommandLine.Option(names = {"--metadata"}, description = "Download project metadata.")
        boolean metadata;

        @CommandLine.Option(names = {"--sb3"}, description = "Download as .sb3 file (zipped JSON + assets).")
        boolean sb3;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "1..*")
        DownloadKind downloadKind;

        static class DownloadKind {
            @CommandLine.Option(names = {"--project-id"}, description = "ID of the project to download.")
            String projectId;

            @CommandLine.Option(names = {"--project-list"}, description = "File containing a list of project IDs.")
            Path projectList;

            @CommandLine.ArgGroup(exclusive = false)
            RangeDownload rangeDownload;

            @CommandLine.Option(names = {"--recent"}, description = "Download X most recent projects.")
            Integer recent;

            @CommandLine.Option(names = {"--popular"}, description = "Download X most popular projects.")
            Integer popular;

            @CommandLine.Option(names = {"--user"}, description = "Download all projects of a user.")
            String user;
        }

        // all options within group required, but the rangeDownload group is optional above: if one of the two options
        // from/to in here is specified, then the other one is also required. This is enforced by the picocli library.
        static class RangeDownload {
            @CommandLine.Option(
                    names = {"--from"},
                    description = "Start project ID for range download.",
                    required = true
            )
            Integer fromId;

            @CommandLine.Option(
                    names = {"--to"},
                    description = "End project ID for range download.",
                    required = true
            )
            Integer toId;
        }

        private final ScratchClient client;

        public MineSubcommand() {
            this.client = new ScratchClient();
        }

        public MineSubcommand(ScratchClient client) {
            this.client = client;
        }

        @Override
        public Integer call() throws Exception {
            if (downloadKind.projectId != null) {
                processId(downloadKind.projectId);
            }

            if (downloadKind.projectList != null) {
                if (Files.exists(downloadKind.projectList)) {
                    List<String> ids = Files.readAllLines(downloadKind.projectList);
                    for (String id : ids) {
                        processId(id.trim());
                    }
                } else {
                    log.warning("Project list file not found: " + downloadKind.projectList);
                }
            }

            if (downloadKind.rangeDownload != null) {
                for (int i = downloadKind.rangeDownload.fromId; i <= downloadKind.rangeDownload.toId; i++) {
                    processId(String.valueOf(i));
                }
            }

            if (downloadKind.recent != null) {
                List<String> ids = client.getRecentProjects(downloadKind.recent);
                for (String id : ids) {
                    processId(id);
                }
            }

            if (downloadKind.popular != null) {
                List<String> ids = client.getPopularProjects(downloadKind.popular);
                for (String id : ids) {
                    processId(id);
                }
            }

            if (downloadKind.user != null) {
                List<String> ids = client.getUserProjects(downloadKind.user);
                for (String id : ids) {
                    processId(id);
                }
            }

            return 0;
        }

        private void processId(String id) {
            try {
                log.info("Downloading project " + id + "...");
                client.downloadProject(id, outputPath, sb3);
                if (metadata) {
                    client.downloadMetadata(id, outputPath);
                }
            } catch (IOException e) {
                log.severe("Failed to download project " + id + ": " + e.getMessage());
            }
        }
    }
}
