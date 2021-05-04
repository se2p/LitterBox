package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

public interface TextToSpeechBlock extends ExtensionBlock {

    void accept(TextToSpeechExtensionVisitor visitor);
}
