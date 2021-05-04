package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedVoice extends AbstractNode implements Language, ASTLeaf {
    private final BlockMetadata metadata;
    private FixedVoiceType type;

    public FixedVoice(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedVoiceType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedVoiceType getType() {
        return type;
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

    public enum FixedVoiceType {

        ALTO("ALTO"), TENOR("TENOR"), SQUEAK("SQUEAK"), GIANT("GIANT"), KITTEN("KITTEN");

        private final String type;

        FixedVoiceType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedVoiceType fromString(String type) {
            for (FixedVoiceType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedLanguage: " + type);
        }

        public String getType() {
            return type;
        }
    }
}
