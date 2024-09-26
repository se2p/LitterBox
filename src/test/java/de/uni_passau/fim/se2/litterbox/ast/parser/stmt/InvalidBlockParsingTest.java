/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertThrows;

// do not run parallel to other tests since we mutate the global property
@Isolated
public class InvalidBlockParsingTest implements JsonTest {

    private static final String SKIP_BROKEN_BLOCKS_PROP = "parser.skip_broken_blocks";

    private static boolean skipUnknown;

    @BeforeAll
    static void setUp() {
        skipUnknown = PropertyLoader.getSystemBooleanProperty(SKIP_BROKEN_BLOCKS_PROP);
        System.setProperty(SKIP_BROKEN_BLOCKS_PROP, "false");
    }

    @AfterAll
    static void tearDown() {
        System.setProperty(SKIP_BROKEN_BLOCKS_PROP, Boolean.toString(skipUnknown));
    }

    @Test
    public void testInvalidRotationStyle() {
        assertThrows(
                ParsingException.class,
                () -> getAST("./src/test/fixtures/stmtParser/invalidRotationStyle.json")
        );
    }
}
