package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.*;

public abstract class AbstractQuestionFinder extends AbstractIssueFinder {

    protected final int MAX_CHOICES;

    protected Set<String> choices;
    protected Set<String> answers;

    public AbstractQuestionFinder() {
        MAX_CHOICES = 4;
    }

    @Override
    public Set<Issue> check(Program program) {
        choices = new LinkedHashSet<>();
        answers = new LinkedHashSet<>();
        return super.check(program);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

}
