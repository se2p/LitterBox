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

import com.google.common.graph.EndpointPair;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class GraphVisitor implements ScratchVisitor, PenExtensionVisitor {
    //Variables
    long counter = 0;
    String backwardEdges = "";
    Integer countIndexOfVertices = 0;
    //Objects

    Map<String, List<String>> astEdges = new HashMap<>();
    Map<String, List<String>> siblingEdges = new HashMap<>();
    Map<String, List<String>> nextTokenEdges = new HashMap<>();
    Map<String, List<String>> nextUseEdges = new HashMap<>();

    List<String> terminalNodes = new ArrayList<>();
    List<String> nodeLabels = new ArrayList<>();
    List<String> terminalVertices = new ArrayList<>();

    Map<String, String> vertexMap = new HashMap<>();
    Map<String, String> vertexMapForFlowEdges = new HashMap<>();
    Map<String, String> vertexMapForDotString = new HashMap<>();
    List<Triple<String, String, String>> verticesList = new ArrayList<>();

    List<String> vertices = new ArrayList<>();

    @Override
    public void visit(ASTNode node) {
        List<String> vertexList = new ArrayList<>();
        if (node instanceof ASTLeaf) {
            recordLeaf((ASTLeaf) node);
        } else {
            String name = String.valueOf(node.hashCode()); //This should only be a workaround this is a hack
            verticesList.add(Triple.of(String.valueOf(countIndexOfVertices), name, node.getUniqueName()));
            countIndexOfVertices++;

            for (ASTNode child : node.getChildren()) {
                astEdges.computeIfAbsent(name, k -> new ArrayList<>()).add(String.valueOf(child.hashCode()));
                vertexList.add(String.valueOf(child.hashCode()));
            }

            addSiblingEdges(vertexList, 0);
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    public void recordLeaf(ASTLeaf node) {
        List<String> vertexList = new ArrayList<>();
        String name = String.valueOf(node.hashCode());

        verticesList.add(Triple.of(String.valueOf(countIndexOfVertices), name, node.getUniqueName()));
        countIndexOfVertices++;

        String[] simpleStrings = node.toSimpleStringArray();
        String uniqueId = "";

        if (simpleStrings.length == 0) {
            terminalVertices.add(name);
        }

        for (String simpleString : simpleStrings) {
            counter++;
            uniqueId = UUID.randomUUID().toString().replace("-", "").replaceAll("\\D+", "");
            astEdges.computeIfAbsent(name, k -> new ArrayList<>()).add(uniqueId);
            vertexList.add(uniqueId);
            terminalVertices.add(uniqueId);

            verticesList.add(Triple.of(String.valueOf(countIndexOfVertices), uniqueId, simpleString));
            countIndexOfVertices++;
        }
        addSiblingEdges(vertexList, 0);
    }

    public void initialize() {
        backwardEdges = "";
        // Index, hash code and name
        for (Triple<String, String, String> triplet : verticesList) {
            vertices.add(triplet.getMiddle());//hash code
            nodeLabels.add(triplet.getRight());//name
            vertexMap.put(triplet.getMiddle(), triplet.getLeft());//hash code and index
            vertexMapForFlowEdges.put(triplet.getRight(), triplet.getMiddle());//name and hash code
            vertexMapForDotString.put(triplet.getMiddle(), triplet.getRight());//hash code and name
        }
    }

    public void toDotString(ControlFlowGraph cfg) {
        backwardEdges = "";
        System.out.println(getBuilderInDotString(cfg));
        System.out.println("}");
    }

    //StringBuilder in dotString
    public String getBuilderInDotString(ControlFlowGraph cfg) {
        StringBuilder builder = new StringBuilder();
        List<String> terminalVertexList = new ArrayList<>(new HashSet<>(terminalVertices));
        initialize();
        builder.append("digraph G {");
        builder.append("\n");
        builder.append("\t shape=rectangle");
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(astEdges, 0, true, false));
        builder.append("\n");
        builder.append(getEdgesForGNN(astEdges, 1, true, false));
        builder.append("\t");
        builder.append(getEdgesForGNN(siblingEdges, 0, true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(checkAndAddNextTokenEdges(terminalVertexList), 0, true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(getFlowEdges(getNextUseEdges(cfg)), 0, true, false));
        builder.append("\n");
        builder.append("\t");
        builder.append(getEdgesForGNN(getFlowEdges(cfg.getFlowEdges()), 0, true, true));
        builder.append("\n");
        builder.append("\t");
        builder.append(backwardEdges);
        builder.append("\n");
        builder.append("\t");
        builder.append("}");

        return builder.toString();
    }

    //Print Graph in console
    public String printGraph(String fileName, ControlFlowGraph controlFlowGraph, String correctLabel, String incorrectLabel, int spriteIndex, int lastIndex) {
        return createBuilderForGNN(fileName, controlFlowGraph, correctLabel, incorrectLabel, spriteIndex, lastIndex);
    }

    //String Builder for generating embeddings in string
    private String createBuilderForGNN(String fileName, ControlFlowGraph controlFlowGraph, String correctLabel, String incorrectLabel, int spriteIndex, int lastIndex) {
        List<String> terminalVertexList = new ArrayList<>(new HashSet<>(terminalVertices));
        initialize();
        StringBuilder builder = new StringBuilder();
        backwardEdges = "";
        builder.append("{\"filename\": \"" + fileName + "\", ");
        builder.append("\"ContextGraph\": {");
        builder.append("\"Edges\": {");
        builder.append("\"Child\": [");
        builder.append(getEdgesForGNN(astEdges, 0, false, false));
        builder.append("], ");
        builder.append("\"Parent\": [");
        builder.append(getEdgesForGNN(astEdges, 1, false, false));
        builder.append("], ");
        builder.append("\"NextSib\": [");
        builder.append(getEdgesForGNN(siblingEdges, 0, false, false));
        builder.append("], ");
        builder.append("\"NextToken\": [");
        builder.append(getEdgesForGNN(checkAndAddNextTokenEdges(terminalVertexList), 0, false, false));
        builder.append("], ");
        builder.append("\"NextUse\": [");
        builder.append(getEdgesForGNN(getFlowEdges(getNextUseEdges(controlFlowGraph)), 0, false, false));
        builder.append("], ");
        builder.append("\"Flow\": [");
        builder.append(getEdgesForGNN(getFlowEdges(controlFlowGraph.getFlowEdges()), 0, false, true));
        builder.append("], ");
        builder.append("\"Backward\": [");
        builder.append(backwardEdges);
        builder.append("] ");
        builder.append("}},");
        builder.append("\"NodeLabels\": {");
        builder.append(getNodes(nodeLabels));
        builder.append("},");
        builder.append("\"NodeTypes\": {");
        List<String> nodeTypes = new LinkedList<>(nodeLabels);
        nodeTypes.remove(0);
        Set<String> nodeTypesWithoutDuplicates = new LinkedHashSet<>(nodeTypes);
        nodeTypes.clear();
        nodeTypes.addAll(nodeTypesWithoutDuplicates);
        builder.append(getNodes(nodeTypes));
        builder.append("},");
        builder.append("\"SymbolCandidates\":[");
        builder.append(getSymbolCandidates(replaceSpecialCharacters(correctLabel), replaceSpecialCharacters(incorrectLabel)));
        builder.append("]}");
        if (spriteIndex != lastIndex - 1) {
            builder.append("\n");
        }

        return builder.toString();
    }

    private String replaceSpecialCharacters(final String label) {
        if (label == null || label.isBlank()) {
            return "blank";
        } else {
            return label.replaceAll("[^a-zA-Z0-9\\s|]", "|").trim();
        }
    }

    //Return symbol candidates
    private String getSymbolCandidates(String correctlabel, String incorrectlabel) {
        String symbolCandidatesInString = "{\"SymbolDummyNode\":1, \"SymbolName\":\"" + correctlabel + "\",\"IsCorrect\":true},";
        symbolCandidatesInString = symbolCandidatesInString + "{\"SymbolDummyNode\":2, \"SymbolName\":\"" + incorrectlabel + "\",\"IsCorrect\":false}";
        return symbolCandidatesInString;
    }

    //Get Flow edges
    private Map<String, List<String>> getFlowEdges(Map<String, List<String>> flowEdges) {
        List<String> tempList = new ArrayList<>();
        Map<String, List<String>> flowEdgesInHashCode = new HashMap<>();
        Set<String> tempListWithoutDuplicates;

        String firstVertex = "";
        String secondVertex = "";
        for (Map.Entry<String, List<String>> entry : flowEdges.entrySet()) {
            if (vertexMapForFlowEdges.containsKey((entry.getKey()))) {
                firstVertex = String.valueOf(vertexMapForFlowEdges.get(entry.getKey()));
            }
            for (String targetVertex : entry.getValue()) {
                if (vertexMapForFlowEdges.containsKey(targetVertex)) {
                    secondVertex = String.valueOf(vertexMapForFlowEdges.get(targetVertex));
                }
                if (!StringUtils.isBlank(firstVertex) && !StringUtils.isBlank(secondVertex)) {
                    tempList.add(secondVertex);
                }
            }
            tempListWithoutDuplicates = new LinkedHashSet<>(tempList);
            if (!StringUtils.isBlank(firstVertex)) {
                for (String string : tempListWithoutDuplicates) {
                    flowEdgesInHashCode.computeIfAbsent(firstVertex, k -> new ArrayList<>()).add(String.valueOf(secondVertex));
                }
            }
        }
        return flowEdgesInHashCode;
    }

    //Return nodes in String
    private String getNodes(List<String> nodeTypes) {
        StringBuilder nodesInString = new StringBuilder();
        int indexOfNodes = 0;
        for (String node : nodeTypes) {
            nodesInString.append("\"").append(indexOfNodes).append("\":");
            nodesInString.append("\"").append(node).append("\",");
            indexOfNodes++;
        }
        return (nodesInString.length() > 0) ? nodesInString.substring(0, nodesInString.length() - 1) : nodesInString.toString();
    }

    //Return Next Token edges
    private Map<String, List<String>> checkAndAddNextTokenEdges(List<String> terminalVertices) {
        if (terminalVertices.size() > 1) {
            for (int i = 0; i < terminalVertices.size() - 1; i++) {
                nextTokenEdges.computeIfAbsent(terminalVertices.get(i), k -> new ArrayList<>()).add(String.valueOf(terminalVertices.get(i + 1)));
            }
        }
        return nextTokenEdges;
    }

    //Return NextUse Edges - Exploit data flow information in AST
    private Map<String, List<String>> getNextUseEdges(ControlFlowGraph controlFlowGraph) {
        DataDependenceGraph ddg = new DataDependenceGraph(controlFlowGraph);
        for (EndpointPair<CFGNode> edge : ddg.getEdges()) {
            String[] nodeUTexts = edge.nodeU().toString().split("\\.");
            String nodeU = nodeUTexts[nodeUTexts.length - 1].split("\\@")[0];
            String[] nodeVTexts = edge.nodeV().toString().split("\\.");
            String nodeV = nodeVTexts[nodeVTexts.length - 1].split("\\@")[0];
            nextUseEdges.computeIfAbsent(nodeU, k -> new ArrayList<>()).add(nodeV);
        }
        return nextUseEdges;
    }

    //Checks and add sibling edges
    private void addSiblingEdges(List<String> vertexList, int edgeType) {
        if (vertexList.size() > 1) {
            for (int i = 0; i < vertexList.size() - 1; i++) {
                siblingEdges.computeIfAbsent(vertexList.get(i), k -> new ArrayList<>()).add(String.valueOf(vertexList.get(i + 1)));
            }
        }
    }

    //Get edges for Graph in string
    private String getEdgesForGNN(Map<String, List<String>> edges, int typeOfEdge, boolean isDotString, boolean isSlotIncluded) {
        String firstVertex;
        String secondVertex;
        StringBuilder edgesInString = new StringBuilder();
        String operatorSymbol = " -> ";
        StringBuilder backwardEdgesInString = new StringBuilder();
        Map<String, String> vertexList;
        if (!isDotString) {
            operatorSymbol = ",";
            vertexList = vertexMap;
        } else {
            vertexList = vertexMapForDotString;
        }

        int numberOfedges = edges.size();
        int edgeIndex = 0;
        List<String> edgeValues;
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            firstVertex = "";
            edgeIndex++;
            if (vertexList.containsKey(entry.getKey())) {
                firstVertex = (!isDotString) ? String.valueOf(vertexList.get(entry.getKey())) : entry.getKey();
                if (isDotString) {
                    edgesInString.append(firstVertex).append(" [label= \"").append(String.valueOf(vertexList.get(entry.getKey()) + "\"];"));
                    edgesInString.append("\n");
                }
            }

            edgeValues = (!isDotString) ? entry.getValue() : new ArrayList<>(new HashSet<>(entry.getValue()));
            for (String targetVertex : edgeValues) {
                secondVertex = "";

                if (vertexList.containsKey(targetVertex)) {
                    secondVertex = vertexList.get(targetVertex);
                    if (isDotString) {
                        secondVertex = targetVertex;
                        edgesInString.append(secondVertex).append(" [label= \"").append(String.valueOf(vertexList.get(secondVertex) + "\"];"));
                        edgesInString.append("\n");
                    }
                }

                if (!isDotString) {
                    edgesInString.append("[");
                    backwardEdgesInString.append("[");
                }
                if (typeOfEdge == 0) {
                    //Child edge
                    edgesInString.append(firstVertex).append(operatorSymbol).append(secondVertex);
                    backwardEdgesInString.append(secondVertex).append(operatorSymbol).append(firstVertex);
                } else if (typeOfEdge == 1) {
                    //Parent edge
                    edgesInString.append(secondVertex).append(operatorSymbol).append(firstVertex);
                    backwardEdgesInString.append(firstVertex).append(operatorSymbol).append(secondVertex);
                }

                if (!isDotString) {
                    edgesInString.append("]");
                    backwardEdgesInString.append("]");
                    if (edgeIndex <= numberOfedges) {
                        edgesInString.append(operatorSymbol);
                        backwardEdgesInString.append(operatorSymbol);
                    }
                } else {
                    edgesInString.append("\n");
                    backwardEdgesInString.append("\n");
                }
            }
        }
        backwardEdges = (backwardEdges.isEmpty()) ? backwardEdges + backwardEdgesInString : backwardEdges + ',' + backwardEdgesInString;
        if (!isDotString) {
            edgesInString = new StringBuilder((edgesInString.length() > 0) ? edgesInString.substring(0, edgesInString.length() - 1) : edgesInString.toString());
            backwardEdges = (backwardEdges.length() > 0) ? backwardEdges.substring(0, backwardEdges.length() - 1) : backwardEdges;
        }

        return edgesInString.toString();
    }

    @Override
    public void visit(PenStmt node) {
        node.accept((PenExtensionVisitor) this);
    }

    @Override
    public void visitParentVisitor(PenStmt node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(PenDownStmt node) {
        if (node != null) {
            recordLeaf(node);
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (node != null) {
            recordLeaf(node);
        }
    }
}
