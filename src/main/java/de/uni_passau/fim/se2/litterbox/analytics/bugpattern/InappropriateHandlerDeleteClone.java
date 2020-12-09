package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;

public class InappropriateHandlerDeleteClone extends AbstractIssueFinder {
    private String NAME = "inappropriate_handler_delete_clone";
    private boolean hasDeleteClone;

    @Override
    public void visit(Script node){
        if (node.getEvent() instanceof GreenFlag){
            super.visit(node);
            if (hasDeleteClone){
                addIssue(node.getEvent(),node.getEvent().getMetadata());
            }
            hasDeleteClone=false;
        }
    }

    @Override
    public void visit(DeleteClone node){
        hasDeleteClone=true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
