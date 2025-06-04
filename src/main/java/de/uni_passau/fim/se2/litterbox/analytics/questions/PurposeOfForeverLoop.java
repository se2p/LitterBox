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

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

/**
 * @QuestionType Free Text
 * @Highlighted Loop
 * @Context Single script
 */
public class PurposeOfForeverLoop extends AbstractQuestionFinder {

    @Override
    public void visit(RepeatForeverStmt node) {
        IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        addIssue(builder.withHint(hint));

        super.visit(node);
    }

    @Override
    public String getName() {
        return "purpose_of_forever_loop";
    }
}
