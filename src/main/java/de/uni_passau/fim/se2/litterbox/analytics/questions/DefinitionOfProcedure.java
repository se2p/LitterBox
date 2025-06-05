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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Statement
 * @Context Single actor
 */
public class DefinitionOfProcedure extends AbstractQuestionFinder {

    private Map<String, ProcedureDefinition> procedures;
    private Map<String, CallStmt> procedureCalls;
    private Map<CallStmt, Script> scriptForCall;
    private Map<CallStmt, ActorDefinition> actorForCall;
    private Set<Script> scripts;

    @Override
    public void visit(ActorDefinition node) {
        procedures = new HashMap<>();
        procedureCalls = new HashMap<>();
        scriptForCall = new HashMap<>();
        actorForCall = new HashMap<>();
        scripts = new LinkedHashSet<>();

        super.visit(node);

        procedureCalls.forEach((ident, stmt) -> {
            if (procedures.containsKey(ident)) {
                choices.clear();
                // prioritize other procedure definitions in list of choices
                procedures.forEach((i, p) -> {
                    if (!i.equals(ident)) {
                        choices.add(wrappedScratchBlocks(p));
                    }
                });
                scripts.forEach(s -> {
                    if (choices.size() < maxChoices) {
                        choices.add(wrappedScratchBlocks(s));
                    }
                });

                currentScript = scriptForCall.get(stmt);
                currentActor = actorForCall.get(stmt);

                IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                hint.setParameter(Hint.ANSWER, wrappedScratchBlocks(procedures.get(ident)));
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(CallStmt node) {
        procedureCalls.putIfAbsent(node.getIdent().getName(), node);
        scriptForCall.putIfAbsent(node, currentScript);
        actorForCall.putIfAbsent(node, currentActor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        String procedureName = program.getProcedureMapping().getProcedures()
                .get(currentActor.getIdent().getName()).get(node.getIdent()).getName();
        procedures.put(procedureName, node);
    }

    @Override
    public void visit(Script node) {
        super.visit(node);
        scripts.add(node);
    }

    @Override
    public String getName() {
        return "definition_of_procedure";
    }
}
