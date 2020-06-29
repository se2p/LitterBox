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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class SameVariableDifferentSprite implements IssueFinder {
    public static final String NAME = "same_variable_different_sprite";
    public static final String SHORT_NAME = "sameVarDiffSprite";
    public static final String HINT_TEXT = "same_variable different sprite";
    private Set<Issue> issues = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        boolean found = false;
        List<ActorDefinition> actorDefinitions = program.getActorDefinitionList().getDefinitions();
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
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    if (actorDefinition.getIdent().getName().equals(currentActor)) {
                        Script script = actorDefinition.getScripts().getScriptList().isEmpty() ? null : actorDefinition.getScripts().getScriptList().get(0);

                        issues.add(new Issue(this,
                                actorDefinition,
                                script, // TODO: What is the correct script?
                                actorDefinition,
                                HINT_TEXT,
                                null)); // TODO: Improve -- null ensures loose comment
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
                for (ActorDefinition actorDefinition : actorDefinitions) {
                    if (actorDefinition.getIdent().getName().equals(currentActor)) {
                        Script script = actorDefinition.getScripts().getScriptList().isEmpty() ? null : actorDefinition.getScripts().getScriptList().get(0);
                        issues.add(new Issue(this,
                                actorDefinition,
                                script, // TODO: What is the correct script?
                                actorDefinition,
                                HINT_TEXT,
                                null)); // TODO: Improve -- null ensures loose comment
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
}
