package de.uni_passau.fim.se2.litterbox.ast.model.expression;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class TertiaryExpression<A extends ASTNode, B extends ASTNode, C extends ASTNode> extends AbstractNode {

    private final A operand1;
    private final B operand2;
    private final C operand3;
    private final BlockMetadata metadata;

    protected TertiaryExpression(A operand1, B operand2, C operand3, BlockMetadata metadata) {
        super(operand1, operand2, operand3, metadata);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
        this.metadata = metadata;
    }

    public A getOperand1() {
        return operand1;
    }

    public B getOperand2() {
        return operand2;
    }

    public C getOperand3() {
        return operand3;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
