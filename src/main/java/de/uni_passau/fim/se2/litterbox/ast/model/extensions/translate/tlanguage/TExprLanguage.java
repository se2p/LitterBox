package de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;

public class TExprLanguage extends AbstractNode implements TLanguage {
    private final Expression expr;
    private final BlockMetadata metadata;

    public TExprLanguage(Expression expr, BlockMetadata metadata) {
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
    public void accept(TranslateExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
