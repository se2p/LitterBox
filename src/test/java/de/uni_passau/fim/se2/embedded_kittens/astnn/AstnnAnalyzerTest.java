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
package de.uni_passau.fim.se2.embedded_kittens.astnn;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.MLOutputPath;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.StatementTreeSequence;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

class AstnnAnalyzerTest {

    @Test
    void testEmptyResultOnInvalidProgram(@TempDir Path outputDir) throws IOException {
        final MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.directory(outputDir),
            true,
            true,
            false,
            ActorNameNormalizer.getDefault()
        );
        final Path programPath = Path.of("src/test/fixtures/astnn_definitely_non_existing.json");
        final AstnnPreprocessor analyzer = new AstnnPreprocessor(commonOptions);

        analyzer.processPerSprite(programPath);

        assertThat(Files.walk(outputDir)).hasSize(1);
    }

    @Test
    void testIgnoreEmptySprites() throws ParsingException {
        // one actor with an empty name, another one with no blocks
        final Stream<StatementTreeSequence> result = processFixture(
            "ml_preprocessing/astnn/empty_actors.json", false, true, false
        );
        assertThat(result.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testFilterSpritesDefaultName(boolean includeDefaultSprites) throws ParsingException {
        final List<StatementTreeSequence> result = processFixture(
            "allBlocks.json", true, includeDefaultSprites, false
        ).toList();

        final List<String> actorsOriginalName = result.stream().map(StatementTreeSequence::originalLabel).toList();
        assertThat(actorsOriginalName).containsAtLeast("Stage", "Ball");

        final List<String> actors = result.stream().map(StatementTreeSequence::label).toList();
        assertThat(actors).containsAtLeast("stage", "ball");

        if (includeDefaultSprites) {
            assertThat(actors).contains("sprite");
            assertThat(actorsOriginalName).contains("Sprite1");
        }
        else {
            assertThat(actors).doesNotContain("sprite");
            assertThat(actorsOriginalName).doesNotContain("Sprite");
        }
    }

    // ToDo: test for empty_string label
    @Test
    void testEmptyStringLabel() throws ParsingException {
        final Program program = new Scratch3Parser()
            .parseFile(Path.of("src/test/fixtures/allBlocks.json").toFile());

        final ActorDefinition actor = new ActorDefinition(
            ActorType.getSprite(),
            new StrId(""),
            new DeclarationStmtList(Collections.emptyList()),
            new SetStmtList(Collections.emptyList()),
            new ProcedureDefinitionList(Collections.emptyList()),
            new ScriptList(Collections.emptyList()),
            new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(Collections.emptyList()),
                new SoundMetadataList(Collections.emptyList())
            )
        );
        final StatementTreeSequence sequence = new StatementTreeSequenceBuilder(
            ActorNameNormalizer.getDefault(), false
        ).build(program, actor);

        assertThat(sequence.label()).isEqualTo("EMPTY_STRING");
        assertThat(sequence.originalLabel()).isEqualTo("EMPTY_STRING");
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testStageIncludedPerSprite(boolean includeStage) throws ParsingException {
        final Stream<StatementTreeSequence> result = processFixture(
            "multipleSprites.json", includeStage, true, false
        );

        int expectedItems = includeStage ? 3 : 2;
        assertThat(result.count()).isEqualTo(expectedItems);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testStageIncludedWholeProgram(boolean includeStage) throws ParsingException {
        final List<StatementTreeSequence> result = processFixture(
            "multipleSprites.json", includeStage, true, true
        ).toList();

        assertThat(result).hasSize(1);

        final StatementTreeSequence sequence = result.getFirst();
        assertThat(sequence.label()).isEqualTo("multipleSprites");
        assertThat(sequence.originalLabel()).isEqualTo(sequence.label());
    }

    @Test
    void testWriteToFile(@TempDir Path outputDir) throws Exception {
        final MLPreprocessorCommonOptions options = new MLPreprocessorCommonOptions(
            MLOutputPath.directory(outputDir),
            false,
            true,
            false,
            ActorNameNormalizer.getDefault()
        );

        final AstnnPreprocessor analyzer = new AstnnPreprocessor(options);
        analyzer.processPerSprite(Path.of("src/test/fixtures/multipleSprites.json"));

        final Path expectedOutputFile = outputDir.resolve("multipleSprites.jsonl");
        assertThat(expectedOutputFile.toFile().exists()).isTrue();

        final List<String> outputContent = Files.readAllLines(expectedOutputFile);
        assertThat(outputContent).hasSize(2);
    }

    private Stream<StatementTreeSequence> processFixture(
        final String program, boolean includeStage, boolean includeDefaultSprites, boolean wholeProgram
    ) throws ParsingException {
        final Path programPath = Path.of("src/test/fixtures").resolve(program);
        final AstnnProgramPreprocessor analyzer = new AstnnProgramPreprocessor(
            options(includeStage, includeDefaultSprites)
        );
        final Program parsedProgram = new Scratch3Parser().parseFile(programPath.toFile());

        if (wholeProgram) {
            return analyzer.processWholeProgram(parsedProgram);
        }
        else {
            return analyzer.processSprites(parsedProgram);
        }
    }

    private MLPreprocessorCommonOptions options(boolean includeStage, boolean includeDefaultSprites) {
        return new MLPreprocessorCommonOptions(
            MLOutputPath.console(),
            includeStage,
            includeDefaultSprites,
            false,
            ActorNameNormalizer.getDefault()
        );
    }
}
