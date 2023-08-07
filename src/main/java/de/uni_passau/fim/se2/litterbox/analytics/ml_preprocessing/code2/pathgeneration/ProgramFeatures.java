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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.program_relation.ProgramRelationFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramFeatures {
    private final String name;
    private final ProgramRelationFactory programRelationFactory;

    private final List<ProgramRelation> features = new ArrayList<>();

    public ProgramFeatures(final String name, final ProgramRelationFactory programRelationFactory) {
        this.name = name;
        this.programRelationFactory = programRelationFactory;
    }

    @Override
    public String toString() {
        return name + ' ' + features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" "));
    }

    public String toStringWithoutNodeName() {
        return features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" "));
    }

    public void addFeature(String source, String path, String target) {
        final ProgramRelation newRelation = programRelationFactory.build(source, target, path);
        features.add(newRelation);
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    public String getName() {
        return name;
    }

    public List<ProgramRelation> getFeatures() {
        return features;
    }
}
