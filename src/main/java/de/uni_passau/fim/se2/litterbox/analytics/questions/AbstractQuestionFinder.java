package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.*;

public abstract class AbstractQuestionFinder extends AbstractIssueFinder {

    protected final int MAX_CHOICES;
    protected final String YES = IssueTranslator.getInstance().getInfo("yes");
    protected final String NO = IssueTranslator.getInstance().getInfo("no");

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
            return String.join("|", list.subList(0, MAX_CHOICES));
        }
        else {
            return String.join("|", list);
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
            return String.join("|", list.subList(0, MAX_CHOICES));
        }
        else {
            return String.join("|", list);
        }
    }

    protected String wrappedScratchBlocks(Script node) {
        return "[scratchblocks]\n" + node.getScratchBlocks() + "[/scratchblocks]";
    }

    protected String wrappedScratchBlocks(StmtList node) {
        return "[scratchblocks]\n" + node.getScratchBlocks() + "[/scratchblocks]";
    }

    protected String wrappedScratchBlocks(Stmt node) {
        return "[sbi]" + node.getScratchBlocksWithoutNewline() + "[/sbi]";
    }

    protected String wrappedScratchBlocks(Expression node) {
        return "[sbi]" + node.getScratchBlocksWithoutNewline() + "[/sbi]";
    }

    protected String wrappedScratchBlocks(NumberLiteral node) {
        return "[sbi]<" + node.getScratchBlocks() + " :: grey ring>[/sbi]";
    }

    protected String wrappedScratchBlocks(StringLiteral node) {
        if (inLookStmt) {
            return "[sbi]<" + node.getScratchBlocks() + " :: look ring>[/sbi]";
        }
        else if (inSoundStmt) {
            return "[sbi]<" + node.getScratchBlocks() + " :: sound ring>[/sbi]";
        }
        else if (inBroadcastStmt) {
            return "[sbi]<" + node.getScratchBlocks() + " :: control ring>[/sbi]";
        }
        else {
            return "[sbi]<" + node.getScratchBlocks() + " :: grey ring>[/sbi]";
        }
    }

    protected String wrappedScratchBlocks(ColorLiteral node) {
        return "[sbi]<" + node.getScratchBlocks() + " :: grey ring>[/sbi]";
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        return first.getActor() == other.getActor();
    }
}
