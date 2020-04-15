package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class CommentMetadata extends AbstractNode implements Metadata {

    private String blockId;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean minimized;
    private String text;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
