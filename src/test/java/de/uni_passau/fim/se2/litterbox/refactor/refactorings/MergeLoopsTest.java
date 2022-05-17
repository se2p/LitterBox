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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeLoopsFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MergeLoopsTest implements JsonTest {

    @Test
    public void testMergeLoopsFinder_Bidirectional() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeLoops.json");
        MergeLoopsFinder finder = new MergeLoopsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(2);
        assertThat(refactorings.get(0)).isInstanceOf(MergeLoops.class);
        assertThat(refactorings.get(1)).isInstanceOf(MergeLoops.class);
    }

    @Test
    public void testMergeLoopsFinder_Dependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeLoopsDependencies.json");
        MergeLoopsFinder finder = new MergeLoopsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testMergeLoopsFinder_Oneway() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unmergeableLoops.json");
        MergeLoopsFinder finder = new MergeLoopsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

}
