package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.HideWithoutShow;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injects HideWithoutShow bug by removing Show statements.
 * Only injects when the actor has:
 * - A Hide statement
 * - A Show statement (which we will remove)
 * - Visible change blocks (SpriteMotionStmt, SpriteLookStmt, or ActorLookStmt)
 */
public class HideWithoutShowInjector extends AbstractBugInjector {

    private boolean hasHide;
    private boolean hasVisibleChange;

    @Override
    public IssueFinder getIssueFinder() {
        return new HideWithoutShow();
    }

    @Override
    public void visit(ActorDefinition node) {
        // Reset state for each actor
        hasHide = false;
        hasVisibleChange = false;

        // First pass: check if actor has hide and visible changes
        checkActorPrerequisites(node);

        // Only proceed with injection if prerequisites are met
        if (hasHide && hasVisibleChange) {
            super.visit(node);
        }
    }

    private void checkActorPrerequisites(ActorDefinition node) {
        node.accept(new ScratchVisitor() {
            @Override
            public void visit(Hide hide) {
                hasHide = true;
            }

            @Override
            public void visit(SpriteMotionStmt stmt) {
                hasVisibleChange = true;
            }

            @Override
            public void visit(SpriteLookStmt stmt) {
                hasVisibleChange = true;
            }

            @Override
            public void visit(ActorLookStmt stmt) {
                hasVisibleChange = true;
            }
        });
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof Show) {
                final int index = i;
                addMutation(node, clonedList -> {
                    // Remove this specific Show statement
                    clonedList.getStmts().remove(index);
                });
            }
        }
        super.visit(node);
    }
}
