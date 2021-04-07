package de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetLanguage extends AbstractNode implements TextToSpeechStmt {
    private final Language language;
    private final BlockMetadata metadata;

    public SetLanguage(Language language, BlockMetadata metadata) {
        super(language, metadata);
        this.language = language;
        this.metadata = metadata;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
