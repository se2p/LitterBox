/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractQuestionFinder extends AbstractIssueFinder {

    protected final int maxChoices;
    protected static final String YES = IssueTranslator.getInstance().getInfo("yes");
    protected static final String NO = IssueTranslator.getInstance().getInfo("no");

    protected Set<String> choices;
    protected Set<String> answers;

    protected boolean inLookStmt;
    protected boolean inSoundStmt;
    protected boolean inBroadcastStmt;

    protected AbstractQuestionFinder() {
        maxChoices = 4;
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
     * If there are more than {@link #maxChoices} elements in {@code choices},
     * choose {@link #maxChoices} elements randomly. Otherwise, return all elements.
     *
     * @return a list of at most {@link #maxChoices} elements from {@code choices}
     */
    protected String getChoices() {
        ArrayList<String> list = new ArrayList<>(choices);
        if (choices.size() > maxChoices) {
            Collections.shuffle(list);
            return String.join("|", list.subList(0, maxChoices));
        } else {
            return String.join("|", list);
        }
    }

    /**
     * If there are more than {@link #maxChoices} elements in {@code answers},
     * choose {@link #maxChoices} elements randomly. Otherwise, return all elements.
     *
     * @return a list of at most {@link #maxChoices} elements from {@code answers}
     */
    protected String getAnswers() {
        ArrayList<String> list = new ArrayList<>(answers);
        if (answers.size() > maxChoices) {
            Collections.shuffle(list);
            return String.join("|", list.subList(0, maxChoices));
        } else {
            return String.join("|", list);
        }
    }

    protected String wrappedScratchBlocks(Script node) {
        return "[scratchblocks]\n" + ScratchBlocksVisitor.of(node) + "[/scratchblocks]";
    }

    protected String wrappedScratchBlocks(ProcedureDefinition node) {
        return "[scratchblocks]\n" + node.getScratchBlocks(program, currentActor) + "[/scratchblocks]";
    }

    protected String wrappedScratchBlocks(StmtList node) {
        return "[scratchblocks]\n" + ScratchBlocksVisitor.of(node) + "[/scratchblocks]";
    }

    protected String wrappedScratchBlocks(Stmt node) {
        return "[sbi]" + ScratchBlocksVisitor.of(node) + "[/sbi]";
    }

    protected String wrappedScratchBlocks(Expression node) {
        return "[sbi]" + ScratchBlocksVisitor.of(node) + "[/sbi]";
    }

    protected String wrappedScratchBlocks(Variable node) {
        return "[var]" + ScratchBlocksVisitor.of(node) + "[/var]";
    }

    protected String wrappedScratchBlocks(NumberLiteral node) {
        return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
    }

    protected String wrappedScratchBlocks(StringLiteral node) {
        if (inLookStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: looks ring>[/sbi]";
        } else if (inSoundStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: sound ring>[/sbi]";
        } else if (inBroadcastStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: control ring>[/sbi]";
        } else {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
        }
    }

    protected String wrappedScratchBlocks(ColorLiteral node) {
        return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
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

    protected Stmt getSingleStmt(Stmt stmt) {
        if (stmt instanceof IfThenStmt ifThenStmt) {
            return new IfThenStmt(ifThenStmt.getBoolExpr(), new StmtList(), ifThenStmt.getMetadata());
        } else if (stmt instanceof IfElseStmt ifElseStmt) {
            return new IfElseStmt(ifElseStmt.getBoolExpr(), new StmtList(), new StmtList(), ifElseStmt.getMetadata());
        } else if (stmt instanceof RepeatTimesStmt repeatTimesStmt) {
            return new RepeatTimesStmt(repeatTimesStmt.getTimes(), new StmtList(), repeatTimesStmt.getMetadata());
        } else if (stmt instanceof RepeatForeverStmt repeatForeverStmt) {
            return new RepeatForeverStmt(new StmtList(), repeatForeverStmt.getMetadata());
        } else if (stmt instanceof UntilStmt untilStmt) {
            return new UntilStmt(untilStmt.getBoolExpr(), new StmtList(), untilStmt.getMetadata());
        } else {
            return stmt;
        }
    }
}
