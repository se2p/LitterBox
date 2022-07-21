package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class MCoreCounterMetric implements MetricExtractor<Program>, MBlockVisitor {

    public static final String NAME = "robot_mcore_counter";
    private int mcoreCounter = 0;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return mcoreCounter;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == MCORE) {
            mcoreCounter++;
        }
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
