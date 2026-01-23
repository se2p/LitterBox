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
package de.uni_passau.fim.se2.embedded_kittens.shared;

import java.util.List;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import tools.jackson.databind.json.JsonMapper;

public class WholeProgramJsonProcessor<T> extends MLProgramPreprocessor<WholeProgramOutput<T>> {

    private final JsonMapper objectMapper = JsonMapper.shared();
    private final MLProgramPreprocessor<T> preprocessor;

    public WholeProgramJsonProcessor(
        final MLPreprocessorCommonOptions commonOptions,
        final MLProgramPreprocessor<T> programPreprocessor
    ) {
        super(commonOptions);
        this.preprocessor = programPreprocessor;
    }

    @Override
    public String resultToString(final WholeProgramOutput<T> result) {
        return objectMapper.writeValueAsString(result);
    }

    @Override
    public Stream<WholeProgramOutput<T>> processSprites(final Program program) {
        throw new UnsupportedOperationException("Can only process programs as a whole!");
    }

    @Override
    public Stream<WholeProgramOutput<T>> processWholeProgram(final Program program) {
        final List<T> perSpriteOutputs = preprocessor.processSprites(program).toList();
        return Stream.of(new WholeProgramOutput<>(program.getIdent().getName(), perSpriteOutputs));
    }
}
