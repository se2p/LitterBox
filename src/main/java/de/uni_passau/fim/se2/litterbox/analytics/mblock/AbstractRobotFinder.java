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
package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.*;

public abstract class AbstractRobotFinder extends AbstractIssueFinder implements MBlockVisitor {

    protected RobotCode robot;
    protected Map<String, StmtList> procedureStmts = new HashMap<>();
    protected Map<Script, List<String>> proceduresInScript = new HashMap<>();
    protected boolean parseProcedureDefinitions = true;
    protected boolean putProceduresinScript = false;

    @Override
    public void visit(Program program) {
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        currentScript = null;
        String currentProcedureName = procMap.get(procedure.getIdent()).getName();
        procedureStmts.put(currentProcedureName, procedure.getStmtList());
        if (parseProcedureDefinitions) {
            visitChildren(procedure);
        }
    }

    @Override
    public void visit(StmtList node) {
        if (currentScript != null && putProceduresinScript) {
            boolean added = false;
            List<Stmt> newList = new LinkedList<>();
            for (Stmt stmt : node.getStmts()) {
                if (stmt instanceof CallStmt callStmt) {
                    String procedureName = callStmt.getIdent().getName();
                    if (procedureStmts.containsKey(procedureName)
                            && !(proceduresInScript.get(currentScript).contains(procedureName))) {
                        added = true;
                        newList.addAll(procedureStmts.get(procedureName).getStmts());
                        proceduresInScript.get(currentScript).add(procedureName);
                    }
                } else {
                    newList.add(stmt);
                }
            }
            StmtList stmtList = new StmtList(newList);
            if (added) {
                visit(stmtList);
            } else {
                visit((ASTNode) stmtList);
            }
        } else {
            visit((ASTNode) node);
        }
    }

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = script;
        proceduresInScript.put(script, new LinkedList<>());
        currentProcedure = null;
        visitChildren(script);
    }

    @Override
    public void visit(MBlockNode node) {
        node.accept((MBlockVisitor) this);
    }

    @Override
    public void visitParentVisitor(MBlockNode node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(ActorDefinition actor) {
        Preconditions.checkNotNull(program);
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        String actorName = actor.getIdent().getName();
        robot = getRobot(actorName, actor.getSetStmtList());
        if (!(robot == CODEY || robot == MCORE)) {
            finishRobotVisit();
            return;
        }

        super.visit(actor);
        finishRobotVisit();
    }

    protected void finishRobotVisit() {
        robot = NO_ROBOT;
    }
}
