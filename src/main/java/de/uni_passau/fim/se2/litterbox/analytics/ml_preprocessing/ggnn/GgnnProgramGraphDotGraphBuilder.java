package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GgnnProgramGraphDotGraphBuilder {
    private GgnnProgramGraphDotGraphBuilder() {
    }

    public static String asDotGraph(final List<GgnnProgramGraph> graphs, final String label) {
        final StringBuilder sb = new StringBuilder();
        int graphId = 1;
        sb.append("digraph ").append(label).append("{\n");
        for (GgnnProgramGraph graph : graphs) {
            sb.append(GgnnProgramGraphDotGraphBuilder.asDotGraph(graph, graphId, true));
            graphId += 1;
        }
        sb.append("}\n");

        return sb.toString();
    }

    public static String asDotGraph(final GgnnProgramGraph programGraph, int graphId, boolean isSubgraph) {
        return asDotGraph(programGraph.getContextGraph(), programGraph.getLabel(), graphId, isSubgraph);
    }

    private static String asDotGraph(final GgnnProgramGraph.ContextGraph graph, final String label, int graphId,
                                     boolean isSubgraph) {
        StringBuilder sb = new StringBuilder();

        if (isSubgraph) {
            sb.append("subgraph ");
        } else {
            sb.append("digraph ");
        }
        sb.append(label).append(" {\n");

        for (Map.Entry<Integer, String> node : graph.getNodeLabels().entrySet()) {
            appendNode(sb, graphId, node.getKey());
            sb.append(" [label=\"").append(node.getValue()).append("\"];\n");
        }
        sb.append('\n');

        for (Map.Entry<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edgeGroup : graph.getEdges().entrySet()) {
            String edgeColor = getEdgeColor(edgeGroup.getKey());

            for (Pair<Integer> edge : edgeGroup.getValue()) {
                appendNode(sb, graphId, edge.getFst());
                sb.append(" -> ");
                appendNode(sb, graphId, edge.getSnd());
                sb.append(' ').append(edgeColor).append(";\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private static void appendNode(final StringBuilder sb, int graphId, int nodeId) {
        sb.append(graphId).append('.').append(nodeId);
    }

    private static String getEdgeColor(GgnnProgramGraph.EdgeType edgeType) {
        switch (edgeType) {
            case CHILD:
                return "[color=black]";
            case NEXT_TOKEN:
                return "[color=blue]";
            case VARIABLE_USE:
                return "[color=green]";
            case COMPUTED_FROM:
                return "[color=orange]";
            case GUARDED_BY:
                return "[color=maroon]";
            default:
                return "[color=red]";
        }
    }
}
