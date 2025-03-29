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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableAsLiteral extends AbstractIssueFinder {

    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;
    private Set<String> variablesInScope = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        variablesInScope = new LinkedHashSet<>();
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(StringLiteral node) {
        if (currentScript == null && currentProcedure == null) {
            return;
        }

        String literal = node.getText();
        if (variablesInScope.contains(literal)) {
            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.HIGH).withMetadata(getMetadata(node));
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.HINT_VARIABLE, node.getText());
            builder = builder.withCurrentNode(getCurrentReferencableNode(node));

            Qualified qualified = new Qualified(currentActor.getIdent(), new Variable(new StrId(literal)));
            if (node.getParentNode() instanceof BoolExpr) {
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(node, qualified);
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                builder = builder.withRefactoring(refactoring);
            } else {
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(node, new AsString(qualified));
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                builder = builder.withRefactoring(refactoring);
            }
            // TODO: Check for NumberExpr?

            addIssue(builder.withHint(hint));
        }
    }

    private ASTNode getCurrentReferencableNode(ASTNode node) {
        if (node instanceof Program) {
            throw new IllegalArgumentException("should have found referencable node before Program node");
        }
        if (AstNodeUtil.getBlockId(node) != null) {
            return node;
        } else {
            return getCurrentReferencableNode(node.getParentNode());
        }
    }

    private Metadata getMetadata(ASTNode node) {
        if (node instanceof Program) {
            throw new IllegalArgumentException("should have found Metadata before Program node");
        }
        if (node.getMetadata() != null && !(node.getMetadata() instanceof NoBlockMetadata)) {
            return node.getMetadata();
        } else {
            return getMetadata(node.getParentNode());
        }
    }

    @Override
    public void visit(StrId node) {
        //no-op
    }

    @Override
    public void visit(Variable node) {
        // No-op
    }

    @Override
    public void visit(ScratchList node) {
        // No-op
    }

    @Override
    public void visit(Parameter node) {
        // No-op
    }

    @Override
    public void visit(Message node) {
        // No-op
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;

        // Only visit statements of procedure
        visit(node.getStmtList());

        currentProcedure = null;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;

        variablesInScope = varMap.values().stream()
                .filter(v -> v.global() || v.actor().equals(currentActor.getIdent().getName()))
                .map(VariableInfo::variableName)
                .collect(Collectors.toSet());
        variablesInScope.addAll(listMap.values().stream()
                .filter(v -> v.global() || v.actor().equals(currentActor.getIdent().getName()))
                .map(ExpressionListInfo::variableName)
                .collect(Collectors.toSet()));

        actor.getScripts().accept(this);
        actor.getProcedureDefinitionList().accept(this);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return "variable_as_literal";
    }
}
