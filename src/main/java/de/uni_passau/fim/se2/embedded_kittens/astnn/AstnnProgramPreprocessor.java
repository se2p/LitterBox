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
package de.uni_passau.fim.se2.embedded_kittens.astnn;

import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.StatementTreeSequence;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import tools.jackson.databind.json.JsonMapper;

public class AstnnProgramPreprocessor extends MLProgramPreprocessor<StatementTreeSequence> {

    private final JsonMapper objectMapper;
    private final StatementTreeSequenceBuilder statementTreeSequenceBuilder;

    public AstnnProgramPreprocessor(final MLPreprocessorCommonOptions commonOptions) {
        super(commonOptions);

        this.objectMapper = JsonMapper.shared();
        statementTreeSequenceBuilder = new StatementTreeSequenceBuilder(
            commonOptions.actorNameNormalizer(), commonOptions.abstractTokens()
        );
    }

    @Override
    public Stream<StatementTreeSequence> processWholeProgram(final Program program) {
        final Stream<StatementTreeSequence> nodes = Stream.of(
            statementTreeSequenceBuilder
                .build(program, commonOptions.includeStage(), commonOptions.includeDefaultSprites())
        );
        return nodes.filter(this::isValidStatementSequence);
    }

    @Override
    public Stream<StatementTreeSequence> processSprites(final Program program) {
        final Stream<StatementTreeSequence> nodes = statementTreeSequenceBuilder
            .buildPerActor(program, commonOptions.includeStage(), commonOptions.includeDefaultSprites());
        return nodes.filter(this::isValidStatementSequence);
    }

    /**
     * We are not interested in trees that either have got no label, or are empty.
     *
     * @param sequence A statement tree sequence for a sprite or program.
     * @return If the sequence is usable for the ML task.
     */
    private boolean isValidStatementSequence(final StatementTreeSequence sequence) {
        final boolean hasEmptyName = sequence.label().isBlank();
        // the actor definition might be the top-most "statement", so we check for actual blocks inside the actor, too
        final boolean hasNoStatements = sequence.statements().isEmpty()
            || sequence.statements().stream().allMatch(AstnnNode::isLeaf);

        return !hasEmptyName && !hasNoStatements;
    }

    @Override
    public String resultToString(StatementTreeSequence result) {
        return sequenceToString(result);
    }

    private String sequenceToString(final StatementTreeSequence sequence) {
        return objectMapper.writeValueAsString(sequence);
    }
}
