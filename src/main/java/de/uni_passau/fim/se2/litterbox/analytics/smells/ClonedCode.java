/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

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
        List<Script> scripts;
        if (ignoreLooseBlocks) {
            scripts = actor.getScripts().getScriptList().stream().filter(s -> !(s.getEvent() instanceof Never)).toList();
        } else {
            scripts = actor.getScripts().getScriptList();
        }
        List<ProcedureDefinition> procedures = actor.getProcedureDefinitionList().getList();
        for (int i = 0; i < scripts.size(); i++) {
            checkScript(scripts.get(i), scripts.subList(i, scripts.size()), procedures);
        }
        for (int i = 0; i < procedures.size(); i++) {
            checkProcedure(procedures.get(i), procedures.subList(i, procedures.size()));
        }
    }

    protected void addIssue(MultiBlockIssue issue) {
        for (Issue otherIssue : issues) {
            if (otherIssue instanceof MultiBlockIssue otherMultiBlockIssue) {
                if (issue.getCodeLocation() == otherMultiBlockIssue.getCodeLocation()
                        && issue.getScript() == otherMultiBlockIssue.getScript()
                        && issue.getNodes().equals(otherMultiBlockIssue.getNodes())) {
                    return;
                }
            }
        }
        super.addIssue(issue);
    }

    private void checkScript(Script script, List<Script> otherScripts, List<ProcedureDefinition> otherProcedures) {
        CloneAnalysis cloneAnalysis = new CloneAnalysis();
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
        CloneAnalysis cloneAnalysis = new CloneAnalysis();
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

    public MultiBlockIssue getFirstCloneIssue(CodeClone clone) {
        final List<ASTNode> statements = new ArrayList<>(clone.getFirstStatements());

        if (clone.getFirstScript() instanceof Script script) {
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, script, statements,
                    clone.getFirstNode().getMetadata(), new Hint(hintName));
        } else {
            final ProcedureDefinition procedure = (ProcedureDefinition) clone.getFirstScript();
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, procedure, statements,
                    clone.getFirstNode().getMetadata(), new Hint(hintName));
        }
    }

    public MultiBlockIssue getSecondCloneIssue(CodeClone clone) {
        final List<ASTNode> statements = new ArrayList<>(clone.getSecondStatements());

        if (clone.getSecondScript() instanceof Script script) {
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, script, statements,
                    clone.getFirstNode().getMetadata(), new Hint(hintName));
        } else {
            final ProcedureDefinition procedure = (ProcedureDefinition) clone.getSecondScript();
            return new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, procedure, statements,
                    clone.getSecondNode().getMetadata(), new Hint(hintName));
        }
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

        if (!(first instanceof MultiBlockIssue mbIssue1) || !(other instanceof MultiBlockIssue mbIssue2)) {
            return false;
        }

        return compareNodes(mbIssue1, mbIssue2);
    }

    protected boolean compareNodes(MultiBlockIssue issue1, MultiBlockIssue issue2) {
        return issue1.getNodes().equals(issue2.getNodes());
    }
}
