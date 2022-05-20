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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GgnnGraphBuilder {
    private final Program program;
    private final ASTNode astRoot;

    public GgnnGraphBuilder(final Program program, final ASTNode astRoot) {
        Preconditions.checkAllArgsNotNull(List.of(program, astRoot));
        Preconditions.checkArgument(astRoot instanceof Program || astRoot instanceof ActorDefinition,
                "Ggnn graphs can only be built either for entire programs or for actors.");

        this.program = program;
        this.astRoot = astRoot;
    }

    public GgnnProgramGraph.ContextGraph build() {
        final List<Pair<ASTNode>> childEdges = getEdges(new ChildEdgesVisitor());
        final List<Pair<ASTNode>> nextTokenEdges = getEdges(new NextTokenVisitor());
        final List<Pair<ASTNode>> guardedByEdges = getEdges(new GuardedByVisitor());
        final List<Pair<ASTNode>> computedFromEdges = getEdges(new ComputedFromVisitor());
        final List<Pair<ASTNode>> parameterPassingEdges = getEdges(new ParameterPassingVisitor(program));
        final List<Pair<ASTNode>> variableDataDependencies = getVariableDataDependencies();

        final Set<ASTNode> allNodes = getAllNodes(childEdges, nextTokenEdges, guardedByEdges, computedFromEdges,
                parameterPassingEdges, variableDataDependencies);
        final Map<ASTNode, Integer> nodeIndices = getNodeIndices(allNodes);

        final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = new EnumMap<>(GgnnProgramGraph.EdgeType.class);
        edges.put(GgnnProgramGraph.EdgeType.CHILD, getIndexedEdges(nodeIndices, childEdges));
        edges.put(GgnnProgramGraph.EdgeType.NEXT_TOKEN, getIndexedEdges(nodeIndices, nextTokenEdges));
        edges.put(GgnnProgramGraph.EdgeType.GUARDED_BY, getIndexedEdges(nodeIndices, guardedByEdges));
        edges.put(GgnnProgramGraph.EdgeType.COMPUTED_FROM, getIndexedEdges(nodeIndices, computedFromEdges));
        edges.put(GgnnProgramGraph.EdgeType.VARIABLE_USE, getIndexedEdges(nodeIndices, variableDataDependencies));
        edges.put(GgnnProgramGraph.EdgeType.PARAMETER_PASSING, getIndexedEdges(nodeIndices, parameterPassingEdges));

        Set<Integer> usedNodes = getUsedNodes(edges);
        final Map<Integer, String> nodeLabels = getNodeLabels(nodeIndices, usedNodes);
        final Map<Integer, String> nodeTypes = getNodeTypes(nodeIndices, usedNodes);

        return new GgnnProgramGraph.ContextGraph(edges, nodeLabels, nodeTypes);
    }

    @SafeVarargs
    private Set<ASTNode> getAllNodes(final List<Pair<ASTNode>>... nodes) {
        // identity hash set instead of regular set, as variable nodes with the same name have the same hash code
        final Supplier<Set<ASTNode>> allNodesSet = () -> Collections.newSetFromMap(new IdentityHashMap<>());
        return Arrays.stream(nodes)
                .flatMap(List::stream)
                .flatMap(Pair::stream)
                .collect(Collectors.toCollection(allNodesSet));
    }

    private Map<ASTNode, Integer> getNodeIndices(final Collection<ASTNode> nodes) {
        final Map<ASTNode, Integer> nodeIndices = new IdentityHashMap<>(nodes.size());
        int idx = 0;
        for (ASTNode node : nodes) {
            nodeIndices.put(node, idx++);
        }
        return nodeIndices;
    }

    private List<Pair<ASTNode>> getEdges(EdgesVisitor v) {
        v.visit(astRoot);
        return v.getEdges();
    }

    private Set<Pair<Integer>> getIndexedEdges(final Map<ASTNode, Integer> nodeIndices,
                                               final List<Pair<ASTNode>> edges) {
        return edges.stream().map(edge -> {
            Integer idxFrom = nodeIndices.get(edge.getFst());
            Integer idxTo = nodeIndices.get(edge.getSnd());
            return Pair.of(idxFrom, idxTo);
        }).collect(Collectors.toSet());
    }

    private Set<Integer> getUsedNodes(final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges) {
        return edges.values()
                .stream()
                .flatMap(Set::stream)
                .flatMap(Pair::stream)
                .collect(Collectors.toSet());
    }

    private Map<Integer, String> getNodeLabels(final Map<ASTNode, Integer> nodeIndices,
                                               final Set<Integer> usedIndices) {
        return getNodeInformation(nodeIndices, usedIndices, StringUtil::getToken);
    }

    private Map<Integer, String> getNodeTypes(final Map<ASTNode, Integer> nodeIndices, final Set<Integer> usedIndices) {
        return getNodeInformation(nodeIndices, usedIndices, node -> node.getClass().getSimpleName());
    }

    private Map<Integer, String> getNodeInformation(final Map<ASTNode, Integer> nodeMap, final Set<Integer> usedIndices,
                                                    final Function<ASTNode, String> infoExtractor) {
        final Map<Integer, String> nodeLabels = new HashMap<>();

        for (Map.Entry<ASTNode, Integer> entry : nodeMap.entrySet()) {
            ASTNode node = entry.getKey();
            Integer idx = entry.getValue();

            if (usedIndices.contains(idx)) {
                String label = infoExtractor.apply(node);
                nodeLabels.put(idx, label);
            }
        }

        return nodeLabels;
    }

    private List<Pair<ASTNode>> getVariableDataDependencies() {
        if (astRoot instanceof Program) {
            return ((Program) astRoot).getActorDefinitionList().getDefinitions()
                    .stream()
                    .flatMap(this::getVariableDataDependencies)
                    .collect(Collectors.toList());
        } else if (astRoot instanceof ActorDefinition) {
            return getVariableDataDependencies((ActorDefinition) astRoot).collect(Collectors.toList());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Stream<Pair<ASTNode>> getVariableDataDependencies(final ActorDefinition actor) {
        ControlFlowGraphVisitor v = new ControlFlowGraphVisitor(program, actor);
        actor.accept(v);
        ControlFlowGraph cfg = v.getControlFlowGraph();

        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        return ddg.getEdges().stream()
                .filter(edge -> hasVariableDataDependency(edge.source(), edge.target()))
                .map(edge -> Pair.of(edge.source().getASTNode(), edge.target().getASTNode()));
    }

    private boolean hasVariableDataDependency(final CFGNode source, final CFGNode target) {
        Set<Identifier> sourceIdentifiers = variableIdentifiers(source);
        Set<Identifier> targetIdentifiers = variableIdentifiers(target);
        return sourceIdentifiers.stream().anyMatch(targetIdentifiers::contains);
    }

    private Set<Identifier> variableIdentifiers(final CFGNode node) {
        Stream<Defineable> definitions = node.getDefinitions().stream().map(Definition::getDefinable);
        Stream<Defineable> uses = node.getUses().stream().map(Use::getDefinable);

        return Stream.concat(definitions, uses)
                .filter(de.uni_passau.fim.se2.litterbox.cfg.Variable.class::isInstance)
                .map(v -> ((de.uni_passau.fim.se2.litterbox.cfg.Variable) v).getIdentifier())
                .collect(Collectors.toSet());
    }

    private abstract static class EdgesVisitor implements ScratchVisitor {
        protected final List<Pair<ASTNode>> edges = new ArrayList<>();

        List<Pair<ASTNode>> getEdges() {
            return edges;
        }

        @Override
        public void visitChildren(ASTNode node) {
            for (ASTNode child : node.getChildren()) {
                if (!AstNodeUtil.isMetadata(node)) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(DeclarationStmtList node) {
            // intentionally empty
        }

        @Override
        public void visit(SetStmtList node) {
            // intentionally empty
        }

        @Override
        public void visit(Metadata node) {
            // intentionally empty
        }

        protected Stream<? extends ASTNode> childrenWithoutMetadata(final ASTNode node) {
            return node.getChildren().stream().filter(c -> !AstNodeUtil.isMetadata(c));
        }
    }

    private static class ChildEdgesVisitor extends EdgesVisitor {
        @Override
        public void visit(ASTNode node) {
            childrenWithoutMetadata(node).forEach(child -> edges.add(Pair.of(node, child)));
            super.visit(node);
        }
    }

    private static class NextTokenVisitor extends EdgesVisitor {
        @Override
        public void visit(ASTNode node) {
            List<? extends ASTNode> children = node.getChildren();
            for (int i = 0; i < children.size() - 1; ++i) {
                ASTNode curr = children.get(i);
                ASTNode next = children.get(i + 1);

                if (!AstNodeUtil.isMetadata(curr) && !AstNodeUtil.isMetadata(next)) {
                    edges.add(Pair.of(curr, next));
                }
            }

            super.visit(node);
        }
    }

    private static class GuardedByVisitor extends EdgesVisitor {
        @Override
        public void visit(IfElseStmt node) {
            final Map<String, List<Variable>> guards = VariableUsesVisitor.getVariables(node.getBoolExpr());
            final Map<String, List<Variable>> thenBlockVars = VariableUsesVisitor.getVariables(node.getThenStmts());
            final Map<String, List<Variable>> elseBlockVars = VariableUsesVisitor.getVariables(node.getElseStmts());

            connectVars(node.getBoolExpr(), guards, thenBlockVars);
            connectVars(node.getBoolExpr(), guards, elseBlockVars);
        }

        @Override
        public void visit(IfThenStmt node) {
            final Map<String, List<Variable>> guards = VariableUsesVisitor.getVariables(node.getBoolExpr());
            final Map<String, List<Variable>> thenStmtVars = VariableUsesVisitor.getVariables(node.getThenStmts());

            connectVars(node.getBoolExpr(), guards, thenStmtVars);
        }

        private void connectVars(final BoolExpr guardExpression, final Map<String, List<Variable>> guards,
                                 final Map<String, List<Variable>> inBlock) {
            for (Map.Entry<String, List<Variable>> usedVar : inBlock.entrySet()) {
                if (!guards.containsKey(usedVar.getKey())) {
                    continue;
                }

                for (Variable v : usedVar.getValue()) {
                    edges.add(Pair.of(v, guardExpression));
                }
            }
        }
    }

    private static class ComputedFromVisitor extends EdgesVisitor {
        @Override
        public void visit(ChangeVariableBy node) {
            if (node.getIdentifier() instanceof Qualified) {
                addEdges((Qualified) node.getIdentifier(), node.getExpr());
            }
        }

        @Override
        public void visit(SetVariableTo node) {
            if (node.getIdentifier() instanceof Qualified) {
                addEdges((Qualified) node.getIdentifier(), node.getExpr());
            }
        }

        private void addEdges(final Qualified assignTo, final ASTNode expr) {
            Map<String, List<Variable>> computedFrom = VariableUsesVisitor.getVariables(expr);

            computedFrom.values()
                    .stream()
                    .flatMap(List::stream)
                    .forEach(variable -> edges.add(Pair.of(assignTo.getSecond(), variable)));
        }
    }

    private static class VariableUsesVisitor implements ScratchVisitor {
        private final Map<String, List<Variable>> variables = new HashMap<>();

        @Override
        public void visit(Variable node) {
            variables.compute(node.getName().getName(), (name, vars) -> {
                if (vars == null) {
                    vars = new ArrayList<>();
                }
                vars.add(node);
                return vars;
            });

            ScratchVisitor.super.visit(node);
        }

        public static Map<String, List<Variable>> getVariables(final ASTNode node) {
            VariableUsesVisitor v = new VariableUsesVisitor();
            node.accept(v);
            return v.variables;
        }
    }

    private static class ParameterPassingVisitor extends EdgesVisitor {
        private final ProcedureDefinitionNameMapping procedureMapping;
        private final Map<LocalIdentifier, ProcedureDefinition> procedures = new IdentityHashMap<>();

        ParameterPassingVisitor(final Program program) {
            this.procedureMapping = program.getProcedureMapping();
        }

        @Override
        public void visit(ProcedureDefinitionList node) {
            for (ProcedureDefinition procedureDefinition : node.getList()) {
                procedures.put(procedureDefinition.getIdent(), procedureDefinition);
            }
            super.visit(node);
        }

        @Override
        public void visit(CallStmt node) {
            List<Expression> passedArguments = node.getExpressions().getExpressions();
            String sprite = getCurrentSprite(node);
            String procedureName = node.getIdent().getName();

            procedureMapping.getProceduresForName(sprite, procedureName)
                    .stream()
                    .filter(p -> {
                        int acceptingArgumentCount = p.getValue().getArguments().length;
                        return passedArguments.size() == acceptingArgumentCount;
                    })
                    .map(org.apache.commons.lang3.tuple.Pair::getKey)
                    .map(procedures::get)
                    .map(procedure -> procedure.getParameterDefinitionList().getParameterDefinitions())
                    .findFirst()
                    .ifPresent(parameters -> {
                        for (int i = 0; i < passedArguments.size(); ++i) {
                            edges.add(Pair.of(passedArguments.get(i), parameters.get(i)));
                        }
                    });
        }

        private String getCurrentSprite(final ASTNode node) {
            ASTNode actorDefinition = node;
            while (!(actorDefinition instanceof ActorDefinition)) {
                actorDefinition = actorDefinition.getParentNode();
            }

            return ((ActorDefinition) actorDefinition).getIdent().getName();
        }
    }
}
