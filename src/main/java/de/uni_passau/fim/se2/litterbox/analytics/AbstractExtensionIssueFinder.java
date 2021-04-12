package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class AbstractExtensionIssueFinder extends AbstractIssueFinder implements ExtensionVisitor {
    ScratchVisitor parent = null;

    @Override
    public abstract IssueType getIssueType();

    @Override
    public abstract String getName();

    @Override
    public void addParent(ScratchVisitor scratchVisitor) {
        parent = scratchVisitor;
    }

    @Override
    public ScratchVisitor getParent() {
        return parent;
    }

    @Override
    public void visit(ExtensionBlock node) {
        //TODO what to put here?
        visitChildren(node);
    }
}
