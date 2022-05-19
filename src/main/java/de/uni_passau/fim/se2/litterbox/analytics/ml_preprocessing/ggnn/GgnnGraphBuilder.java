package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GgnnGraphBuilder<T extends ASTNode> {
    private final T sprite;
    // private final ControlFlowGraph cfg;

    public GgnnGraphBuilder(final T astRoot) {
        this.sprite = astRoot;

        // ControlFlowGraphVisitor cfgVisitor = new ControlFlowGraphVisitor();
        // astRoot.accept(cfgVisitor);
        // this.cfg = cfgVisitor.getControlFlowGraph();
    }

    public GgnnProgramGraph.ContextGraph build() {
        final List<Pair<ASTNode>> childEdges = getEdges(new ChildEdgesVisitor());
        final List<Pair<ASTNode>> nextTokenEdges = getEdges(new NextTokenVisitor());
        final List<Pair<ASTNode>> guardedByEdges = getEdges(new GuardedByVisitor());
        final List<Pair<ASTNode>> computedFromEdges = getEdges(new ComputedFromVisitor());

        final List<ASTNode> allNodes = getAllNodes(childEdges, nextTokenEdges, guardedByEdges, computedFromEdges);
        final Map<ASTNode, Integer> nodeIndices = getNodeIndices(allNodes);

        final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = new EnumMap<>(GgnnProgramGraph.EdgeType.class);
        edges.put(GgnnProgramGraph.EdgeType.CHILD, getIndexedEdges(nodeIndices, childEdges));
        edges.put(GgnnProgramGraph.EdgeType.NEXT_TOKEN, getIndexedEdges(nodeIndices, nextTokenEdges));
        edges.put(GgnnProgramGraph.EdgeType.GUARDED_BY, getIndexedEdges(nodeIndices, guardedByEdges));
        edges.put(GgnnProgramGraph.EdgeType.COMPUTED_FROM, getIndexedEdges(nodeIndices, computedFromEdges));
        edges.put(GgnnProgramGraph.EdgeType.VARIABLE_USE, Collections.emptySet());

        final Map<Integer, String> nodeLabels = getNodeLabels(nodeIndices, getUsedLabels(edges));

        return new GgnnProgramGraph.ContextGraph(edges, nodeLabels, Map.of());
    }

    @SafeVarargs
    private List<ASTNode> getAllNodes(final List<Pair<ASTNode>>... nodes) {
        final List<ASTNode> allNodes = new ArrayList<>();
        Arrays.stream(nodes).flatMap(List::stream).flatMap(Pair::stream).forEach(node -> {
            if (allNodes.stream().noneMatch(n -> n == node)) {
                allNodes.add(node);
            }
        });
        return allNodes;
    }

    private Map<ASTNode, Integer> getNodeIndices(final List<ASTNode> nodes) {
        final Map<ASTNode, Integer> nodeIndices = new IdentityHashMap<>(nodes.size());
        int idx = 0;
        for (ASTNode node : nodes) {
            nodeIndices.put(node, idx++);
        }
        return nodeIndices;
    }

    private List<Pair<ASTNode>> getEdges(EdgesVisitor v) {
        v.visit(sprite);
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

    private Set<Integer> getUsedLabels(final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges) {
        return edges.values()
                .stream()
                .flatMap(Set::stream)
                .flatMap(Pair::stream)
                .collect(Collectors.toSet());
    }

    private Map<Integer, String> getNodeLabels(final Map<ASTNode, Integer> nodeIndices,
                                               final Set<Integer> usedIndices) {
        final Map<Integer, String> nodeLabels = new HashMap<>();

        for (Map.Entry<ASTNode, Integer> entry : nodeIndices.entrySet()) {
            ASTNode node = entry.getKey();
            Integer idx = entry.getValue();

            if (usedIndices.contains(idx)) {
                String label = StringUtil.replaceSpecialCharacters(StringUtil.getToken(node));
                nodeLabels.put(idx, label);
            }
        }

        return nodeLabels;
    }

    /*
    private Set<Pair<ASTNode>> getDefUseEdges() {
        final Set<Pair<ASTNode>> edges = new HashSet<>();

        for (EndpointPair<CFGNode> edge : new DataDependenceGraph(cfg).getEdges()) {
            edges.add(Pair.of(edge.source().getASTNode(), edge.target().getASTNode()));
        }

        return edges;
    }
     */

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

        @Override
        public void visit(CommentMetadataList node) {
            // intentionally empty
        }

        @Override
        public void visit(ImageMetadataList node) {
            // intentionally empty
        }

        @Override
        public void visit(MonitorMetadataList node) {
            // intentionally empty
        }

        @Override
        public void visit(MonitorParamMetadataList node) {
            // intentionally empty
        }

        @Override
        public void visit(SoundMetadataList node) {
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
}
