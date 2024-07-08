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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.extraction.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.List;

public class ProgramExtractionAnalyzer implements ProgramAnalyzer<List<ExtractionResult>> {

    private final List<NameExtraction> extrators = List.of(
            new BackdropNameExtraction(),
            new CostumeNameExtraction(),
            new ListNameExtraction(),
            new SpriteNameExtraction(),
            new VariableNameExtraction()
    );

    @Override
    public List<ExtractionResult> analyze(Program program) {
        return extract(program);
    }

    public List<NameExtraction> getExtractors() {
        return extrators;
    }

    public List<String> getExtractorNames() {
        return extrators.stream().map(NameExtraction::getName).toList();
    }

    private List<ExtractionResult> extract(Program program) {
        List<ExtractionResult> list = new ArrayList<>();
        for (NameExtraction extractor : getExtractors()) {
            list.add(new ExtractionResult(extractor.getName(), extractor.extractNames(program)));
        }
        return list;
    }
}
