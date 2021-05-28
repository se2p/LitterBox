package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SeparateScriptBySemantics;

public class SemanticScriptFinder extends AbstractRefactoringFinder implements PenExtensionVisitor {

    private static final String NAME = "separate_script_by_semantics";
    private final boolean[] differentSemantics = new boolean[5];

    @Override
    public void visit(Script node) {
        visitChildren(node);

        int count = 0;
        for (boolean differentSemantic : differentSemantics) {
            if (differentSemantic) {
                ++count;
            }
        }

        if (count > 1) {
            refactorings.add(new SeparateScriptBySemantics(node));
        }
    }

    @Override
    public void visit(ActorLookStmt actorLookStmt) {
        differentSemantics[0] = true;
    }

    @Override
    public void visit(ActorSoundStmt actorSoundStmt) {
        differentSemantics[1] = true;
    }

    @Override
    public void visit(PenStmt penStmt) {
        differentSemantics[2] = true;
    }

    @Override
    public void visitParentVisitor(PenStmt node){
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(SpriteLookStmt spriteLookStmt) {
        differentSemantics[3] = true;
    }

    @Override
    public void visit(SpriteMotionStmt spriteMotionStmt) {
        differentSemantics[4] = true;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
