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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context Whole program
 */
public class ScriptsTriggeredByEvent extends AbstractQuestionFinder {

    private Map<Event, List<Script>> triggeredScripts;

    @Override
    public void visit(Program node) {
        triggeredScripts = new HashMap<>();
        super.visit(node);
        currentScript = null;
        currentProcedure = null;

        triggeredScripts.forEach((event, scripts) -> {
            if (!(event instanceof Never)) {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.EVENT, ScratchBlocksVisitor.of(event));
                hint.setParameter(Hint.ANSWER, Integer.toString(scripts.size()));
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(Script node) {
        Event event = node.getEvent();
        List<Script> list = triggeredScripts.getOrDefault(event, new ArrayList<>());
        list.add(node);
        triggeredScripts.put(event, list);
    }

    @Override
    public String getName() {
        return "scripts_triggered_by_event";
    }
}
