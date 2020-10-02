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
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CommonStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class VariableInitializationRace extends AbstractIssueFinder {

    private static class InitializationInstance {
        private Script script;
        private CommonStmt statement;
        public InitializationInstance(Script script, CommonStmt commonStmt) {
            this.script = script;
            this.statement = commonStmt;
        }

        public Script getScript() {
            return script;
        }

        public CommonStmt getStatement() {
            return statement;
        }
    }

    private Map<Event, Map<Identifier, Set<InitializationInstance>>> variableMap = new LinkedHashMap<>();
    private Event currentEvent = null;


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        variableMap = new LinkedHashMap<>();
        currentEvent = null;
        program.accept(this);
        checkConcurrentModifications();
        return Collections.unmodifiableSet(issues);
    }

    private void checkConcurrentModifications() {
        for (Event event : variableMap.keySet()) {
            for (Map.Entry<Identifier, Set<InitializationInstance>> entry : variableMap.get(event).entrySet()) {
                Set<InitializationInstance> modifyingScripts = entry.getValue();
                long numScripts = modifyingScripts.stream().map(InitializationInstance::getScript).map(System::identityHashCode).distinct().count();
                if (numScripts > 1) {
                    InitializationInstance instance = modifyingScripts.iterator().next();
                    currentScript = instance.getScript();
                    addIssue(instance.getStatement(), instance.getStatement().getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = script;
        currentProcedure = null;
        currentEvent = script.getEvent();

        List<Stmt> statements = script.getStmtList().getStmts();
        for (Stmt stmt : statements) {
            if (!isInitializationStatement(stmt)) {
                return;
            }
            stmt.accept(this);
        }
        currentScript = null;
    }

    private void trackVariableModification(Identifier identifier, CommonStmt stmt) {
        if (currentScript == null) {
            return;
        }

        if (!variableMap.containsKey(currentEvent)) {
            variableMap.put(currentEvent, new LinkedHashMap<>());
        }
        Map<Identifier, Set<InitializationInstance>> theMap = variableMap.get(currentEvent);

        if (!theMap.containsKey(identifier)) {
            theMap.put(identifier, new LinkedHashSet<>());
        }

        Set<InitializationInstance> scriptSet = theMap.get(identifier);
        scriptSet.add(new InitializationInstance(currentScript, stmt));
    }


    @Override
    public void visit(SetVariableTo node) {
        if (currentScript != null) {
            trackVariableModification(node.getIdentifier(), node);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (currentScript != null) {
            trackVariableModification(node.getIdentifier(), node);
        }
    }

    private boolean isInitializationStatement(Stmt stmt) {
        return stmt instanceof SetVariableTo || stmt instanceof ChangeVariableBy;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return "variable_initialization_race";
    }
}
