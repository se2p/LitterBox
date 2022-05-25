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

import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Map;
import java.util.Set;

class GgnnProgramGraph {
    private final String filename;
    private final String label;
    private final ContextGraph contextGraph;

    public GgnnProgramGraph(String filename, String label, ContextGraph contextGraph) {
        this.filename = filename;
        this.label = label;
        this.contextGraph = contextGraph;
    }

    public String getFilename() {
        return filename;
    }

    public String getLabel() {
        return label;
    }

    public ContextGraph getContextGraph() {
        return contextGraph;
    }

    static class ContextGraph {
        private final Map<EdgeType, Set<Pair<Integer>>> edges;
        private final Map<Integer, String> nodeLabels;
        private final Map<Integer, String> nodeTypes;

        public ContextGraph(Map<EdgeType, Set<Pair<Integer>>> edges, Map<Integer, String> nodeLabels,
                            Map<Integer, String> nodeTypes) {
            for (EdgeType edgeType : EdgeType.values()) {
                Preconditions.checkArgument(edges.containsKey(edgeType),
                        "The context graph is missing edges of type %s!", edgeType);
            }

            this.edges = edges;
            this.nodeLabels = nodeLabels;
            this.nodeTypes = nodeTypes;
        }

        public Map<EdgeType, Set<Pair<Integer>>> getEdges() {
            return edges;
        }

        public Set<Pair<Integer>> getEdges(EdgeType edgeType) {
            return edges.get(edgeType);
        }

        public Map<Integer, String> getNodeLabels() {
            return nodeLabels;
        }

        public Map<Integer, String> getNodeTypes() {
            return nodeTypes;
        }
    }

    enum EdgeType {
        /**
         * Links a parent to its children.
         */
        CHILD,
        /**
         * Links each token to the following one.
         */
        NEXT_TOKEN,
        /**
         * Links nodes with data dependencies.
         */
        DATA_DEPENDENCY,
        /**
         * Links all variables and attributes on the right-hand side of an assignment to the variable on the left.
         */
        COMPUTED_FROM,
        /**
         * Links variables and attributes occurring in an if-condition to their uses in the then- and else-blocks.
         */
        GUARDED_BY,
        /**
         * Links arguments passed into custom blocks to the parameter definition.
         */
        PARAMETER_PASSING,
    }
}
