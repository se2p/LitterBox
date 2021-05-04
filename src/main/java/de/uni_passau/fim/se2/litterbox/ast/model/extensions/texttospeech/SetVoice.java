package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

public class SetVoice extends AbstractNode implements TextToSpeechStmt {
    private final Voice voice;
    private final BlockMetadata metadata;

    public SetVoice(Voice voice, BlockMetadata metadata) {
        super(voice, metadata);
        this.voice = voice;
        this.metadata = metadata;
    }

    public Voice getVoice() {
        return voice;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((ExtensionBlock) this);
    }

    @Override
    public void accept(TextToSpeechExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(ExtensionVisitor visitor) {
        if (visitor instanceof TextToSpeechExtensionVisitor) {
            ((TextToSpeechExtensionVisitor) visitor).visit(this);
        } else {
            visitor.visit(this);
        }
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
