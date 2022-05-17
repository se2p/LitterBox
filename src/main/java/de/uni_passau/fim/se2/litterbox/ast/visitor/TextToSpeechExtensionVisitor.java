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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;

public interface TextToSpeechExtensionVisitor {

    /**
     * @param node TextToSpeech  Node of which the children will be iterated
     */
    default void visit(TextToSpeechStmt node) {
        visit((TextToSpeechBlock) node);
    }

    /**
     * @param node TextToSpeech  Node of which the children will be iterated
     */
    void visit(TextToSpeechBlock node);

    /**
     * Default implementation of visit method for Language.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Language of which the children will be iterated
     */
    default void visit(Language node) {
        visitParentVisitor(node);
    }

    /**
     * Default implementation of visit method for FixedLanguage.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FixedLanguage of which the children will be iterated
     */
    default void visit(FixedLanguage node) {
        visit((Language) node);
    }

    /**
     * Default implementation of visit method for ExprLanguage.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExprLanguage of which the children will be iterated
     */
    default void visit(ExprLanguage node) {
        visit((Language) node);
    }

    /**
     * Default implementation of visit method for Voice.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Voice of which the children will be iterated
     */
    default void visit(Voice node) {
        visitParentVisitor(node);
    }

    /**
     * Default implementation of visit method for FixedVoice.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FixedVoice of which the children will be iterated
     */
    default void visit(FixedVoice node) {
        visit((Voice) node);
    }

    /**
     * Default implementation of visit method for ExprVoice.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExprVoice of which the children will be iterated
     */
    default void visit(ExprVoice node) {
        visit((Voice) node);
    }

    /**
     * Default implementation of visit method for SetLanguage.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetLanguage of which the children will be iterated
     */
    default void visit(SetLanguage node) {
        visit((TextToSpeechStmt) node);
    }

    /**
     * Default implementation of visit method for SetVoice.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVoice of which the children will be iterated
     */
    default void visit(SetVoice node) {
        visit((TextToSpeechStmt) node);
    }

    /**
     * Default implementation of visit method for SayTextToSpeech.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SayTextToSpeech of which the children will be iterated
     */
    default void visit(Speak node) {
        visitParentVisitor(node);
    }

    void visitParentVisitor(TextToSpeechBlock node);
}
