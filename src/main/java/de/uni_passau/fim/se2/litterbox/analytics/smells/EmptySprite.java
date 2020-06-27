/*
 * Copyright (C) 2020 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EmptySprite implements IssueFinder, ScratchVisitor {
    public static final String NAME = "empty_sprite";
    public static final String SHORT_NAME = "empSprite";
    private static final String NOTE1 = "There are no sprites without scripts in your project.";
    private static final String NOTE2 = "Some of the sprites contain no scripts.";
    private boolean found = false;
    private int count = 0;
    private Set<Issue> issues = new LinkedHashSet<>();
    private List<String> actorNames = new LinkedList<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return issues;
        // return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (actor.getProcedureDefinitionList().getList().size() == 0 && actor.getScripts().getScriptList().size() == 0
                && !actor.getActorType().equals(ActorType.STAGE)) {
            actorNames.add(actor.getIdent().getName());
            count++;
            issues.add(new Issue(this, actor, actor));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
