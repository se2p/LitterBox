package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

public class ExprVoice extends AbstractNode implements Language {
    private final Expression expr;
    private final BlockMetadata metadata;

    public ExprVoice(Expression expr, BlockMetadata metadata) {
        super(expr, metadata);
        this.expr = expr;
        this.metadata = metadata;
    }

    public Expression getExpr() {
        return expr;
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
        visitor.visit( this);
    }

    @Override
    public void accept(ExtensionVisitor visitor){
        if (visitor instanceof TextToSpeechExtensionVisitor){
            ((TextToSpeechExtensionVisitor) visitor).visit(this);
        }else{
            visitor.visit(this);
        }
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
