/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.dataflow;

import de.uni_passau.fim.se2.litterbox.cfg.DataflowFact;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class MustFunctionTest {

    @Test
    public void testIntersection() {
        DataflowFact fact1 = new DataflowFact() {
        };
        DataflowFact fact2 = new DataflowFact() {
        };
        DataflowFact fact3 = new DataflowFact() {
        };

        Set<DataflowFact> set1 = Set.of(fact1, fact2);
        Set<DataflowFact> set2 = Set.of(fact2, fact3);
        Set<Set<DataflowFact>> join = Set.of(set1, set2);

        MustFunction<DataflowFact> mustFunction = new MustFunction<>();
        Set<DataflowFact> result = mustFunction.apply(join);
        assertThat(result).containsExactly(fact2);
    }
}
