/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * If a custom block calls itself inside its body and has no condition to stop the recursion, it will run for an
 * indefinite amount of time. The same holds true for broadcast reception scripts that send the same message.
 */
public class EndlessRecursion extends AbstractIssueFinder {
    public static final String NAME = "endless_recursion";
    private static final String BROADCAST_HINT = "endless_recursion_broadcast";
    private static final String PROCEDURE_HINT = "endless_recursion_procedure";
    private String currentProcedureName;
    private String currentMessageName;
    private boolean insideProcedure;
    private boolean insideBroadcastReception;
    private int loopIfCounter;

    @Override
    public void visit(ActorDefinition actor) {
        loopIfCounter = 0;
        super.visit(actor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        super.visit(node);
        insideProcedure = false;
        currentProcedureName = null;
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        currentMessageName = ((StringLiteral) node.getMsg().getMessage()).getText();
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof ReceptionOfMessage) {
            insideBroadcastReception = true;
            super.visit(node);
            insideBroadcastReception = false;
            currentMessageName = null;
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (insideBroadcastReception && node.getMessage().getMessage() instanceof StringLiteral && loopIfCounter == 0) {
            if (((StringLiteral) node.getMessage().getMessage()).getText().equals(currentMessageName)) {
                IssueBuilder builder = prepareIssueBuilder(node)
                        .withSeverity(IssueSeverity.HIGH)
                        .withHint(BROADCAST_HINT)
                        .withRefactoring(getBroadcastRefactoring(node));

                addIssue(builder);

            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (insideBroadcastReception && node.getMessage().getMessage() instanceof StringLiteral && loopIfCounter == 0) {
            if (((StringLiteral) node.getMessage().getMessage()).getText().equals(currentMessageName)) {
                IssueBuilder builder = prepareIssueBuilder(node)
                        .withSeverity(IssueSeverity.HIGH)
                        .withHint(BROADCAST_HINT)
                        .withRefactoring(getBroadcastRefactoring(node));

                addIssue(builder);
            }
        }
    }

    @Override
    public void visit(CallStmt node) {
        if (insideProcedure && loopIfCounter == 0) {
            String call = node.getIdent().getName();
            if (call.equals(currentProcedureName)) {
                IssueBuilder builder = prepareIssueBuilder(node)
                        .withSeverity(IssueSeverity.HIGH)
                        .withHint(PROCEDURE_HINT)
                        .withRefactoring(getProcedureRefactoring(node));

                addIssue(builder);
            }
        }
    }

    private ProcedureDefinition getProcedureRefactoring(CallStmt node) {
        List<Stmt> stmtList = ((StmtList)node.getParentNode()).getStmts();
        int position = stmtList.indexOf(node);
        stmtList = stmtList.subList(0, position);
        RepeatForeverStmt repeatForeverStmt = new RepeatForeverStmt(new StmtList(stmtList), node.getMetadata());
        return new NodeReplacementVisitor(node.getParentNode(), new StmtList(repeatForeverStmt)).apply(currentProcedure);
    }

    private ScriptEntity getBroadcastRefactoring(ASTNode node) {
        // Note that this is a valid local refactoring, but if there are other receivers this
        // would break the program
        List<Stmt> stmtList = ((StmtList)node.getParentNode()).getStmts();
        int position = stmtList.indexOf(node);
        stmtList = stmtList.subList(0, position);
        RepeatForeverStmt repeatForeverStmt = new RepeatForeverStmt(new StmtList(stmtList), node.getMetadata());
        return new NodeReplacementVisitor(node.getParentNode(), new StmtList(repeatForeverStmt)).apply(getCurrentScriptEntity());
    }

    @Override
    public void visit(IfElseStmt node) {
        loopIfCounter++;
        visitChildren(node);
        loopIfCounter--;
    }

    @Override
    public void visit(IfThenStmt node) {
        loopIfCounter++;
        visitChildren(node);
        loopIfCounter--;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(BROADCAST_HINT);
        keys.add(PROCEDURE_HINT);
        return keys;
    }
}
