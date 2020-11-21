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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;

abstract class ClonedCode extends AbstractIssueFinder {

    private CodeClone.CloneType targetType;

    private String hintKey;

    protected ClonedCode(CodeClone.CloneType targetType, String hintKey) {
        this.targetType = targetType;
        this.hintKey = hintKey;
    }

    @Override
    public void visit(ActorDefinition actor) {
        // TODO: Don't need these?
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());

        List<Script> scripts = actor.getScripts().getScriptList();
        List<ProcedureDefinition> procedures = actor.getProcedureDefinitionList().getList();

        // TODO: Enable comparision of scripts with themself
        for (int i = 0; i < scripts.size(); i++) {
            checkScript(scripts.get(i), scripts.subList(i + 1, scripts.size()), procedures);
        }
        for (int i = 0; i < procedures.size(); i++) {
            checkProcedure(procedures.get(i), procedures.subList(i + 1, procedures.size()));
        }
    }

    private void checkScript(Script script, List<Script> otherScripts, List<ProcedureDefinition> otherProcedures) {
        CloneAnalysis cloneAnalysis = new CloneAnalysis(currentActor);
        for (Script otherScript : otherScripts) {
            Set<CodeClone> clones = cloneAnalysis.check(script, otherScript, targetType);
            for (CodeClone clone : clones) {
                addIssue(clone.getFirstNode(), clone.getFirstNode().getMetadata(), IssueSeverity.MEDIUM, new Hint(hintKey));
            }
        }
        for (ProcedureDefinition procedure : otherProcedures) {
            Set<CodeClone> clones = cloneAnalysis.check(script, procedure, targetType);
            for (CodeClone clone : clones) {
                addIssue(clone.getFirstNode(), clone.getFirstNode().getMetadata(), IssueSeverity.MEDIUM, new Hint(hintKey));
            }
        }
    }

    private void checkProcedure(ProcedureDefinition procedure, List<ProcedureDefinition> otherProcedures) {
        CloneAnalysis cloneAnalysis = new CloneAnalysis(currentActor);
        for (ProcedureDefinition otherProcedure : otherProcedures) {
            Set<CodeClone> clones = cloneAnalysis.check(procedure, otherProcedure, targetType);
            for (CodeClone clone : clones) {
                addIssue(clone.getFirstNode(), clone.getFirstNode().getMetadata(), IssueSeverity.MEDIUM, new Hint(""));
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return hintKey;
    }
}
