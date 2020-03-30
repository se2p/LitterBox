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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

class MessageNeverSentTest {
    private static Program program;
    private static Program messageRec;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/broadcastSync.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("broadcastSync", objectMapper.readTree(file));
            messageRec = ProgramParser.parseProgram("messageRec", objectMapper.readTree(new File("src/test/fixtures" +
                    "/bugpattern/messageRec.json")));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMessageNeverSent() {
        MessageNeverSent finder = new MessageNeverSent();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(0);
    }

    @Test
    public void testMessageRec() {
        MessageNeverSent finder = new MessageNeverSent();
        final IssueReport check = finder.check(messageRec);
        Truth.assertThat(check.getCount()).isEqualTo(1);
    }
}