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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.PlayNoteForBeats;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

class MusicBlockParserTest implements JsonTest {
    @Test
    void testParseMusicBlocks() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/stmtParser/manipulatedFixedNoteBlock.json");
        assertNoUnspecifiedBlocks(program);

        final var playNoteForBeats = NodeFilteringVisitor.getBlocks(program, PlayNoteForBeats.class);
        assertThat(playNoteForBeats).hasSize(1);

        final var fixedNote = NodeFilteringVisitor.getBlocks(program, FixedNote.class);
        assertThat(fixedNote).hasSize(1);
    }
}
