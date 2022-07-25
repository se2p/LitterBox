package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.CODEY;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class CodeyCounterMetric extends AbstractRobotMetric<Program> {

    public static final String NAME = "robot_codey_counter";
    private int codeyCounter = 0;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return codeyCounter;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == CODEY) {
            codeyCounter++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
