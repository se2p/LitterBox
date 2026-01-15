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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterOutOfScope;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the {@link ParameterOutOfScope} bug.
 *
 * <p>
 * This injector finds statements/expressions in scripts that accept expressions
 * (like MoveSteps or comparisons), and replaces an expression with a
 * parameter reference from a procedure, causing the parameter to be used out
 * of its defining scope.
 *
 * <p>
 * Transformation examples:
 * <pre>
 * Before: define myProc (param)
 *           say (param)
 *         when green flag clicked
 *           move (10) steps
 *           if &lt;(answer) = [yes]&gt; then ...
 *
 * After:  define myProc (param)
 *           say (param)
 *         when green flag clicked
 *           move (param) steps     &lt;-- param is out of scope!
 *           if &lt;(param) = [yes]&gt; then ...  &lt;-- param is out of scope!
 * </pre>
 */
public class ParameterOutOfScopeInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ParameterOutOfScope();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ActorDefinition actorDefinition) {
        // Find a parameter from any procedure in this actor
        ParameterDefinition paramDef = findFirstParameter(actorDefinition);
        if (paramDef == null) {
            return; // No procedures with parameters
        }

        String paramName = paramDef.getIdent().getName();

        // Find nodes in scripts that have replaceable expressions
        List<ASTNode> targetNodes = findReplaceableNodes(actorDefinition);

        for (ASTNode node : targetNodes) {
            replaceNodeWith(node, paramName);
        }
    }

    /**
     * Finds the first parameter definition from any procedure in the actor.
     */
    private ParameterDefinition findFirstParameter(ActorDefinition actorDef) {
        for (ProcedureDefinition proc : actorDef.getProcedureDefinitionList().getList()) {
            if (!proc.getParameterDefinitionList().getParameterDefinitions().isEmpty()) {
                return proc.getParameterDefinitionList().getParameterDefinitions().getFirst();
            }
        }
        return null;
    }

    /**
     * Finds nodes with replaceable expressions in scripts.
     */
    private List<ASTNode> findReplaceableNodes(ActorDefinition actorDef) {
        List<ASTNode> nodes = new ArrayList<>();

        for (Script script : actorDef.getScripts().getScriptList()) {
            NodeFinder finder = new NodeFinder();
            script.accept(finder);
            nodes.addAll(finder.nodes);
        }
        return nodes;
    }

    /**
     * Creates a replacement for the given node, using a parameter.
     */
    private void replaceNodeWith(ASTNode node, String paramName) {
        if (node instanceof MoveSteps moveSteps) {
            replaceNodeImmutably(moveSteps, original -> {
                AsNumber paramAsNum = new AsNumber(createParameter(paramName));
                return new MoveSteps(paramAsNum, original.getMetadata());
            });
        } else if (node instanceof Equals equals) {
            replaceNodeImmutably(equals, original -> {
                AsString paramAsStr = new AsString(createParameter(paramName));
                return new Equals(paramAsStr, original.getOperand2(), original.getMetadata());
            });
        } else if (node instanceof BiggerThan biggerThan) {
            replaceNodeImmutably(biggerThan, original -> {
                AsString paramAsStr = new AsString(createParameter(paramName));
                return new BiggerThan(paramAsStr, original.getOperand2(), original.getMetadata());
            });
        } else if (node instanceof LessThan lessThan) {
            replaceNodeImmutably(lessThan, original -> {
                AsString paramAsStr = new AsString(createParameter(paramName));
                return new LessThan(paramAsStr, original.getOperand2(), original.getMetadata());
            });
        } else if (node instanceof SetXTo setXTo) {
            replaceNodeImmutably(setXTo, original -> {
                AsNumber paramAsNum = new AsNumber(createParameter(paramName));
                return new SetXTo(paramAsNum, original.getMetadata());
            });
        } else if (node instanceof SetYTo setYTo) {
            replaceNodeImmutably(setYTo, original -> {
                AsNumber paramAsNum = new AsNumber(createParameter(paramName));
                return new SetYTo(paramAsNum, original.getMetadata());
            });
        } else if (node instanceof PointInDirection pointIn) {
            replaceNodeImmutably(pointIn, original -> {
                AsNumber paramAsNum = new AsNumber(createParameter(paramName));
                return new PointInDirection(paramAsNum, original.getMetadata());
            });
        }
    }

    private Parameter createParameter(String paramName) {
        return new Parameter(
                new StrId(paramName),
                new StringType(),
                NonDataBlockMetadata.emptyNonBlockMetadata()
        );
    }

    /**
     * Visitor that finds nodes with replaceable expressions.
     */
    private static class NodeFinder implements ScratchVisitor {
        final List<ASTNode> nodes = new ArrayList<>();

        @Override
        public void visit(MoveSteps node) {
            nodes.add(node);
        }

        @Override
        public void visit(Equals node) {
            nodes.add(node);
        }

        @Override
        public void visit(BiggerThan node) {
            nodes.add(node);
        }

        @Override
        public void visit(LessThan node) {
            nodes.add(node);
        }

        @Override
        public void visit(SetXTo node) {
            nodes.add(node);
        }

        @Override
        public void visit(SetYTo node) {
            nodes.add(node);
        }

        @Override
        public void visit(PointInDirection node) {
            nodes.add(node);
        }
    }
}
