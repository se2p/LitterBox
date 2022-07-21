package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.MBlockEvent;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.MBlockExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.CODEY;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class RealCodeyMetric implements MetricExtractor<Program>, MBlockVisitor {

    public static final String NAME = "real_codey_program";
    private boolean robot = false;
    private boolean realRobot = false;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return realRobot ? 1 : 0;
    }

    @Override
    public void visit(ActorDefinition actor) {
        robot = robot || getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == CODEY;
        if (robot) {
            visit((ASTNode) actor);
        }
        robot = false;
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        visitChildren(script);
    }

    @Override
    public void visit(Event event) {
        if (robot && event instanceof MBlockEvent) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) event);
    }

    @Override
    public void visit(Stmt stmt) {
        if (robot && stmt instanceof MBlockStmt) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) stmt);
    }

    @Override
    public void visit(Expression stmt) {
        if (robot && stmt instanceof MBlockExpr) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) stmt);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(MBlockNode node) {
        node.accept(this);
    }
}
