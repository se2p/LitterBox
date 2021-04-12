package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

public interface ExtensionVisitor {

    void addParent(ScratchVisitor scratchVisitor);

    ScratchVisitor getParent();

    /**
     * Default implementation of visit method for ExtensionBlock.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExtensionBlock of which the children will be iterated
     */
    default void visit(ExtensionBlock node) {
        getParent().visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link PenStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenStmt  Node of which the children will be iterated
     */
    default void visit(PenStmt node) {
        getParent().visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for PenDownStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenDownStmt of which the children will be iterated
     */
    default void visit(PenDownStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenUpStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenClearStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenColorToColorStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorToColorStmt  Node of which the children will be iterated
     */
    default void visit(SetPenColorToColorStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PenStampStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenStampStmt  Node of which the children will be iterated
     */
    default void visit(PenStampStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangePenColorParamBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenColorParamBy  Node of which the children will be iterated
     */
    default void visit(ChangePenColorParamBy node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenColorParamTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorParamTo Node of which the children will be iterated
     */
    default void visit(SetPenColorParamTo node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenSizeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenSizeTo Node of which the children will be iterated
     */
    default void visit(SetPenSizeTo node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangePenSizeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenSizeBy Node of which the children will be iterated
     */
    default void visit(ChangePenSizeBy node) {
        visit((PenStmt) node);
    }
}
