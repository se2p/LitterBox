package de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech.language;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.texttospeech.TextToSpeechStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ExprLanguage extends AbstractNode implements TextToSpeechStmt {
    private final Expression expr;
    private final BlockMetadata metadata;

    public ExprLanguage(Expression expr, BlockMetadata metadata) {
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
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
