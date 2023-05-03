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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;

/**
 * Visitor that creates a .dot output for a Program-AST
 */
public class DotVisitor implements ScratchVisitor, PenExtensionVisitor {
    StringBuilder edgesString = new StringBuilder();
    long counter = 0;

    public static String buildDotGraph(final ASTNode node) {
        final DotVisitor visitor = new DotVisitor();

        visitor.initDotString();
        node.accept(visitor);
        visitor.finishDotString();

        return visitor.edgesString.toString();
    }

    private void initDotString() {
        addln("digraph G {");
        addln("shape=rectangle");
    }

    private void finishDotString() {
        edgesString.append("}");
    }

    private void addln(String line) {
        edgesString.append(line).append(System.lineSeparator());
    }

    @Override
    public void visit(ASTNode node) {
        if (node instanceof ASTLeaf astLeaf) {
            recordLeaf(astLeaf);
        } else {
            String name = String.valueOf(System.identityHashCode(node)); //This should only be a workaround this is a hack
            String label = name + " [label = \"" + node.getUniqueName() + "\"];";
            addln(label);
            for (ASTNode child : node.getChildren()) {
                String edge = name + " -> " + System.identityHashCode(child);
                addln(edge);
            }

            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    public void recordLeaf(ASTLeaf node) {
        String name = String.valueOf(System.identityHashCode(node));
        String label = name + " [label = \"" + node.getUniqueName() + "\"];";
        addln(label);
        String[] simpleStrings = node.toSimpleStringArray();
        for (String simpleString : simpleStrings) {
            counter++;
            String sLabel = counter + " [label = \"" + simpleString + "\"];";
            addln(sLabel);
            String edge = name + " -> " + counter;
            addln(edge);
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
}
