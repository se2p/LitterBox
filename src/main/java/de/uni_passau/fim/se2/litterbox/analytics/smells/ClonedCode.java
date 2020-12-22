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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CloneAnalysis;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CodeClone;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.*;

abstract class ClonedCode extends AbstractIssueFinder {

    private CodeClone.CloneType targetType;

    private String hintName;

    protected ClonedCode(CodeClone.CloneType targetType, String hintName) {
        this.targetType = targetType;
        this.hintName = hintName;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());

        List<Script> scripts = actor.getScripts().getScriptList();
        List<ProcedureDefinition> procedures = actor.getProcedureDefinitionList().getList();

        for (int i = 0; i < scripts.size(); i++) {
            checkScript(scripts.get(i), scripts.subList(i, scripts.size()), procedures);
        }
        for (int i = 0; i < procedures.size(); i++) {
            checkProcedure(procedures.get(i), procedures.subList(i, procedures.size()));
        }
    }

    private void checkScript(Script script, List<Script> otherScripts, List<ProcedureDefinition> otherProcedures) {
        CloneAnalysis cloneAnalysis = new CloneAnalysis(currentActor);
        for (Script otherScript : otherScripts) {
            Set<CodeClone> clones = cloneAnalysis.check(script, otherScript, targetType);
            for (CodeClone clone : clones) {
                addIssue(getFirstCloneIssue(clone));
                addIssue(getSecondCloneIssue(clone));
            }
        }
        for (ProcedureDefinition procedure : otherProcedures) {
            Set<CodeClone> clones = cloneAnalysis.check(script, procedure, targetType);
            for (CodeClone clone : clones) {
                addIssue(getFirstCloneIssue(clone));
                addIssue(getSecondCloneIssue(clone));
            }
        }
    }

    private void checkProcedure(ProcedureDefinition procedure, List<ProcedureDefinition> otherProcedures) {
        CloneAnalysis cloneAnalysis = new CloneAnalysis(currentActor);
        for (ProcedureDefinition otherProcedure : otherProcedures) {
            Set<CodeClone> clones = cloneAnalysis.check(procedure, otherProcedure, targetType);
            for (CodeClone clone : clones) {
                addIssue(getFirstCloneIssue(clone));
                addIssue(getSecondCloneIssue(clone));
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return hintName;
    }

    public Issue getFirstCloneIssue(CodeClone clone) {
        if (clone.getFirstScript() instanceof Script)
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, (Script)clone.getFirstScript(), new ArrayList<>(clone.getFirstStatements()), clone.getFirstNode().getMetadata(), new Hint(hintName));
        else
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, (ProcedureDefinition) clone.getFirstScript(), new ArrayList<>(clone.getFirstStatements()), clone.getFirstNode().getMetadata(), new Hint(hintName));
    }

    public Issue getSecondCloneIssue(CodeClone clone) {
        if (clone.getSecondScript() instanceof Script)
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, (Script)clone.getSecondScript(), new ArrayList<>(clone.getSecondStatements()), clone.getFirstNode().getMetadata(), new Hint(hintName));
        else
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, (ProcedureDefinition) clone.getSecondScript(), new ArrayList<>(clone.getSecondStatements()), clone.getSecondNode().getMetadata(), new Hint(hintName));
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        if (!(first instanceof MultiBlockIssue) || !(other instanceof MultiBlockIssue)) {
            return false;
        }

        MultiBlockIssue mbIssue1 = (MultiBlockIssue)first;
        MultiBlockIssue mbIssue2 = (MultiBlockIssue)other;

        return compareNodes(mbIssue1.getNodes(), mbIssue2.getNodes());
    }

    protected boolean compareNodes(List<ASTNode> nodes1, List<ASTNode> nodes2) {
        List<Stmt> statements1 = new ArrayList<>();
        List<Stmt> statements2 = new ArrayList<>();
        for (ASTNode node : nodes1) {
            if (!(node instanceof Stmt)) {
                return false;
            }
            statements1.add((Stmt)node);
        }
        for (ASTNode node : nodes2) {
            if (!(node instanceof Stmt)) {
                return false;
            }
            statements2.add((Stmt)node);
        }

        return compareStatements(statements1, statements2);
    }

    protected boolean compareStatements(List<Stmt> statements1, List<Stmt> statements2) {
        return statements1.equals(statements2);
    }
}
