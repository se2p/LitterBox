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
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import tools.jackson.databind.json.JsonMapper;

public class TokenizingProgramPreprocessor extends MLProgramPreprocessor<TokenSequence> {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.shared();

    private final BiFunction<Program, ASTNode, List<String>> tokenizeFunction;
    private final boolean sequencePerScript;

    public TokenizingProgramPreprocessor(
        final MLPreprocessorCommonOptions commonOptions, final MaskingStrategy maskingStrategy,
        final boolean abstractFixedNodeOption, final TokenizingMethod tokenizingMethod, final boolean sequencePerScript
    ) {
        super(commonOptions);

        this.sequencePerScript = sequencePerScript;

        tokenizeFunction = switch (tokenizingMethod) {
            case REGULAR -> (program, astNode) -> Tokenizer
                .tokenize(program, astNode, commonOptions.abstractTokens(), abstractFixedNodeOption, maskingStrategy);
            case PREFIX -> (program, astNode) -> PrefixTokenizer
                .tokenize(program, astNode, commonOptions.abstractTokens(), abstractFixedNodeOption, maskingStrategy);
            case PREFIX_FEWER_PARENTHESES -> (program, astNode) -> PrefixTokenizer
                .tokenizeWithReducedParentheses(
                    program, astNode, commonOptions.abstractTokens(), abstractFixedNodeOption, maskingStrategy
                );
            case STATEMENT_LEVEL -> (program, astNode) -> StatementLevelTokenizer
                .tokenize(program, astNode, commonOptions.abstractTokens(), maskingStrategy);
        };
    }

    @Override
    public Stream<TokenSequence> processSprites(Program program) {
        final Stream<ActorDefinition> actors = getActors(program);
        return actors.flatMap(actor -> generateSequenceForActor(program, actor).stream());
    }

    @Override
    public Stream<TokenSequence> processWholeProgram(Program program) {
        final Stream<ActorDefinition> actors = getActors(program);

        final List<String> tokens = actors
            .flatMap(actor -> getTokenSequencesForActor(program, actor))
            .flatMap(List::stream)
            .toList();
        return Stream.of(TokenSequenceBuilder.build(program.getIdent().getName(), List.of(tokens)));
    }

    @Override
    public String resultToString(TokenSequence result) {
        return OBJECT_MAPPER.writeValueAsString(result);
    }

    private Stream<ActorDefinition> getActors(final Program program) {
        if (commonOptions.includeDefaultSprites()) {
            return AstNodeUtil.getActors(program, commonOptions.includeStage());
        }
        else {
            return AstNodeUtil.getActorsWithoutDefaultSprites(program, commonOptions.includeStage());
        }
    }

    private Optional<TokenSequence> generateSequenceForActor(final Program program, final ActorDefinition actor) {
        return commonOptions.actorNameNormalizer().normalizeName(actor).map(label -> {
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
                .map(node -> tokenizeFunction.apply(program, node));
        }
        else {
            return Stream.of(tokenizeFunction.apply(program, actor));
        }
    }
}
