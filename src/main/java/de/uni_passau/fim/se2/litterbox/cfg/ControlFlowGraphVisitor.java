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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowGraphVisitor implements ScratchVisitor {

    private ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder();

    private boolean inScript = false;

    private Program program = null;

    public ControlFlowGraph getControlFlowGraph() {
        return builder.getControlFlowGraph();
    }

    @Override
    public void visit(Program node) {
        this.program = node;
        List<ActorDefinition> actors = node.getActorDefinitionList().getDefinitions();
        builder.setActors(actors);
        visit((ASTNode) node);
    }

    @Override
    public void visit(Script node) {
        inScript = true;
        builder.setCurrentScriptOrProcedure(node);
        visit((ASTNode) node);
        inScript = false;
        builder.addEdgeToExit();
    }

    @Override
    public void visit(ActorDefinition node) {
        builder.setCurrentActor(node);
        visit((ASTNode) node);
        // TODO: Unset afterwards?
    }

    @Override
    public void visit(Stmt node) {
        if (!isInScript()) {
            // Variable declarations outside of scripts are irrelevant for the CFG
            return;
        }

        builder.addStatement(node);
    }

    //---------------------------------------------------------------
    // Custom blocks and invocation

    @Override
    public void visit(CallStmt node) {
        builder.addCall(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        builder.addProcedure(program, node);
        builder.setCurrentScriptOrProcedure(node);
        inScript = true;
        node.getStmtList().accept(this);
        inScript = false;
        builder.addEndOfProcedure(program, node, builder.getCurrentStatements());
    }

    //---------------------------------------------------------------
    // Clone statements

    @Override
    public void visit(CreateCloneOf node) {
        builder.addCreateClone(node);
    }

    @Override
    public void visit(StartedAsClone node) {
        builder.addCloneHandler(node);
    }

    //---------------------------------------------------------------
    // Broadcast statements

    @Override
    public void visit(BroadcastAndWait node) {
        builder.addBroadcastStatement(node, node.getMessage());
    }

    @Override
    public void visit(Broadcast node) {
        builder.addBroadcastStatement(node, node.getMessage());
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        builder.addBroadcastHandler(node.getMsg());
    }

    //---------------------------------------------------------------
    // Control statements

    @Override
    public void visit(RepeatForeverStmt stmt) {
        CFGNode node = builder.addStatement(stmt);

        stmt.getStmtList().accept(this);

        // Edge back to loop header, and update current node
        builder.addEdge(node);
        builder.addEdgeToExit();
    }

    @Override
    public void visit(RepeatTimesStmt stmt) {
        CFGNode node = builder.addStatement(stmt);

        stmt.getStmtList().accept(this);

        // Edge back to loop header, and update current node
        builder.addEdge(node);
    }

    @Override
    public void visit(UntilStmt stmt) {
        CFGNode node = builder.addStatement(stmt);

        stmt.getStmtList().accept(this);

        // Edge back to loop header, and update current node
        builder.addEdge(node);
    }

    @Override
    public void visit(IfElseStmt stmt) {
        CFGNode node = builder.addStatement(stmt);

        // Then statements:
        stmt.getStmtList().accept(this);
        List<CFGNode> endOfThen = new ArrayList<>(builder.getCurrentStatements());

        // Go back to head so that else is attached to if
        builder.setCurrentStatement(node);

        // Else statements:
        stmt.getElseStmts().accept(this);

        // Next node should attach to end of then and else
        endOfThen.forEach(n -> builder.addCurrentStatement(n));
    }

    @Override
    public void visit(IfThenStmt stmt) {
        CFGNode node = builder.addStatement(stmt);
        StmtList thenStatements = stmt.getThenStmts();
        thenStatements.accept(this);

        // Next statement linked to if and end of then
        builder.addCurrentStatement(node);
    }

    //---------------------------------------------------------------
    // Termination statements

    @Override
    public void visit(DeleteClone node) {
        builder.addStopStatement(node);
    }

    @Override
    public void visit(StopAll node) {
        builder.addStopStatement(node);
    }

    @Override
    public void visit(StopThisScript node) {
        builder.addStopStatement(node);
    }

    //---------------------------------------------------------------
    // Events

    @Override
    public void visit(GreenFlag node) {
        builder.addUserEventHandler(node);
    }

    @Override
    public void visit(KeyPressed node) {
        builder.addUserEventHandler(node);
    }

    @Override
    public void visit(Clicked node) {
        builder.addUserEventHandler(node);
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        builder.addEventHandler(node);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        builder.addVariableEventHandler(node);
    }

    private boolean isInScript() {
        return inScript;
    }
}
