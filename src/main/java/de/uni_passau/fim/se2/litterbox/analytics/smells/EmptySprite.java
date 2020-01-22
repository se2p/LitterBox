/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class EmptySprite implements IssueFinder {
    public static final String NAME = "empty_sprite";
    public static final String SHORT_NAME = "empSprite";
    private static final String NOTE1 = "There are no sprites without scripts in your project.";
    private static final String NOTE2 = "Some of the sprites contain no scripts.";

    public EmptySprite() {
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        List<String> found = new ArrayList<>();

        final List<ActorDefinition> definitions = program.getActorDefinitionList().getDefintions();

        for (ActorDefinition actor : definitions) {
            if (actor.getScripts().getScriptList().size() == 0 && !(actor.getActorType()== ActorType.STAGE)) {
                found.add(actor.getIdent().getName());
            }

        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), IssueTool.getOnlyUniqueActorList(found), notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
