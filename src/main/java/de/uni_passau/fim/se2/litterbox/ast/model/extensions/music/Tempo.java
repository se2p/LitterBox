package de.uni_passau.fim.se2.litterbox.ast.model.extensions.music;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.MusicOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class Tempo extends SingularExpression implements MusicExpression {

    public Tempo(BlockMetadata metadata) {
        super(metadata);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((TextToSpeechBlock) this);
    }

    @Override
    public void accept(MusicExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public Opcode getOpcode() {
        return MusicOpcode.music_getTempo;
    }
}
