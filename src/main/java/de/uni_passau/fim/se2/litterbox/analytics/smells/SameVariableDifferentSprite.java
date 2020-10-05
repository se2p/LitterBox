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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class SameVariableDifferentSprite extends AbstractIssueFinder {
    public static final String NAME = "same_variable_different_sprite";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        boolean found = false;
        issues = new LinkedHashSet<>();
        List<ActorDefinition> actorDefinitions = program.getActorDefinitionList().getDefinitions();
        Map<String, VariableInfo> variableInfoMap = program.getSymbolTable().getVariables();
        ArrayList<VariableInfo> varInfos = new ArrayList<>(variableInfoMap.values());
        for (int i = 0; i < varInfos.size(); i++) {
            String currentName = varInfos.get(i).getVariableName();
            String currentActorName = varInfos.get(i).getActor();
            for (int j = 0; j < varInfos.size(); j++) {
                if (i != j && currentName.equals(varInfos.get(j).getVariableName())
                        && !currentActorName.equals(varInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    currentActor = actorDefinition;
                    if (actorDefinition.getIdent().getName().equals(currentActorName)) {
                        addIssueWithLooseComment();
                        break;
                    }
                }
            }
        }

        Map<String, ExpressionListInfo> listInfoMap = program.getSymbolTable().getLists();
        ArrayList<ExpressionListInfo> listInfos = new ArrayList<>(listInfoMap.values());
        for (int i = 0; i < listInfos.size(); i++) {
            String currentName = listInfos.get(i).getVariableName();
            String currentActorName = listInfos.get(i).getActor();
            for (int j = 0; j < listInfos.size(); j++) {
                if (i != j && currentName.equals(listInfos.get(j).getVariableName())
                        && !currentActorName.equals(listInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    if (actorDefinition.getIdent().getName().equals(currentActorName)) {
                        currentActor = actorDefinition;
                        addIssueWithLooseComment();
                        break;
                    }
                }
            }
        }

        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
