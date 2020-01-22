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
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * Checks if there are unused variables.
 */
public class UnusedVariable implements IssueFinder, ScratchVisitor {

    public static final String NAME = "unused_variables";
    public static final String SHORT_NAME = "unusedVar";
    private static final String NOTE1 = "There are no unused variables in your project.";
    private static final String NOTE2 = "Some of the sprites contain unused variables.";
    public static final String[] MY_VARIABLE_LANGUAGES = {"meine Variable", "исхатәу аҽеиҭак", "my variable", "متغيري", "мая зменная", "моята променлива", "la meva variable", "گۆڕاوەکەم", "moje proměnná", "fy newidyn", "min variabel", "η μεταβλητή μου", "mi variable", "minu muutuja", "nire aldagaia", "متغیر من", "muuttujani", "ma variable", "m'athróg", "an caochladair agam", "a miña variábel", "המשתנה שלי", "moja varijabla", "az én változóm", "variabel saya", "la mia variabile", "へんすう", "変数", "ჩემი ცვლადი", "អថេរខ្ញុំ", "나의 변수", "mano kintamasis", "mans mainīgais", "taku taurangi", "min variabel", "mijn variabele", "min variabel", "moja zmienna", "minha variável", "a minha variável", "toʾoku variable", "variabila mea", "моя переменная", "premenná", "moja spremenljivka", "моја променљива", "min variabel", "kibadilika changu", "ตัวแปรของฉัน", "değişkenim", "моя змінна", "mening o'zgaruvchim", "biến của tôi", "我的变量", "i-variable yami"};
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

            if (!currFound && !Arrays.asList(MY_VARIABLE_LANGUAGES).contains(name.substring(Constants.VARIABLE_ABBREVIATION.length()))) {
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
