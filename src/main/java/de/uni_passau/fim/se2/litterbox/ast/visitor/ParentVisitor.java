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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

public class ParentVisitor implements ScratchVisitor, MusicExtensionVisitor, PenExtensionVisitor,
        TextToSpeechExtensionVisitor, TranslateExtensionVisitor, VideoSensingExtensionVisitor {

    /**
     * Links all nodes in the AST to their parent nodes.
     *
     * @param root The root of the tree.
     */
    public static void apply(final ASTNode root) {
        root.accept(new ParentVisitor());
    }

    @Override
    public void visit(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (child != null) {
                child.setParentNode(node);
                child.accept(this);
            }
        }
    }
}
