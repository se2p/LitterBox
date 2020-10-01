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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MissingCloneInitializationTest implements JsonTest {

    @Test
    public void testCloneInit() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingCloneInitialization.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        // TODO: Restore check
        // Truth.assertThat(check.getPosition().get(0)).isEqualTo("Anina Dance");
    }

    @Test
    public void testCloningWithClicked() throws IOException, ParsingException {
        Program clicked = getAST("src/test/fixtures/bugpattern/cloningWithClicked.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(clicked);
        Truth.assertThat(reports).isEmpty();
    }
}
