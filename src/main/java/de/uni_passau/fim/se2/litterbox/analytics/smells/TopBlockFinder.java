/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

public abstract class TopBlockFinder extends AbstractIssueFinder implements PenExtensionVisitor, TextToSpeechExtensionVisitor {
    boolean setHint = false;

    public void visit(Stmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    public void visit(ASTNode node) {
        if (setHint) {
            addIssueWithLooseComment();
        } else {
            visitChildren(node);
        }
    }

    public void visit(ExpressionStmt node) {
        if (setHint) {
            addIssue(node.getExpression(), node.getExpression().getMetadata());
        } else {
            visitChildren(node);
        }
    }

    //PenBlocks

    @Override
    public void visit(PenStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    //TextToSpeechBlocks

    @Override
    public void visit(TextToSpeechBlock node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visitParentVisitor(PenStmt node) {
        visitDefaultVisitor((Stmt) node);
    }

    @Override
    public void visitParentVisitor(TextToSpeechBlock node) {
        visitDefaultVisitor(node);
    }
}
