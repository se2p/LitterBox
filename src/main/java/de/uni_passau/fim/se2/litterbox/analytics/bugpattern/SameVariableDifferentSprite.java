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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addLooseComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class SameVariableDifferentSprite implements IssueFinder {
    public static final String NAME = "same_variable_different_sprite";
    public static final String SHORT_NAME = "sameVarDiffSprite";
    public static final String HINT_TEXT = "same_variable different sprite";
    private static final String NOTE1 = "There are no variables with the same name in your project.";
    private static final String NOTE2 = "Some of the variables have the same name but are in different sprites.";
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
        // TODO: Fix typo in signature
        List<ActorDefinition> actorDefinitions = program.getActorDefinitionList().getDefintions();
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        Map<String, VariableInfo> variableInfoMap = program.getSymbolTable().getVariables();
        ArrayList<VariableInfo> varInfos = new ArrayList<>(variableInfoMap.values());
        for (int i = 0; i < varInfos.size(); i++) {
            String currentName = varInfos.get(i).getVariableName();
            String currentActor = varInfos.get(i).getActor();
            for (int j = 0; j < varInfos.size(); j++) {
                if (i != j && currentName.equals(varInfos.get(j).getVariableName()) && !currentActor.equals(varInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                count++;
                actorNames.add(currentActor);
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    if (actorDefinition.getIdent().getName().equals(currentActor)) {
                        addLooseComment(actorDefinition, HINT_TEXT + " Variable " + currentName,
                                SHORT_NAME + count);
                        break;
                    }
                }
            }
        }

        Map<String, ExpressionListInfo> listInfoMap = program.getSymbolTable().getLists();
        ArrayList<ExpressionListInfo> listInfos = new ArrayList<>(listInfoMap.values());
        for (int i = 0; i < listInfos.size(); i++) {
            String currentName = listInfos.get(i).getVariableName();
            String currentActor = listInfos.get(i).getActor();
            for (int j = 0; j < listInfos.size(); j++) {
                if (i != j && currentName.equals(listInfos.get(j).getVariableName()) && !currentActor.equals(listInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                count++;
                actorNames.add(currentActor);
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    if (actorDefinition.getIdent().getName().equals(currentActor)) {
                        issues.add(new Issue(this, actorDefinition, actorDefinition));
                        addLooseComment(actorDefinition, HINT_TEXT + " List " + currentName,
                                SHORT_NAME + count);
                        break;
                    }
                }
            }
        }

        return issues;
        // return new IssueReport(NAME, count, IssueTool.getOnlyUniqueActorList(actorNames), notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
