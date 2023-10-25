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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.MLPreprocessingAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TokenizingAnalyzer extends MLPreprocessingAnalyzer<TokenSequence> {
    private final ObjectMapper objectMapper;

    private final boolean sequencePerScript;

    /**
     * An analyzer that flattens the program into a token sequence.
     *
     * @param commonOptions     The common ML preprocessor options.
     * @param sequencePerScript Generate one token sequence per script instead of one per actor.
     */
    public TokenizingAnalyzer(
            final MLPreprocessorCommonOptions commonOptions,
            boolean sequencePerScript
    ) {
        super(commonOptions);

        Preconditions.checkArgument(
                !(commonOptions.wholeProgram() && sequencePerScript),
                "Cannot generate one sequence for the whole program and sequences per script at the same time."
        );

        this.objectMapper = new ObjectMapper();

        this.sequencePerScript = sequencePerScript;
    }

    @Override
    public Stream<TokenSequence> check(final Program program) {
        final Stream<ActorDefinition> actors = getActors(program);

        final Stream<TokenSequence> result;
        if (wholeProgram) {
            final List<String> tokens = actors
                    .flatMap(actor -> getTokenSequencesForActor(program, actor))
                    .flatMap(List::stream)
                    .toList();
            result = Stream.of(TokenSequenceBuilder.build(program.getIdent().getName(), List.of(tokens)));
        } else {
            result = actors.flatMap(actor -> generateSequenceForActor(program, actor).stream());
        }

        return result;
    }

    private Stream<ActorDefinition> getActors(final Program program) {
        if (includeDefaultSprites) {
            return AstNodeUtil.getActors(program, includeStage);
        } else {
            return AstNodeUtil.getActorsWithoutDefaultSprites(program, includeStage);
        }
    }

    @Override
    protected String resultToString(final TokenSequence result) {
        return toJson(result);
    }

    private Optional<TokenSequence> generateSequenceForActor(final Program program, final ActorDefinition actor) {
        return actorNameNormalizer.normalizeName(actor).map(label -> {
            final List<List<String>> tokens = getTokenSequencesForActor(program, actor).toList();
            return TokenSequenceBuilder.build(label, tokens);
        });
    }

    private Stream<List<String>> getTokenSequencesForActor(final Program program, final ActorDefinition actor) {
        if (sequencePerScript) {
            final Stream<ASTNode> scripts = actor.getScripts().getScriptList()
                    .stream().map(ASTNode.class::cast);
            final Stream<ASTNode> procedures = actor.getProcedureDefinitionList()
                    .getList().stream().map(ASTNode.class::cast);

            return Stream.concat(procedures, scripts)
                    .map(node -> Tokenizer.tokenize(program, node, abstractTokens));
        } else {
            return Stream.of(Tokenizer.tokenize(program, actor, abstractTokens));
        }
    }

    // save as JSON list to avoid having to manually deal with escaping whitespace, quotes, … and parsing it again
    private String toJson(final TokenSequence items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Path outputFileName(final File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()) + ".jsonl");
    }
}
