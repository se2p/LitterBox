/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import de.uni_passau.fim.se2.embedded_kittens.astnn.AstnnPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.code2.Code2SeqPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.code2.Code2VecPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.ggnn.GgnnGraphPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.ggnn.GgnnOutputFormat;
import de.uni_passau.fim.se2.embedded_kittens.llm.LlmPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.embedded_kittens.shared.WholeProgramFileProcessor;
import de.uni_passau.fim.se2.embedded_kittens.shared.WholeProgramJsonProcessor;
import de.uni_passau.fim.se2.embedded_kittens.tokenizer.TokenizingMethod;
import de.uni_passau.fim.se2.embedded_kittens.tokenizer.TokenizingPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.embedded_kittens.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import picocli.CommandLine;

@CommandLine.Command(
    name = "EmbeddedKittens",
    mixinStandardHelpOptions = true,
    version = "EmbeddedKittens 1.1.0-SNAPSHOT",
    subcommands = {
        Main.AstnnSubcommand.class,
        Main.Code2SeqSubcommand.class,
        Main.Code2vecSubcommand.class,
        Main.GgnnSubcommand.class,
        Main.LlmSubcommand.class,
        Main.TokenizerSubcommand.class
    },
    footerHeading = "%nExamples:%n",
    footer = {
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
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(mixinStandardHelpOptions = true)
    abstract static class MLPreprocessorSubcommand implements Callable<Integer> {

        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
            names = { "-l", "--lang" },
            description = "Language of the hints in the output."
        )
        String language = "en";

        @CommandLine.Option(
            names = { "-p", "--path" },
            description = "Path to the folder or file that should be analysed, "
                + "or path in which to store downloaded projects."
        )
        Path projectPath;

        @CommandLine.Option(
            names = { "-o", "--output" },
            description = "Path to the file or folder for the analyser results. "
                + "Has to be a folder if multiple projects are analysed."
        )
        Path outputPath;

        @CommandLine.Option(
            names = { "--exclude-stage" },
            description = "Ignore the stage when performing the analysis."
        )
        boolean excludeStage = false;

        @CommandLine.ArgGroup(exclusive = true)
        WholeProgramGroup wholeProgram = new WholeProgramGroup();

        static class WholeProgramGroup {

            @CommandLine.Option(
                names = { "-w", "--whole-program" },
                description = "Treat the program as a single big sprite."
            )
            boolean wholeProgram = false;

            @CommandLine.Option(
                names = { "--whole-program-json" },
                description = "Creates a JSON per program with the sub-outputs per sprite inside."
            )
            boolean wholeProgramJson = false;
        }

        @CommandLine.Option(
            names = { "--exclude-default-sprites" },
            description = "Ignore sprites that have the default name in any language, e.g. ‘Sprite1’, ‘Actor3’."
        )
        boolean excludeDefaultSprites = false;

        @CommandLine.Option(
            names = { "--abstract-tokens" },
            description = "Replaces literal values and variable names with abstract tokens. "
                + "E.g., '1.0' -> 'numberliteral'."
        )
        boolean abstractTokens = false;

        @CommandLine.Option(
            names = { "--latin-only-sprite-names" },
            description = "Normalise sprite names to include characters in the latin base alphabet (a-z) only."
        )
        boolean latinOnlyActorNames;

        protected abstract MLFilePreprocessor<?> getAnalyzer() throws Exception;

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

            final MLFilePreprocessor<?> analyzer = getAnalyzer();
            if (wholeProgram.wholeProgramJson) {
                return runWholeProgramJsonAnalysis(analyzer.getProgramPreprocessor());
            }
            else {
                return runAnalysis(analyzer);
            }
        }

        private int runWholeProgramJsonAnalysis(final MLProgramPreprocessor<?> baseProcessor) {
            final WholeProgramJsonProcessor<?> wholeProgramJsonProcessor = new WholeProgramJsonProcessor<>(
                getCommonOptions(), baseProcessor
            );
            final WholeProgramFileProcessor<?> wholeProgramFileProcessor = new WholeProgramFileProcessor<>(
                wholeProgramJsonProcessor, getOutputPath()
            );

            wholeProgramFileProcessor.processProgram(projectPath);

            return 0;
        }

        private int runAnalysis(final MLFilePreprocessor<?> analyzer) {
            if (wholeProgram.wholeProgram) {
                analyzer.processProgram(projectPath);
            }
            else {
                analyzer.processPerSprite(projectPath);
            }
            return 0;
        }

        protected void requireProjectPath() throws CommandLine.ParameterException {
            if (projectPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Input path option '--path' required.");
            }
        }

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
            }
            else {
                return MLOutputPath.console();
            }
        }

        protected final MLPreprocessorCommonOptions getCommonOptions() {
            requireProjectPath();

            return new MLPreprocessorCommonOptions(
                getOutputPath(), !excludeStage, !excludeDefaultSprites, abstractTokens, buildActorNameNormalizer()
            );
        }

        private ActorNameNormalizer buildActorNameNormalizer() {
            if (latinOnlyActorNames) {
                return NodeNameUtil::normalizeSpriteNameLatinOnly;
            }
            else {
                return ActorNameNormalizer.getDefault();
            }
        }
    }

    @CommandLine.Command(
        name = "astnn",
        description = "Transform Scratch projects into the ASTNN input format."
    )
    static class AstnnSubcommand extends MLPreprocessorSubcommand {

        @Override
        protected AstnnPreprocessor getAnalyzer() {
            return new AstnnPreprocessor(getCommonOptions());
        }
    }

    private abstract static class Code2Subcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
            names = { "--max-path-length" },
            description = "The maximum length for connecting two AST leaves. "
                + "Zero means there is no max path length. "
                + "Default: 8."
        )
        int maxPathLength = 8;

        @CommandLine.Option(
            names = { "--scripts" },
            description = "Generate token per script."
        )
        boolean isPerScript = false;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            if (maxPathLength < 0) {
                throw new CommandLine.ParameterException(spec.commandLine(), "The path length can’t be negative.");
            }

            if (wholeProgram.wholeProgram && isPerScript) {
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
        protected Code2VecPreprocessor getAnalyzer() {
            return new Code2VecPreprocessor(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
        name = "code2seq",
        description = "Transform Scratch projects into the code2seq input format."
    )
    static class Code2SeqSubcommand extends Code2Subcommand {

        @Override
        protected Code2SeqPreprocessor getAnalyzer() {
            return new Code2SeqPreprocessor(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
        name = "ggnn",
        description = "Transform Scratch projects into the Gated Graph Neural Network input format."
    )
    static class GgnnSubcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
            names = { "--label" },
            description = "Use a specific label for the graph instead of the file name."
        )
        String label;

        @CommandLine.Option(
            names = { "--dotgraph" },
            description = "Generate a dot-graph representation of the GGNN graph."
        )
        boolean dotGraph;

        @Override
        protected GgnnGraphPreprocessor getAnalyzer() {
            final GgnnOutputFormat format;
            if (dotGraph) {
                format = GgnnOutputFormat.DOT_GRAPH;
            }
            else {
                format = GgnnOutputFormat.JSON_GRAPH;
            }

            return new GgnnGraphPreprocessor(getCommonOptions(), format, label);
        }
    }

    @CommandLine.Command(
        name = "llm",
        description = "Transforms each Scratch project into the ScratchBlocks format for an LLM."
    )
    static class LlmSubcommand extends MLPreprocessorSubcommand {

        @Override
        protected MLFilePreprocessor<?> getAnalyzer() {
            return new LlmPreprocessor(getCommonOptions(), getOutputPath());
        }
    }

    @CommandLine.Command(
        name = "tokenizer",
        description = "Transforms each Scratch project into a token sequence."
    )
    static class TokenizerSubcommand extends MLPreprocessorSubcommand {

        private static final String MASKED_BLOCK_ID = "--masked-block-id";
        private static final String MASKED_INPUT_KEY = "--masked-input-key";
        private static final String MASKED_FIXED_NODE_OPTION = "--masked-fixed-node-option";

        @CommandLine.Option(
            names = { "--sequence-per-script" },
            description = "Generate one token sequence per script instead of per sprite/program."
                + "Custom procedure definitions count as scripts."
        )
        boolean sequencePerScript = false;

        @CommandLine.Option(
            names = { "--abstract-fixed-node-options" },
            description = "Replace fixed node options with abstract tokens."
        )
        boolean abstractFixedNodeOption = false;

        @CommandLine.Option(
            names = { "--tokenizing-method" },
            defaultValue = "REGULAR",
            description = "Selects the tokenizing method that should be used (case-sensitive): "
                + "Regular (`REGULAR`), regular with prefix notation for binary expressions (`PREFIX`), "
                + "prefix without parentheses around literals (`PREFIX_FEWER_PARENTHESES`), "
                + "or only statements (`STATEMENT_LEVEL`). Default: REGULAR"
        )
        TokenizingMethod tokenizingMethod;

        @CommandLine.Option(
            names = { MASKED_BLOCK_ID },
            description = "Mask the block with the given ID. Default: no masking."
        )
        String maskedBlockId = null;

        @CommandLine.Option(
            names = { MASKED_INPUT_KEY },
            description = "Mask the input of a block. Valid input keys are the same as in the Scratch JSON. "
                + "Use together with " + MASKED_BLOCK_ID
                + " to specify the block that takes the input. Default: no masking."
        )
        String maskedInputKey = null;

        @CommandLine.Option(
            names = { MASKED_FIXED_NODE_OPTION },
            description = "Mask the fixed node option of the block with the given ID. Default: no masking."
        )
        String maskedFixedNodeOption = null;

        private void throwParameterException(final String... message) {
            throw new CommandLine.ParameterException(
                spec.commandLine(),
                String.join(" ", message)
            );
        }

        private MaskingStrategy getMaskingStrategy() {
            if (maskedBlockId != null) {
                if (maskedFixedNodeOption != null) {
                    throwParameterException(
                        "You cannot use", MASKED_BLOCK_ID, "together with", MASKED_FIXED_NODE_OPTION
                    );
                }

                if (maskedInputKey != null) {
                    return MaskingStrategy.input(maskedBlockId, maskedInputKey);
                }

                return MaskingStrategy.block(maskedBlockId);
            }

            if (maskedFixedNodeOption != null) {
                if (maskedInputKey != null) {
                    throwParameterException(
                        "You cannot use", MASKED_INPUT_KEY, "together with", MASKED_FIXED_NODE_OPTION
                    );
                }

                return MaskingStrategy.fixedOption(maskedFixedNodeOption);
            }

            if (maskedInputKey != null) {
                throwParameterException(
                    "You must also use", MASKED_BLOCK_ID, "to specify the ID of block whose input should be masked"
                );
            }

            return MaskingStrategy.none();
        }

        @Override
        protected TokenizingPreprocessor getAnalyzer() {
            if (wholeProgram.wholeProgram && sequencePerScript) {
                throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "Cannot generate one sequence for the whole program and sequences per script at the same time."
                );
            }

            return new TokenizingPreprocessor(
                getCommonOptions(), sequencePerScript, abstractFixedNodeOption, tokenizingMethod, getMaskingStrategy()
            );
        }
    }
}
