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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * Checks if there are unused variables.
 */
public class UnusedVariable extends AbstractIssueFinder {

    public static final String NAME = "unused_variables";
    private static final String[] MY_VARIABLE_LANGUAGES = {"meine Variable", "исхатәу аҽеиҭак", "my variable",
            "متغيري", "мая зменная", "моята променлива", "la meva variable", "گۆڕاوەکەم", "moje proměnná", "fy "
            + "newidyn", "min variabel", "η μεταβλητή μου", "mi variable", "minu muutuja", "nire aldagaia", "متغیر من",
            "muuttujani", "ma variable", "m'athróg", "an caochladair agam", "a miña variábel", "המשתנה שלי", "moja "
            + "varijabla", "az én változóm", "variabel saya", "la mia variabile", "へんすう", "変数", "ჩემი ცვლადი",
            "អថេរខ្ញុំ", "나의 변수", "mano kintamasis", "mans mainīgais", "taku taurangi", "min variabel", "mijn "
            + "variabele", "min variabel", "moja zmienna", "minha variável", "a minha variável", "toʾoku variable",
            "variabila mea", "моя переменная", "premenná", "moja spremenljivka", "моја променљива", "min variabel",
            "kibadilika changu", "ตัวแปรของฉัน", "değişkenim", "моя змінна", "mening o'zgaruvchim", "biến của tôi",
            "我的变量", "i-variable yami"};
    private List<Qualified> variableCalls;
    private boolean insideProcedure;
    private boolean insideScript;
    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        variableCalls = new ArrayList<>();
        program.accept(this);
        checkVariables();
        return issues;
    }

    private void checkVariables() {
        List<ActorDefinition> actors = program.getActorDefinitionList().getDefinitions();
        for (Map.Entry<String, VariableInfo> entry : varMap.entrySet()) {
            VariableInfo curr = entry.getValue();
            String actorName = curr.getActor();
            String name = curr.getVariableName();
            boolean currFound = false;
            for (int i = 0; i < variableCalls.size() && !currFound; i++) {
                if (variableCalls.get(i).getFirst().getName().equals(actorName)
                        && variableCalls.get(i).getSecond().getName().getName().equals(name)) {
                    currFound = true;
                    break;
                }
            }

            if (!currFound && !Arrays.asList(MY_VARIABLE_LANGUAGES).contains(name)) {
                for (ActorDefinition actor : actors) {
                    if (actor.getIdent().getName().equals(actorName)) {
                        currentActor = actor;
                        break;
                    }
                }
                Qualified qualified = new Qualified(new StrId(actorName), new Variable(new StrId(name)));
                addScriptWithIssueFor(qualified);
            }
        }

        for (Map.Entry<String, ExpressionListInfo> entry : listMap.entrySet()) {
            ExpressionListInfo curr = entry.getValue();
            String actorName = curr.getActor();
            String name = curr.getVariableName();
            boolean currFound = false;
            for (int i = 0; i < variableCalls.size() && !currFound; i++) {
                if (variableCalls.get(i).getFirst().getName().equals(actorName)
                        && variableCalls.get(i).getSecond().getName().getName().equals(name)) {
                    currFound = true;
                    break;
                }
            }
            if (!currFound) {
                for (ActorDefinition actor : actors) {
                    if (actor.getIdent().getName().equals(actorName)) {
                        currentActor = actor;
                        break;
                    }
                }
                Qualified qualified = new Qualified(new StrId(actorName), new ScratchList(new StrId(name)));
                addScriptWithIssueFor(qualified);
            }
        }
    }

    private void addScriptWithIssueFor(Expression expr) {
        Script theScript = new Script(new Never(), new StmtList(Arrays.asList(new ExpressionStmt(expr))));
        addIssueForSynthesizedScript(theScript, expr, new NoBlockMetadata());
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        super.visit(node);
        insideScript = false;
    }

    @Override
    public void visit(Qualified node) {
        if (insideProcedure || insideScript) {
            variableCalls.add(node);
        }
        visitChildren(node);
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
