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
package de.uni_passau.fim.se2.embedded_kittens.llm;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

public class LlmProgramPreprocessor extends MLProgramPreprocessor<LlmPreprocessorOutput> {

    private static final Logger log = Logger.getLogger(LlmProgramPreprocessor.class.getName());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

    protected LlmProgramPreprocessor(MLPreprocessorCommonOptions commonOptions) {
        super(commonOptions);
    }

    @Override
    public Stream<LlmPreprocessorOutput> processSprites(Program program) {
        return AstNodeUtil.getActors(program, commonOptions.includeStage())
            .filter(actor -> commonOptions.includeDefaultSprites() || !NodeNameUtil.hasDefaultName(actor))
            .map(this::convertActor);
    }

    private LlmPreprocessorOutput convertActor(final ActorDefinition node) {
        final String label = node.getIdent().getName();
        final Optional<String> normalisedLabel = commonOptions.actorNameNormalizer().apply(node);
        final String scratchBlocks = ScratchBlocksVisitor.of(node);

        return new LlmPreprocessorOutput(label, normalisedLabel, scratchBlocks);
    }

    @Override
    public Stream<LlmPreprocessorOutput> processWholeProgram(Program program) {
        final String label = program.getIdent().getName();
        final String scratchBlocks = ScratchBlocksVisitor.of(program);

        return Stream.of(new LlmPreprocessorOutput(label, Optional.empty(), scratchBlocks));
    }

    @Override
    public String resultToString(LlmPreprocessorOutput result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        }
        catch (JsonProcessingException ex) {
            // If this breaks: check that Jackson has reflection access to the classes
            log.warning("The output cannot be converted to JSON. Please report this bug to the developers.");
            ex.printStackTrace();
            return null;
        }
    }

}
