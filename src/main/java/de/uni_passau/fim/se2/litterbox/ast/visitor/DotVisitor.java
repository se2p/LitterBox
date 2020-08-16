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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Visitor that creates a .dot output for a Program-AST
 */
public class DotVisitor implements ScratchVisitor {

    List<String> edges = new LinkedList<>();
    long counter = 0;

    @Override
    public void visit(ASTNode node) {
        if (node instanceof ASTLeaf) {
            recordLeaf((ASTLeaf) node);
        } else {
            String name = String.valueOf(node.hashCode()); //This should only be a workaround this is a hack
            String label = name + " [label = \"" + node.getUniqueName() + "\"];";
            edges.add(label);
            for (ASTNode child : node.getChildren()) {
                String edge = name + " -> " + child.hashCode() + "";
                edges.add(edge);
            }

            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
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

    public void recordLeaf(ASTLeaf node) {
        String name = String.valueOf(node.hashCode());
        String label = name + " [label = \"" + node.getUniqueName() + "\"];";
        edges.add(label);
        String[] simpleStrings = node.toSimpleStringArray();
        for (String simpleString : simpleStrings) {
            counter++;
            String sLabel = counter + " [label = \"" + simpleString + "\"];";
            edges.add(sLabel);
            String edge = name + " -> " + counter;
            edges.add(edge);
        }
    }

    public void printGraph() {
        System.out.println("digraph G {");
        System.out.println("\t rankdir=LR");
        System.out.println("\t shape=rectangle");
        for (String edge : edges) {
            System.out.print("\t");
            System.out.println(edge);
        }
        System.out.println("}");
    }

    public void saveGraph(String fileName) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));

        bw.write("digraph G {");
        bw.newLine();
        // bw.write("\t rankdir=LR");
        // bw.newLine();
        bw.write("\t shape=rectangle");
        bw.newLine();
        for (String edge : edges) {
            bw.write("\t");
            bw.write(edge);
            bw.newLine();
        }
        bw.write("}");
        bw.close();
    }
}
