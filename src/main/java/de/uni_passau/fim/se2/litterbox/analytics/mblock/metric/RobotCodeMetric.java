package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class RobotCodeMetric extends AbstractRobotMetric<Program> {

    public static final String NAME = "robot_code_finder";
    private boolean robot = false;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return robot ? 1 : 0;
    }

    @Override
    public void visit(ActorDefinition actor) {
        robot = robot || getRobot(actor.getIdent().getName(), actor.getSetStmtList()).isRobot();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
