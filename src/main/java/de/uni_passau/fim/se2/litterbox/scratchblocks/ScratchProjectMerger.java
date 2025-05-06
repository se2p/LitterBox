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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.SetStmtListReplacementVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScratchProjectMerger {

    public Program updateProject(Program baseProject, String actorName, ActorContent actorContent) {
        baseProject = addNewFields(actorContent, baseProject);
        addNewProcedureInfo(actorContent.procedures().getList(), baseProject.getProcedureMapping(), actorName);

        List<Script> newScripts = new ArrayList<>(actorContent.scripts().getScriptList());
        newScripts.addAll(AstNodeUtil.findActorByName(baseProject, actorName).getScripts().getScriptList());

        List<ProcedureDefinition> newProcedures = new ArrayList<>(actorContent.procedures().getList());
        newProcedures.addAll(
                AstNodeUtil.findActorByName(baseProject, actorName).getProcedureDefinitionList().getList()
        );

        final NodeReplacementVisitor scriptsReplacementVisitor = new NodeReplacementVisitor(
                AstNodeUtil.findActorByName(baseProject, actorName).getScripts(),
                new ScriptList(List.copyOf(newScripts))
        );

        Program newExtendedProject = (Program) baseProject.accept(scriptsReplacementVisitor);

        final NodeReplacementVisitor procedureReplacementVisitor = new NodeReplacementVisitor(
                AstNodeUtil.findActorByName(newExtendedProject, actorName).getProcedureDefinitionList(),
                new ProcedureDefinitionList(List.copyOf(newProcedures))
        );

        return (Program) newExtendedProject.accept(procedureReplacementVisitor);
    }

    public Program updateProjectInfo(Program baseProject, String actorName, ScriptEntity newScript) {
        Program extended = addNewFields(newScript, baseProject);
        if (newScript instanceof ProcedureDefinition procedureDefinition) {
            addNewProcedureInfo(procedureDefinition, extended.getProcedureMapping(), actorName);
        }
        return extended;
    }

    void addNewProcedureInfo(
            List<ProcedureDefinition> procedures, ProcedureDefinitionNameMapping procedureMapping, String actorName
    ) {
        for (ProcedureDefinition procedure : procedures) {
            addNewProcedureInfo(procedure, procedureMapping, actorName);
        }
    }

    void addNewProcedureInfo(
            ProcedureDefinition procedure, ProcedureDefinitionNameMapping procedureMapping, String actorName
    ) {
        procedureMapping.addProcedure(
                procedure.getIdent(), actorName, procedure.getIdent().getName(), procedure.getParameterDefinitionList()
        );
    }

    Program addNewFields(ScriptEntity additionalContent, Program extendedProject) {
        extendedProject = addQualifiedDataExpressions(additionalContent, extendedProject);
        addMessages(additionalContent, extendedProject);
        return extendedProject;
    }

    Program addNewFields(ActorContent additionalContent, Program extendedProject) {
        for (Script script : additionalContent.scripts().getScriptList()) {
            extendedProject = addNewFields(script, extendedProject);
        }
        for (ProcedureDefinition procedure : additionalContent.procedures().getList()) {
            extendedProject = addNewFields(procedure, extendedProject);
        }
        return extendedProject;
    }

    void addMessages(ScriptEntity script, Program extendedProject) {
        List<Message> messages = new ArrayList<>();
        messages.addAll(NodeFilteringVisitor.getBlocks(script, Message.class));

        SymbolTable symbolTable = extendedProject.getSymbolTable();

        for (Message message : messages) {
            if (message.getMessage() instanceof StringLiteral text
                    && symbolTable.getMessage(text.getText()).isEmpty()) {
                symbolTable.addMessage(text.getText(), message, true, "Stage", CloneVisitor.generateUID());
            }
        }
    }

    Program addQualifiedDataExpressions(ScriptEntity script, Program extendedProject) {
        List<Qualified> qualifieds = new ArrayList<>();
        qualifieds.addAll(NodeFilteringVisitor.getBlocks(script, Qualified.class));

        SymbolTable symbolTable = extendedProject.getSymbolTable();
        for (Qualified qualified : qualifieds) {
            String actorInQualified = qualified.getFirst().getName();
            DataExpr data = qualified.getSecond();
            if (data instanceof Variable variable) {
                String varName = variable.getName().getName();
                if (symbolTable.getVariableIdentifierFromActorAndName(actorInQualified, varName) == null
                        && symbolTable.getVariableIdentifierFromActorAndName("Stage", varName) == null
                ) {
                    String uid = CloneVisitor.generateUID();
                    symbolTable.addVariable(uid, varName, new NumberType(), false, actorInQualified);
                    ActorDefinition actor = AstNodeUtil.findActorByName(extendedProject, actorInQualified);
                    List<SetStmt> setStmts = new ArrayList<>(actor.getSetStmtList().getStmts());
                    setStmts.add(new SetVariableTo(qualified, new NumberLiteral(0), new NoBlockMetadata()));
                    SetStmtList setStmtList = new SetStmtList(Collections.unmodifiableList(setStmts));
                    final SetStmtListReplacementVisitor setStmtRelacementVisitor = new SetStmtListReplacementVisitor(
                            actor.getSetStmtList(),
                            setStmtList
                    );
                    extendedProject = (Program) extendedProject.accept(setStmtRelacementVisitor);
                }
            }
            if (data instanceof ScratchList variable) {
                String varName = variable.getName().getName();
                if (symbolTable.getListIdentifierFromActorAndName(actorInQualified, varName) == null
                        && symbolTable.getListIdentifierFromActorAndName("Stage", varName) == null
                ) {
                    String uid = CloneVisitor.generateUID();
                    symbolTable.addExpressionListInfo(
                            uid, varName, new ExpressionList(new ArrayList<>()), false, actorInQualified
                    );
                    ActorDefinition actor = AstNodeUtil.findActorByName(extendedProject, actorInQualified);
                    List<SetStmt> setStmts = new ArrayList<>(actor.getSetStmtList().getStmts());
                    setStmts.add(new SetVariableTo(qualified, new ExpressionList(new ArrayList<>()), new NoBlockMetadata()));
                    SetStmtList setStmtList = new SetStmtList(Collections.unmodifiableList(setStmts));
                    final SetStmtListReplacementVisitor setStmtRelacementVisitor = new SetStmtListReplacementVisitor(
                            actor.getSetStmtList(),
                            setStmtList
                    );
                    extendedProject = (Program) extendedProject.accept(setStmtRelacementVisitor);
                }
            }
        }
        return extendedProject;
    }
}
