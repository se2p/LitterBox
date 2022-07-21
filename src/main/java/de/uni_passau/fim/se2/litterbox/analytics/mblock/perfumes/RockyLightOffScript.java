package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLightOff;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class RockyLightOffScript extends AbstractRobotFinder {
    private static final String NAME = "rocky_light_off_script";
    private boolean turnsOnRockyLight;
    private boolean turnsOffRockyLight;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnRockyLight = false;
        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffRockyLight = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().get(node.getStmtList().getStmts().size() - 1);
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never) && turnsOnRockyLight && turnsOffRockyLight) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(RockyLight node) {
        turnsOnRockyLight = true;
    }

    @Override
    public void visit(RockyLightOff node) {
        if (secondRun) {
            turnsOffRockyLight = true;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
