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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.tokenizer;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizingAnalyzerTest implements JsonTest {

    private final List<TokenSequence> concreteScriptSequences = List.of(
            TokenSequenceBuilder.build(
                    "stage",
                    List.of(List.of("BEGIN_SCRIPT", "event_whenflagclicked", "control_repeat", "10", "END_SCRIPT"))
            ),
            TokenSequenceBuilder.build(
                    "cat",
                    List.of(
                            List.of("BEGIN_SCRIPT", "event_whenkeypressed", "key", "39", "looks_say", "hi_!",
                                    "looks_show", "END_SCRIPT")
                    )
            ),
            TokenSequenceBuilder.build(
                    "abby",
                    List.of(List.of("BEGIN_SCRIPT", "event_whenflagclicked", "looks_say", "hello_!", "END_SCRIPT"))
            )
    );

    private final List<TokenSequence> concreteSpriteSequences = List.of(
            TokenSequenceBuilder.build(
                    "stage",
                    List.of(
                            List.of("BEGIN", "BEGIN_SCRIPT", "event_whenflagclicked", "control_repeat", "10",
                                    "END_SCRIPT", "END")
                    )
            ),
            new TokenSequence(
                    "cat",
                    List.of("cat"),
                    List.of(
                            List.of("BEGIN", "BEGIN_SCRIPT", "event_whenkeypressed", "key", "39", "looks_say", "hi_!",
                                    "looks_show", "END_SCRIPT", "END")
                    ),
                    List.of(
                            List.of("begin", "begin", "script", "event", "whenkeypressed", "key", "39", "looks", "say",
                                    "hi", "!", "looks", "show", "end", "script", "end")
                    )
            ),
            TokenSequenceBuilder.build(
                    "abby",
                    List.of(
                            List.of("BEGIN", "BEGIN_SCRIPT", "event_whenflagclicked", "looks_say", "hello_!",
                                    "END_SCRIPT", "END")
                    )
            )
    );

    private final List<TokenSequence> abstractScriptSequences = List.of(
            new TokenSequence(
                    "stage",
                    List.of("stage"),
                    List.of(
                            List.of("BEGIN_SCRIPT", "event_whenflagclicked", "control_repeat", "LITERAL_NUMBER",
                                    "END_SCRIPT")
                    ),
                    List.of(
                            List.of("begin", "script", "event", "whenflagclicked", "control", "repeat", "literal",
                                    "number", "end", "script")
                    )
            ),
            TokenSequenceBuilder.build(
                    "cat",
                    List.of(
                            List.of("BEGIN_SCRIPT", "event_whenkeypressed", "key", "looks_say", "LITERAL_STRING",
                                    "looks_show", "END_SCRIPT")
                    )
            ),
            TokenSequenceBuilder.build(
                    "abby",
                    List.of(
                            List.of("BEGIN_SCRIPT", "event_whenflagclicked", "looks_say", "LITERAL_STRING",
                                    "END_SCRIPT")
                    )
            )
    );

    @Test
    void testNoCrashOnUnparseableProgam() {
        final TokenizingAnalyzer analyzer = getAnalyzer(true, true, false, false);
        assertEquals(0, analyzer.check(inputFile("unparseable.json")).count());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testWholeProgramSingleSequence(boolean includeStage) {
        final TokenizingAnalyzer analyzer = getAnalyzer(includeStage, true, false, false);

        final var output = analyzer.check(inputFile("multipleSprites.json")).collect(Collectors.toList());
        assertThat(output).hasSize(1);
        assertThat(output.get(0).label()).isEqualTo("multipleSprites");

        final List<String> tokens = output.get(0).tokens().get(0);

        int expectedSize = includeStage ? 24 : 17;
        assertThat(tokens).hasSize(expectedSize);

        int expectedSpriteCount = includeStage ? 3 : 2;
        assertThat(tokens.stream().filter("BEGIN"::equals).count()).isEqualTo(expectedSpriteCount);

        assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(1).tokens().get(0));
        assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(2).tokens().get(0));
        if (includeStage) {
            assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(0).tokens().get(0));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSequencePerSprite(boolean includeStage) {
        final TokenizingAnalyzer analyzer = getAnalyzer(includeStage, false, false, false);

        final var output = analyzer.check(inputFile("multipleSprites.json")).collect(Collectors.toList());

        int expectedSpriteCount = includeStage ? 3 : 2;
        assertThat(output).hasSize(expectedSpriteCount);

        if (includeStage) {
            assertThat(output).containsExactlyElementsIn(concreteSpriteSequences);
        } else {
            assertThat(output).containsExactly(concreteSpriteSequences.get(1), concreteSpriteSequences.get(2));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSequencePerScript(boolean abstractTokens) {
        final TokenizingAnalyzer analyzer = getAnalyzer(true, false, abstractTokens, true);

        final var output = analyzer.check(inputFile("multipleSprites.json")).collect(Collectors.toList());

        if (abstractTokens) {
            assertEquals(abstractScriptSequences, output);
        } else {
            assertEquals(concreteScriptSequences, output);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSequencePerProcedureDefinition(boolean abstractTokens) {
        final TokenizingAnalyzer analyzer = getAnalyzer(true, false, abstractTokens, true);

        final var output = analyzer.check(inputFile("customBlocks.json")).collect(Collectors.toList());
        assertThat(output).hasSize(2);

        final List<TokenSequence> expectedOutput;
        if (abstractTokens) {
            expectedOutput = List.of(
                    TokenSequenceBuilder.build("stage", Collections.emptyList()),
                    TokenSequenceBuilder.build("sprite", List.of(
                            List.of("BEGIN_PROCEDURE", "PROCEDURE_DEFINITION", "looks_say", "LITERAL_STRING",
                                    "motion_movesteps", "LITERAL_NUMBER", "control_stop", "END_PROCEDURE"),
                            List.of("BEGIN_PROCEDURE", "PROCEDURE_DEFINITION", "motion_movesteps", "PARAMETER",
                                    "control_if", "PARAMETER", "motion_movesteps", "LITERAL_NUMBER", "control_stop",
                                    "END_PROCEDURE"))
                    )
            );
        } else {
            expectedOutput = List.of(
                    TokenSequenceBuilder.build("stage", Collections.emptyList()),
                    TokenSequenceBuilder.build("sprite", List.of(
                            List.of("BEGIN_PROCEDURE", "block_no_inputs", "looks_say", "hello_!", "motion_movesteps",
                                    "10", "control_stop", "this_script", "END_PROCEDURE"),
                            List.of("BEGIN_PROCEDURE", "block_with_inputs", "motion_movesteps", "num_input", "control_if",
                                    "boolean", "motion_movesteps", "10", "control_stop", "this_script",
                                    "END_PROCEDURE"))
                    )
            );
        }

        assertThat(output).containsExactlyElementsIn(expectedOutput);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testCustomProcedureCall(boolean abstractTokens) {
        final TokenizingAnalyzer analyzer = getAnalyzer(false, false, abstractTokens, true);

        final File inputFile = inputFile("ml_preprocessing/tokenizer/custom_procedure_call.json");
        final List<TokenSequence> output = analyzer.check(inputFile).collect(Collectors.toList());

        final TokenSequence expectedOutput;
        if (abstractTokens) {
            expectedOutput = TokenSequenceBuilder.build("sprite", List.of(
                    List.of("BEGIN_PROCEDURE", "PROCEDURE_DEFINITION", "control_wait", "LITERAL_NUMBER",
                            "END_PROCEDURE"),
                    List.of("BEGIN_SCRIPT", "event_whenflagclicked", "CUSTOM_BLOCK", "sound_volume",
                            "sensing_mousedown", "sensing_mousey", "END_SCRIPT")
            ));
        } else {
            expectedOutput = TokenSequenceBuilder.build("sprite", List.of(
                    List.of("BEGIN_PROCEDURE", "my_proc_defn", "control_wait", "1", "END_PROCEDURE"),
                    List.of("BEGIN_SCRIPT", "event_whenflagclicked", "my_proc_defn", "sound_volume",
                            "sensing_mousedown", "sensing_mousey", "END_SCRIPT")
            ));
        }

        assertThat(output).containsExactly(expectedOutput);
    }

    @Test
    void testTransformPenBlocks() {
        final TokenizingAnalyzer analyzer = getAnalyzer(false, true, true, false);

        final File inputFile = inputFile("ml_preprocessing/shared/pen_blocks.json");
        final var output = analyzer.check(inputFile).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(27);
        assertThat(output.get(0).tokens().get(0)).containsAtLeast(
                "pen_clear", "pen_stamp", "pen_pendown", "pen_penup", "pen_setpencolortocolor", "LITERAL_COLOR",
                "pen_changecolorby", "pen_setcolorto", "pen_changepensizeby", "pen_setpensizeto"
        );
    }

    @Test
    void testTransformTtsBlocks() {
        final TokenizingAnalyzer analyzer = getAnalyzer(false, true, true, false);

        final File inputFile = inputFile("ml_preprocessing/shared/tts_blocks.json");
        final var output = analyzer.check(inputFile).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens()).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(13);
        assertThat(output.get(0).tokens().get(0))
                .containsAtLeast("tts_speak", "tts_setvoice", "tts_voice", "tts_setlanguage", "tts_language");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testTransformMusicBlocks(boolean abstractTokens) {
        final TokenizingAnalyzer analyzer = getAnalyzer(false, true, abstractTokens, false);

        final File inputFile = inputFile("ml_preprocessing/shared/music_blocks.json");
        final var output = analyzer.check(inputFile).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens()).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(22);
        assertThat(output.get(0).tokens().get(0))
                .containsAtLeast("music_playdrumforbeats", "music_restforbeats", "music_playnoteforbeats",
                        "music_playnoteforbeats", "music_setinstrumentto", "music_settempoto", "music_tempo",
                        "music_changetempoby");

        if (abstractTokens) {
            assertThat(output.get(0).tokens().get(0))
                    .containsAtLeast("music_noteliteral", "music_drumliteral", "music_instrumentliteral");
        } else {
            assertThat(output.get(0).tokens().get(0))
                    .containsAtLeast("BASS_DRUM", "0.25", "60", "my_variable", "ORGAN");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json"
    })
    void testAllBlocksNoSpaces(final String filename) throws Exception {
        final Program program = getAST(filename);
        final NoSpacesChecker noSpacesChecker = new NoSpacesChecker();
        program.accept(noSpacesChecker);
    }

    static class NoSpacesChecker implements ScratchVisitor {
        @Override
        public void visit(ASTNode node) {
            final String token = TokenVisitorFactory.getNormalisedToken(node);
            assertThat(token).doesNotContain(" ");

            visitChildren(node);
        }
    }

    private File inputFile(final String fixture) {
        return Path.of("src", "test", "fixtures").resolve(fixture).toFile();
    }

    private TokenizingAnalyzer getAnalyzer(
            boolean includeStage,
            boolean wholeProgram,
            boolean abstractTokens,
            boolean sequencePerScript
    ) {
        final MLPreprocessorCommonOptions common = new MLPreprocessorCommonOptions(
                Path.of(""),
                MLOutputPath.console(),
                false,
                includeStage,
                wholeProgram,
                true,
                abstractTokens,
                ActorNameNormalizer.getDefault()
        );
        return new TokenizingAnalyzer(common, sequencePerScript, abstractTokens, false,
                MaskingStrategy.none());
    }
}
