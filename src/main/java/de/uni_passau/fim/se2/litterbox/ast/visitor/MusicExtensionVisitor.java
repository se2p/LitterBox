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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;

public interface MusicExtensionVisitor {

    /**
     * @param node Music Node of which the children will be iterated
     */
    void visit(MusicBlock node);

    default void visit(MusicStmt node) {
        visit((MusicBlock) node);
    }

    default void visit(MusicExpression node) {
        visit((MusicBlock) node);
    }

    default void visit(Tempo node) {
        visit((MusicExpression) node);
    }

    default void visit(ChangeTempo node) {
        visit((MusicStmt) node);
    }

    default void visit(SetTempoTo node) {
        visit((MusicStmt) node);
    }

    default void visit(SetInstrument node) {
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

    void visitParentVisitor(MusicBlock node);
}
