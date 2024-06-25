package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ScratchblocksBaseVisitor;
import de.uni_passau.fim.se2.litterbox.ScratchblocksParser;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

public class ScratchBlocksToScratchVisitor extends ScratchblocksBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitMotionStmt(ScratchblocksParser.MotionStmtContext ctx) {
        return super.visitMotionStmt(ctx);
    }
}
