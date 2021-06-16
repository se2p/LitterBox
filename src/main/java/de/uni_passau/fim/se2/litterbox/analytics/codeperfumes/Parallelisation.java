package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This checks for starting of two scripts or more on the same event.
 */
public class Parallelisation extends AbstractIssueFinder {
    public static final String NAME = "parallelisation";
    private List<Event> events = new ArrayList<>();

    @Override
    public Set<Issue> check(Program program) {
        events = new ArrayList<>();
        return super.check(program);
    }

    @Override
    public void visit(GreenFlag node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(KeyPressed node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(SpriteClicked node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(StageClicked node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        checkEvents(node);
        events.add(node);
    }


    private void checkEvents(AbstractNode event) {
        for (Event e : events) {
            if (e.equals(event)){
                addIssue(event, event.getMetadata(), IssueSeverity.HIGH);
                break;
            }
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
