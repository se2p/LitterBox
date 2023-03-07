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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GeneratePathTask {

    private final PathGenerator pathGenerator;

    public GeneratePathTask(PathGenerator pathGenerator) {
        this.pathGenerator = Objects.requireNonNull(pathGenerator);
    }

    public List<ProgramFeatures> createContextForCode2Vec() {
        // pathGenerator.printLeafs();
        return pathGenerator.generatePaths();
    }

    public Stream<String> featuresToString(List<ProgramFeatures> features, boolean includeActorName) {
        if (features == null) {
            return Stream.empty();
        }
        if (!includeActorName)
            return features.stream().map(ProgramFeatures::toStringWithoutNodeName);
        else
            return features.stream().map(ProgramFeatures::toString);
    }
}
