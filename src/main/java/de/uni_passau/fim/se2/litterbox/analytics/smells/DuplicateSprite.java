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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateSprite extends AbstractIssueFinder {

    private static final String NAME = "duplicate_sprite";

    @Override
    public void visit(Program node) {
        Set<ActorDefinition> checked = new HashSet<>();
        ActorDefinitionList actors = node.getActorDefinitionList();
        for (ActorDefinition actor : actors.getDefinitions()) {
            for (ActorDefinition other : actors.getDefinitions()) {
                if (actor == other || checked.contains(other)) {
                    continue;
                }

                if (areActorsIdentical(actor, other)) {
                    currentActor = actor;
                    procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
                    addIssueWithLooseComment();
                }
            }
            checked.add(actor);
        }
    }

    private boolean areActorsIdentical(ActorDefinition actor, ActorDefinition other) {

        if (!actor.getActorType().equals(other.getActorType())) {
            return false;
        }

        if (!compareScriptLists(actor.getScripts(), other.getScripts())) {
            return false;
        }

        if (!compareProcedureDefinitions(actor.getProcedureDefinitionList(), other.getProcedureDefinitionList())) {
            return false;
        }

        return actor.getDecls().equals(other.getDecls());
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private boolean compareProcedureDefinitions(ProcedureDefinitionList procList1, ProcedureDefinitionList procList2) {
        if (procList1.getList().size() != procList2.getList().size()) {
            return false;
        }

        // For each procedure in list1 there has to be one with the same parameters
        // and the same code in the other
        for (ProcedureDefinition procDef1 : procList1.getList()) {
            boolean hasMatching = false;

            for (ProcedureDefinition procDef2 : procList2.getList()) {
                if (!procDef1.getParameterDefinitionList().equals(procDef2.getParameterDefinitionList())) {
                    continue;
                }
                if (!procDef1.getStmtList().equals(procDef2.getStmtList())) {
                    continue;
                }
                hasMatching = true;
                break;
            }
            if (!hasMatching) {
                return false;
            }
        }

        return true;
    }

    private boolean compareScriptLists(ScriptList scriptList1, ScriptList scriptList2) {
        if (scriptList1.getScriptList().size() != scriptList2.getScriptList().size()) {
            return false;
        }

        // For each script in list1 there has to be one with the same code in the other
        for (Script script1 : scriptList1.getScriptList()) {
            boolean hasMatching = false;

            for (Script script2 : scriptList2.getScriptList()) {

                if (!compareScript(script1, script2)) {
                    continue;
                }
                hasMatching = true;
                break;
            }
            if (!hasMatching) {
                return false;
            }
        }

        return true;
    }

    private boolean compareScript(Script script1, Script script2) {
        List<Stmt> statements1 = script1.getStmtList().getStmts();
        List<Stmt> statements2 = script2.getStmtList().getStmts();

        if (statements1.size() != statements2.size()) {
            return false;
        }

        for (int i = 0; i < statements1.size(); i++) {
            if (!compareNodes(statements1.get(i), statements2.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean compareChildren(ASTNode node1, ASTNode node2) {
        List<? extends ASTNode> nodes1 = node1.getChildren();
        List<? extends ASTNode> nodes2 = node2.getChildren();

        if (nodes1.size() != nodes2.size()) {
            return false;
        }

        for (int i = 0; i < nodes1.size(); i++) {
            if (!compareNodes(nodes1.get(i), nodes2.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean compareNodes(ASTNode node1, ASTNode node2) {

        if (!node1.getClass().equals(node2.getClass())) {
            return false;
        }

        if (node1 instanceof Qualified) {
            // Only compare variable name, not actor, such that local variables can be equal
            return ((Qualified)node1).getSecond().equals(((Qualified)node2).getSecond());
        } else {
            // If the class of the nodes does not define its own equals method
            // use our own local method to visit children, such that we
            // can control how Qualifieds are compared
            try {
                node1.getClass().getDeclaredMethod("equals", Object.class);
                return node1.equals(node2);
            } catch (NoSuchMethodException e) {
                return compareChildren(node1, node2);
            }
        }
    }
}
