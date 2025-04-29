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

import de.uni_passau.fim.se2.litterbox.analytics.FileAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.llm.*;
import de.uni_passau.fim.se2.litterbox.llm.prompts.CommonQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "llm",
        description = "Query an LLM for a given Scratch project.",
        mixinStandardHelpOptions = true,
        subcommands = {
                LlmSubcommand.AnalyzeSubcommand.class,
                LlmSubcommand.CompleteSubcommand.class,
                LlmSubcommand.FixSubcommand.class,
                LlmSubcommand.QuerySubcommand.class,
                LlmSubcommand.CommonQuerySubcommand.class,
        }
)
class LlmSubcommand implements Callable<Integer> {

    abstract static class LlmSubcommandBase extends Main.LitterBoxSubcommand {

        @CommandLine.Option(
                names = {"-t", "--target"},
                description = "Target sprite name."
        )
        String spriteName;

        @CommandLine.Option(
                names = {"-s", "--script"},
                description = "Target script ID."
        )
        String scriptId;

        @CommandLine.Option(
                names = {"-i", "--ignore-loose"},
                description = "Ignore loose blocks when checking bug patterns."
        )
        boolean ignoreLooseBlocks;

        protected QueryTarget buildQueryTarget() {
            if (spriteName != null) {
                return new QueryTarget.SpriteTarget(spriteName);
            } else if (scriptId != null) {
                return new QueryTarget.ScriptTarget(scriptId);
            } else {
                return new QueryTarget.ProgramTarget();
            }
        }
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        throw new CommandLine.ParameterException(
                spec.commandLine(),
                "Cannot use 'llm' subcommand directly. Use 'llm --help' to see available further subcommands."
        );
    }

    @CommandLine.Command(
            name = "analyze",
            description = "Use LLM to improve LitterBox analysis.",
            mixinStandardHelpOptions = true
    )
    static class AnalyzeSubcommand extends LlmSubcommandBase {

        static class AnalysisOptions {
            @CommandLine.Option(
                    names = {"--fixes"},
                    defaultValue = "false",
                    description = "Produce suggested fixes/refactorings."
            )
            boolean fix;

            @CommandLine.Option(
                    names = {"--filter"},
                    defaultValue = "false",
                    description = "Filter false positives."
            )
            boolean filter;

            @CommandLine.Option(
                    names = {"--hint"},
                    defaultValue = "false",
                    description = "Produce textual explanations."
            )
            boolean hint;

            @CommandLine.Option(
                    names = {"--effect"},
                    defaultValue = "false",
                    description = "Describe possible effects."
            )
            boolean effects;

            @CommandLine.Option(
                    names = {"--new-issues"},
                    defaultValue = "false",
                    description = "Add further issues."
            )
            boolean addIssues;

            @CommandLine.Option(
                    names = {"--new-perfumes"},
                    defaultValue = "false",
                    description = "Add further code perfumes."
            )
            boolean addPerfumes;
        }

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "1..1")
        AnalysisOptions analysisOptions = new AnalysisOptions();

        @CommandLine.Option(
                names = {"-d", "--detectors"},
                description = "All detectors you want to run, separated by commas.",
                split = ",",
                defaultValue = "flaws",
                paramLabel = "<detector>"
        )
        List<String> detectors;

        @Override
        protected void validateParams() {
            requireProjectPath();
        }

        @Override
        protected FileAnalyzer<?> getAnalyzer() {
            return buildEnhancer(buildQueryTarget());
        }

        private LLMAnalysisEnhancer buildEnhancer(QueryTarget target) {
            final LLMAnalysisEnhancer enhancer = new LLMAnalysisEnhancer(
                    buildQueryTarget(),
                    outputPath,
                    String.join(",", detectors),
                    ignoreLooseBlocks
            );
            if (analysisOptions.addIssues) {
                enhancer.addIssueProcessor(new LLMIssueBugExtender(target));
            }
            if (analysisOptions.addPerfumes) {
                enhancer.addIssueProcessor(new LLMIssuePerfumeExtender(target));
            }
            if (analysisOptions.filter) {
                enhancer.addIssueProcessor(new LLMIssueFalsePositiveFilter(target));
            }
            if (analysisOptions.fix) {
                enhancer.addIssueProcessor(new LLMIssueFixProcessor(target));
            }
            if (analysisOptions.hint) {
                enhancer.addIssueProcessor(new LLMIssueHintProcessor(target));
            }
            if (analysisOptions.effects) {
                enhancer.addIssueProcessor(new LLMIssueEffectExplainer(target));
            }
            return enhancer;
        }
    }

    @CommandLine.Command(
            name = "complete",
            description = "Asks the LLM to autocomplete code in the project.",
            mixinStandardHelpOptions = true
    )
    static class CompleteSubcommand extends LlmSubcommandBase {

        @Override
        protected void validateParams() {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected FileAnalyzer<?> getAnalyzer() throws Exception {
            final LLMProgramCompletionAnalyzer analyzer = new LLMProgramCompletionAnalyzer(
                    buildQueryTarget(), ignoreLooseBlocks
            );
            return new LLMCodeAnalyzer(analyzer, outputPath, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "fix",
            description = "Ask LLM to fix bugs or smells.",
            mixinStandardHelpOptions = true
    )
    static class FixSubcommand extends LlmSubcommandBase {

        @CommandLine.Option(
                names = {"-d", "--detectors"},
                description = "All detectors you want to run, separated by commas.",
                split = ",",
                defaultValue = "flaws",
                paramLabel = "<detector>"
        )
        List<String> detectors;

        @Override
        protected void validateParams() {
            requireProjectPath();
            requireOutputPath();
        }

        @Override
        protected FileAnalyzer<?> getAnalyzer() {
            final LLMProgramImprovementAnalyzer analyzer = new LLMProgramImprovementAnalyzer(
                    buildQueryTarget(), String.join(",", detectors), ignoreLooseBlocks
            );
            return new LLMCodeAnalyzer(analyzer, outputPath, deleteProject);
        }
    }

    @CommandLine.Command(
            name = "query",
            description = "Query an LLM for a given Scratch project.",
            mixinStandardHelpOptions = true
    )
    static class QuerySubcommand extends LlmSubcommandBase {

        @Override
        protected void validateParams() {
            requireProjectPath();
        }

        @CommandLine.Parameters(description = "Custom query for the LLM.")
        String query;

        @Override
        protected FileAnalyzer<?> getAnalyzer() {
            final LlmQuery q = new LlmQuery.CustomQuery(query);
            return new LLMQueryAnalyzer(outputPath, deleteProject, q, buildQueryTarget(), ignoreLooseBlocks);
        }
    }

    @CommandLine.Command(
            name = "predefined-query",
            description = "Query an LLM for a given Scratch project.",
            mixinStandardHelpOptions = true
    )
    static class CommonQuerySubcommand extends LlmSubcommandBase {

        @Override
        protected void validateParams() {
            requireProjectPath();
        }

        @CommandLine.Parameters(
                description = "Choose a predefined query for the LLM. Possible choices: ${COMPLETION-CANDIDATES}"
        )
        CommonQuery commonQuery;

        @Override
        protected FileAnalyzer<?> getAnalyzer() {
            final LlmQuery q = new LlmQuery.PredefinedQuery(commonQuery);
            return new LLMQueryAnalyzer(outputPath, deleteProject, q, buildQueryTarget(), ignoreLooseBlocks);
        }
    }
}
