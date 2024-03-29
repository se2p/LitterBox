/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementDeletionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This finder looks if a repeat times loop only does one or zero repetition.
 */
public class UnnecessaryLoop extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_loop";
    public static final String ONE_HINT = "loop_one";
    public static final String ZERO_HINT = "loop_zero";

    @Override
    public void visit(RepeatTimesStmt node) {
        if (node.getTimes() instanceof NumberLiteral) {
            if (((NumberLiteral) node.getTimes()).getValue() == 1) {
                IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW).withHint(ONE_HINT);
                // TODO: Statements are not copied, is this important?
                StatementReplacementVisitor visitor = new StatementReplacementVisitor(node, node.getStmtList().getStmts());
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                addIssue(builder.withRefactoring(refactoring));
            } else if (((NumberLiteral) node.getTimes()).getValue() == 0) {
                IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW).withHint(ZERO_HINT);
                // TODO: Resulting script may be empty?
                StatementDeletionVisitor visitor = new StatementDeletionVisitor(node);
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                addIssue(builder.withRefactoring(refactoring));
            }
        }
    }

    @Override
    public boolean isSubsumedBy(Issue theIssue, Issue other) {
        if (theIssue.getFinder() != this) {
            return super.isSubsumedBy(theIssue, other);
        }

        if (other.getFinder() instanceof ForeverInsideLoop) {
            //need parent of the parent (the parent of forever is the StmtList) of forever because UnnecessaryLoop flags the parent loop and not the nested forever loop
            if (theIssue.getCodeLocation().equals(other.getCodeLocation().getParentNode().getParentNode())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(ZERO_HINT);
        keys.add(ONE_HINT);
        return keys;
    }
}
