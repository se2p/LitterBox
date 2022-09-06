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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class NumValueVisitor implements ScratchVisitor {
    double endValue;

    public NumValueVisitor() {
        endValue = 0;
    }

    //Stops all unwanted recursive parsing of the ScratchVisitor Interface
    @Override
    public void visitChildren(ASTNode node) {
        throw new NoNumberException();
    }

    @Override
    public void visit(NumberLiteral node) {
        endValue = node.getValue();
    }

    @Override
    public void visit(Add node) {
        double aValue;
        double bValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        endValue = aValue + bValue;
    }

    @Override
    public void visit(Minus node) {
        double aValue;
        double bValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        endValue = aValue - bValue;
    }

    @Override
    public void visit(Mult node) {
        double aValue;
        double bValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        endValue = aValue * bValue;
    }

    @Override
    public void visit(Div node) {
        double aValue;
        double bValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        endValue = aValue / bValue;
    }

    @Override
    public void visit(Mod node) {
        double aValue;
        double bValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        endValue = aValue % bValue;
    }

    @Override
    public void visit(Round node) {
        double aValue;

        node.getOperand1().accept(this);
        aValue = getEndValue();

        endValue = Math.round(aValue);
    }

    @Override
    public void visit(NumFunctOf node) throws NoNumberException {
        NumFunct.NumFunctType operand;
        double bValue;

        operand = node.getOperand1().getType();
        node.getOperand2().accept(this);
        bValue = getEndValue();

        switch (operand) {

            case ABS:
                endValue = Math.abs(bValue);
                break;

            case ACOS:
                endValue = Math.acos(bValue);
                break;

            case ASIN:
                endValue = Math.asin(bValue);
                break;

            case ATAN:
                endValue = Math.atan(bValue);
                break;

            case CEILING:
                endValue = Math.ceil(bValue);
                break;

            case COS:
                endValue = Math.cos(bValue);
                break;

            case FLOOR:
                endValue = Math.floor(bValue);
                break;

            case LN:
                endValue = Math.log(bValue);
                break;

            case LOG:
                endValue = Math.log10(bValue);
                break;

            case POW10:
                endValue = Math.pow(bValue, 10);
                break;

            case POWE:
                endValue = Math.pow(bValue, Math.E);
                break;

            case SIN:
                endValue = Math.sin(bValue);
                break;

            case SQRT:
                endValue = Math.sqrt(bValue);
                break;

            case TAN:
                endValue = Math.tan(bValue);
                break;

            case UNKNOWN:
            default:
                throw new NoNumberException();
        }
    }

    private double getEndValue() {
        return endValue;
    }

    public double calculateEndValue(ASTNode node) throws Exception {
        node.accept(this);
        return endValue;
    }
}
