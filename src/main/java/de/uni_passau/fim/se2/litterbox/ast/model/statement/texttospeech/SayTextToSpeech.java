package de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SayTextToSpeech extends AbstractNode implements TextToSpeechStmt {
    private final StringExpr text;
    private final BlockMetadata metadata;

    public SayTextToSpeech(StringExpr text, BlockMetadata metadata) {
        super(text, metadata);
        this.text = text;
        this.metadata = metadata;
    }

    public StringExpr getText() {
        return text;
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
