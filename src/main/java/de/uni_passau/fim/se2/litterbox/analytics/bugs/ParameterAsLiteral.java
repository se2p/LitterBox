/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ParameterAsLiteral extends AbstractIssueFinder {

    private Set<String> parametersInScope = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        parametersInScope = new LinkedHashSet<>();
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;

        parametersInScope = node.getParameterDefinitionList().getParameterDefinitions().stream()
                .map(p -> p.getIdent().getName())
                .collect(Collectors.toSet());

        // Only visit statements of procedure
        visit(node.getStmtList());

        parametersInScope.clear();
        currentProcedure = null;
    }

    @Override
    public void visit(StringLiteral node) {
        if (currentProcedure == null) {
            return;
        }

        // Skip StringLiterals that are part of Parameter or ParameterDefinition nodes
        // These represent the parameter NAME itself, not a misuse of a literal
        if (isInsideParameterNode(node)) {
            return;
        }

        String literal = node.getText();
        if (parametersInScope.contains(literal)) {
            IssueBuilder builder = prepareIssueBuilder()
                    .withSeverity(IssueSeverity.HIGH)
                    .withMetadata(getMetadata(node));
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.HINT_VARIABLE, node.getText());
            builder = builder.withCurrentNode(getCurrentReferencableNode(node));
            addIssue(builder.withHint(hint));
        }
    }

    /**
     * Checks if the node is a descendant of a Parameter or ParameterDefinition node.
     * StringLiterals inside these nodes are the parameter NAME, not a misuse.
     */
    private boolean isInsideParameterNode(ASTNode node) {
        ASTNode current = node.getParentNode();
        while (current != null) {
            if (current instanceof Parameter
                    || current instanceof ParameterDefinition) {
                return true;
            }
            current = current.getParentNode();
        }
        return false;
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
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return "parameter_as_literal";
    }
}
