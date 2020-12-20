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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Detects type errors of the following form:
 * - Comparisons between Number and Boolean
 * - Comparisons between Direction and Loudness
 * - Comparisons between Direction and Position nodes (MouseX, MouseY, PositionX, PositionY)
 * - Comparisons between Loudness and Position nodes (MouseX, MouseY, PositionX, PositionY)
 * - Comparisons containing Touching or Not nodes
 * - Distance to containing weird blocks
 */
public class TypeError extends AbstractIssueFinder {
    public static final String NAME = "type_error";
    public static final String WEIRD_DISTANCE = "type_error_weird_distance";
    private boolean insideComparison = false;
    private boolean isRightSide = false;

    private Type type = null;

    private enum Type {BOOLEAN, NUMBER, STRING, LOUDNESS, POSITION, DIRECTION}

    @Override
    public void visit(LessThan node) {
        comparison(node);
    }

    @Override
    public void visit(BiggerThan node) {
        comparison(node);
    }

    @Override
    public void visit(Equals node) {
        comparison(node);
    }

    @Override
    public void visit(DistanceTo node) {
        Position position = node.getPosition();
        if (position instanceof FromExpression) {
            Hint hint = new Hint(WEIRD_DISTANCE);
            if (!(((FromExpression) position).getStringExpr() instanceof AsString)) {
                addIssue(node, node.getMetadata(), hint);
            } else if (!((((AsString) ((FromExpression) position).getStringExpr()).getOperand1() instanceof StrId) || ((AsString) ((FromExpression) position).getStringExpr()).getOperand1() instanceof Qualified)) {
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    private void comparison(BinaryExpression<ComparableExpr, ComparableExpr> node) {
        insideComparison = true;
        if (!node.getChildren().isEmpty()) {
            isRightSide = false;
            ASTNode leftChild = node.getChildren().get(0);
            leftChild.accept(this);
            ASTNode rightChild = node.getChildren().get(1);
            isRightSide = true;
            rightChild.accept(this);
        }
        insideComparison = false; // TODO: Can comparisons be nested?
        type = null;
    }

    @Override
    public void visit(StringExpr node) {
        if (insideComparison) {
            if (!isRightSide) {
                type = null;
            }
        }
    }

    @Override
    public void visit(Loudness node) {
        if (!isValid(Type.LOUDNESS)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(StringLiteral node) {
        if (insideComparison) {
            if (!isRightSide) {
                this.type = Type.STRING;
            } else {
                if (this.type != null && type != Type.STRING) {
                    addIssue(node.getParentNode(), node.getParentNode().getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(MouseX node) {
        if (!isValid(Type.POSITION)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(MouseY node) {
        if (!isValid(Type.POSITION)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(PositionX node) {
        if (!isValid(Type.POSITION)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(PositionY node) {
        if (!isValid(Type.POSITION)) {
            addIssue(node, node.getMetadata());
        }
    }

    /*@Override
    public void visit(PickRandom node) {
        if(insideComparison) {
            checker("Number");
        }
    }*/

    @Override
    public void visit(NumExpr node) {
        if (insideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                if (this.type != null && this.type == Type.BOOLEAN) {
                    addIssue(node, node.getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(Direction node) {
        if (!isValid(Type.DIRECTION)) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(AsString node) {
        if (insideComparison) {
            if (!isRightSide) {
                if (node.getOperand1().getUniqueName().equals("Touching")) {
                    type = Type.BOOLEAN;
                } else if (node.getOperand1().getUniqueName().equals("Not")) {
                    type = Type.BOOLEAN;
                } else {
                    type = null;
                }
            } else {
                if (node.getOperand1().getUniqueName().equals("Touching") || node.getOperand1().getUniqueName().equals("Not")) {
                    addIssue(node, node.getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(AsNumber node) {
        if (insideComparison) {
            if (!isRightSide) {
                type = null;
            } else {
                if (this.type == Type.BOOLEAN) {
                    addIssue(node, node.getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                if (this.type != null && this.type == Type.BOOLEAN) {
                    addIssue(node.getParentNode(), node.getParentNode().getMetadata());
                }
            }
        }
    }

    private boolean isValid(Type type) {
        if (insideComparison) {
            if (!isRightSide) {
                this.type = type;
            } else {
                if (this.type != null && this.type != type && this.type != Type.NUMBER) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(WEIRD_DISTANCE);
        return keys;
    }
}
