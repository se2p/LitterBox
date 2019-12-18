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
package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.variable.Qualified;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.*;

/**
 * Checks if there are unused variables.
 */
public class UnusedVariable implements IssueFinder, ScratchVisitor {

    public static final String NAME = "unused_variables";
    public static final String SHORT_NAME = "unsdvrbls";
    private static final String NOTE1 = "There are no unused variables in your project.";
    private static final String NOTE2 = "Some of the sprites contain unused variables.";
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private List<Qualified> variableCalls;
    private boolean insideProcedure;
    private boolean insideScript;
    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);

        count = 0;
        actorNames = new LinkedList<>();
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        variableCalls = new ArrayList<>();
        program.accept(this);
        String notes = NOTE1;
        checkVariables();
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }


    private void checkVariables() {

        Set<String> ids = varMap.keySet();
        for (String id : ids) {
            VariableInfo curr = varMap.get(id);
            String actorName = curr.getActor();
            String name = curr.getVariableName();
            boolean currFound = false;
            for (int i = 0; i < variableCalls.size() && !currFound; i++) {
                if (variableCalls.get(i).getFirst().getName().equals(actorName)
                        && variableCalls.get(i).getSecond().getName().equals(name)) {
                    currFound = true;
                }
            }
            if (!currFound) {
                count++;

            }
        }
        ids = listMap.keySet();
        for (String id : ids) {
            ExpressionListInfo curr = listMap.get(id);
            String actorName = curr.getActor();
            String name = curr.getVariableName();
            boolean currFound = false;
            for (int i = 0; i < variableCalls.size() && !currFound; i++) {
                if (variableCalls.get(i).getFirst().getName().equals(actorName)
                        && variableCalls.get(i).getSecond().getName().equals(name)) {
                    currFound = true;
                }
            }
            if (!currFound) {
                count++;
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideScript = false;

    }

    @Override
    public void visit(Qualified node) {
        if (insideProcedure || insideScript) {
            variableCalls.add(node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
