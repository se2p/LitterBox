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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Identifier;

import java.util.*;

public class ControlFlowGraphBuilder {

    private ControlFlowGraph cfg = new ControlFlowGraph();

    private java.util.List<CFGNode> currentNodes = new ArrayList<>();

    private ActorDefinition currentActor = null;

    private ASTNode currentScriptOrProcedure = null;

    private Map<CFGNode, List<CFGNode>> procedureMap = new LinkedHashMap<>();

    private Map<CFGNode, CFGNode> procedureCallMap = new LinkedHashMap<>();

    public ControlFlowGraph getControlFlowGraph() {
        addMissingEdgesToExit();
        connectCustomBlockCalls();
        cfg.fixDetachedEntryExit();
        return cfg;
    }

    public void addEndOfProcedure(ProcedureDefinition node, List<CFGNode> endOfProcedure) {
        ProcedureInfo procDef = ProgramParser.procDefMap.getProcedureForHash(currentActor.getIdent().getName(), node.getIdent().getName());

        ProcedureNode customBlockNode = new ProcedureNode(procDef.getName(), procDef.getActorName());

        procedureMap.put(customBlockNode, endOfProcedure);
    }

    private void connectCustomBlockCalls() {
        for(Map.Entry<CFGNode, CFGNode> entry : procedureCallMap.entrySet()) {
            CFGNode callNode = entry.getKey();
            CFGNode procedureNode = entry.getValue();
            if(procedureMap.containsKey(procedureNode)) {
                List<CFGNode> endNodes = procedureMap.get(procedureNode);
                endNodes.forEach(n -> cfg.addEdge(n, callNode));

            } else {
                cfg.addEdge(procedureNode, callNode);
            }
        }
    }

    public void setCurrentActor(ActorDefinition actor) {
        this.currentActor = actor;
    }

    public void setCurrentScriptOrProcedure(ASTNode node) {
        this.currentScriptOrProcedure = node;
    }

    public CFGNode addStatement(Stmt stmt) {
        CFGNode node = cfg.addNode(stmt, currentActor, currentScriptOrProcedure);
        currentNodes.forEach(n -> cfg.addEdge(n, node));
        setCurrentNode(node);
        return node;
    }

    public void addEdge(CFGNode node) {
        currentNodes.forEach(n -> cfg.addEdge(n, node));
        setCurrentNode(node);
    }

    public void addMissingEdgesToExit() {
        currentNodes.forEach(n -> cfg.addEdgeToExit(n));
        currentNodes.clear();
    }

    public void addEdgeToExit() {
        currentNodes.forEach(n -> cfg.addEdgeToExit(n));
        currentNodes.clear();
    }

    public void setCurrentStatement(CFGNode... nodes) {
        currentNodes.clear();
        Arrays.stream(nodes).forEach(s -> currentNodes.add(s));
    }

    public void addCurrentStatement(CFGNode node) {
        currentNodes.add(node);
    }

    public List<CFGNode> getCurrentStatements() {
        return Collections.unmodifiableList(currentNodes);
    }

    private void setCurrentNode(CFGNode... node) {
        currentNodes = new ArrayList<>(Arrays.asList(node));
    }

    public void addUserEventHandler(Event event) {

        // Create new event node
        CFGNode node = cfg.addNode(event);

        // Add edge from Entry to event node
        cfg.addEdgeFromEntry(node);

        // Add edge from event node to exit node
        cfg.addEdgeToExit(node);

        // Update current node to event node (so that it branches)
        setCurrentNode(node);
    }

    public void addEventHandler(Event event) {
        CFGNode eventNode = cfg.addNode(event);
        cfg.addEdgeToExit(eventNode);

        // Update current node to event node (so that it branches)
        setCurrentNode(eventNode);
    }

    public void addVariableEventHandler(AttributeAboveValue node) {
        CFGNode eventNode = cfg.addNode(node);
        cfg.addEdgeFromEntry(eventNode);
        cfg.addEdgeToExit(eventNode);

        // Update current node to event node (so that it branches)
        setCurrentNode(eventNode);
    }


    public void addBroadcastHandler(Message message) {

        CFGNode handlerNode = cfg.addNode(message);
        cfg.addEdgeToExit(handlerNode);
        setCurrentNode(handlerNode);
    }

    public void addBroadcastStatement(Stmt stmt, Message message) {
        // Add node and edge from current
        CFGNode node = addStatement(stmt);

        // Retrieve broadcast handler, or create if it doesn't exist yet
        CFGNode handlerNode = cfg.addNode(message);
        cfg.addEdgeToExit(handlerNode); // Broadcasts need a second edge to exit

        // Add edge from node to broadcast handler
        cfg.addEdge(node, handlerNode);
    }

    public void addCreateClone(CreateCloneOf stmt) {
        // Add node and edge from current
        CFGNode node = addStatement(stmt);

        List<String> names = new LinkedList<>();
        stmt.getStringExpr().accept(new ScratchVisitor() {
            @Override
            public void visit(StringLiteral node) {
                names.add(node.getText());
            }
        });

        assert(names.size() == 1);
        String name = names.get(0);
        if(name.equals(Identifier.MYSELF.getValue())) {
            name = currentActor.getIdent().getName();
        }

        CloneEventNode handlerNode = new CloneEventNode(name);
        cfg.addEdgeToExit(handlerNode);

        // Add edge from node to clone handler
        cfg.addEdge(node, handlerNode);
    }

    public void addCloneHandler(StartedAsClone node) {
        CloneEventNode handlerNode = new CloneEventNode(currentActor.getIdent().getName());
        cfg.addEdgeToExit(handlerNode);
        setCurrentNode(handlerNode);
    }

    public void addProcedure(ProcedureDefinition node) {

        ProcedureInfo procDef = ProgramParser.procDefMap.getProcedureForHash(currentActor.getIdent().getName(), node.getIdent().getName());
        ProcedureNode customBlockNode = new ProcedureNode(procDef.getName(), procDef.getActorName());
        setCurrentNode(customBlockNode);
    }

    public void addCall(CallStmt stmt) {

        // Add node and edge from current
        CFGNode node = addStatement(stmt);

        // Add edge to procedure entry node
        ProcedureNode customBlockNode = new ProcedureNode(stmt.getIdent().getName(), currentActor.getIdent().getName());
        cfg.addEdge(node, customBlockNode);

        // Add return node as next node
        CallReturnNode returnNode = new CallReturnNode(stmt);
        //currentNodes.forEach(n -> cfg.addEdge(n, node));
        setCurrentNode(returnNode);

        // Ensure edge from end of procedure to return node
        procedureCallMap.put(returnNode, customBlockNode);
    }

    public void addStopStatement(Stmt stmt) {
        CFGNode node = addStatement(stmt);
        cfg.addEdgeToExit(node);
    }
}
