/*
 * Copyright (C) 2019 LitterBox contributors
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
package newanalytics.bugpattern;

import static junit.framework.TestCase.fail;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import newanalytics.IssueReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

class ComparingLiteralsTest {

    private static Program program;
    private static Program emptyFields;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/comparingLiterals.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("comparing literals", objectMapper.readTree(file));
            file = new File("./src/test/fixtures/bugpattern/twoNotColo.json");
            emptyFields =ProgramParser.parseProgram("comparing empty literals", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        ComparingLiterals finder = new ComparingLiterals();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(12);
        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Sprite1");
    }

    @Test
    public void testEmptyCompare() {
        ComparingLiterals finder = new ComparingLiterals();
        final IssueReport check = finder.check(emptyFields);
        Truth.assertThat(check.getCount()).isEqualTo(1);
    }
}