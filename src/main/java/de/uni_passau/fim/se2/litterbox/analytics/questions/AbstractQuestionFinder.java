package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.*;

public abstract class AbstractQuestionFinder extends AbstractIssueFinder {

    protected final int MAX_CHOICES;

    protected Set<String> choices;
    protected Set<String> answers;

    protected boolean inLookStmt;
    protected boolean inSoundStmt;
    protected boolean inBroadcastStmt;

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

    /**
     * If there are more than {@code MAX_CHOICES} elements in {@code choices},
     * choose {@code MAX_CHOICES} elements randomly. Otherwise, return all elements.
     *
     * @return a list of at most {@code MAX_CHOICES} elements from {@code choices}
     */
    protected String getChoices() {
        ArrayList<String> list = new ArrayList<>(choices);
        if (choices.size() > MAX_CHOICES) {
            Collections.shuffle(list);
            return list.subList(0, MAX_CHOICES).toString();
        }
        else {
            return list.toString();
        }
    }

    /**
     * If there are more than {@code MAX_CHOICES} elements in {@code answers},
     * choose {@code MAX_CHOICES} elements randomly. Otherwise, return all elements.
     *
     * @return a list of at most {@code MAX_CHOICES} elements from {@code answers}
     */
    protected String getAnswers() {
        ArrayList<String> list = new ArrayList<>(answers);
        if (answers.size() > MAX_CHOICES) {
            Collections.shuffle(list);
            return list.subList(0, MAX_CHOICES).toString();
        }
        else {
            return list.toString();
        }
    }

    protected String wrappedScratchBlocks(Stmt node) {
        return "[sbi]\n" + node.getScratchBlocks() + "[/sbi]";
    }

    protected String wrappedScratchBlocks(Expression node) {
        return "[sbi]" + node.getScratchBlocks() + "[/sbi]";
    }

    protected String wrappedScratchBlocks(NumberLiteral node) {
        return "<" + node.getScratchBlocks() + " :: grey ring>";
    }

    protected String wrappedScratchBlocks(StringLiteral node) {
        if (inLookStmt) {
            return "<" + node.getScratchBlocks() + " :: look ring>";
        }
        else if (inSoundStmt) {
            return "<" + node.getScratchBlocks() + " :: sound ring>";
        }
        else if (inBroadcastStmt) {
            return "<" + node.getScratchBlocks() + " :: control ring>";
        }
        else {
            return "<" + node.getScratchBlocks() + " :: grey ring>";
        }
    }

    protected String wrappedScratchBlocks(ColorLiteral node) {
        return "<" + node.getScratchBlocks() + " :: grey ring>";
    }
}
