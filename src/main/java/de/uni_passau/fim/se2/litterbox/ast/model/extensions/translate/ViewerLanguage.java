package de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;

public class ViewerLanguage extends SingularExpression implements StringExpr, TranslateExpression {

    public ViewerLanguage(BlockMetadata metadata) {
        super(metadata);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((TranslateBlock) this);
    }

    @Override
    public void accept(TranslateExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public Opcode getOpcode() {
        return StringExprOpcode.translate_getViewerLanguage;
    }
}
