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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.Drum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.ExprDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.ExprInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.Instrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.ExprNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.Note;

public interface MusicExtensionVisitor extends ScratchVisitor {

    default void visit(MusicStmt node) {
        visit((MusicBlock) node);
    }

    default void visit(MusicExpression node) {
        visit((MusicBlock) node);
    }

    default void visit(Tempo node) {
        visit((MusicExpression) node);
    }

    default void visit(ChangeTempoBy node) {
        visit((MusicStmt) node);
    }

    default void visit(SetTempoTo node) {
        visit((MusicStmt) node);
    }

    default void visit(SetInstrumentTo node) {
        visit((MusicStmt) node);
    }

    default void visit(PlayDrumForBeats node) {
        visit((MusicStmt) node);
    }

    default void visit(PlayNoteForBeats node) {
        visit((MusicStmt) node);
    }

    default void visit(RestForBeats node) {
        visit((MusicStmt) node);
    }

    default void visit(Note node) {
        visit((MusicBlock) node);
    }

    default void visit(FixedNote node) {
        visit((Note) node);
    }

    default void visit(ExprNote node) {
        visit((Note) node);
    }

    default void visit(Instrument node) {
        visit((MusicBlock) node);
    }

    default void visit(FixedInstrument node) {
        visit((Instrument) node);
    }

    default void visit(ExprInstrument node) {
        visit((Instrument) node);
    }

    default void visit(Drum node) {
        visit((MusicBlock) node);
    }

    default void visit(FixedDrum node) {
        visit((Drum) node);
    }

    default void visit(ExprDrum node) {
        visit((Drum) node);
    }
}
