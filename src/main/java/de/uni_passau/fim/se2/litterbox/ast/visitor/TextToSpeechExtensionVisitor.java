package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;

public interface TextToSpeechExtensionVisitor extends ExtensionVisitor {

    /**
     * @param node TextToSpeech  Node of which the children will be iterated
     */
    void visit(TextToSpeechStmt node);

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
        visit((TextToSpeechBlock) node);
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
        visit((Voice) node);
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
        visit((Voice) node);
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
        visit((TextToSpeechBlock) node);
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
    default void visit(SayTextToSpeech node) {
        visit((TextToSpeechStmt) node);
    }
}
