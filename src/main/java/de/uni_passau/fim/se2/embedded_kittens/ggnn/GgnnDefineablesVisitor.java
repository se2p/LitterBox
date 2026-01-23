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
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

abstract class GgnnDefineablesVisitor implements ScratchVisitor {

    protected abstract void addVariable(Variable variable);

    protected abstract void addList(ScratchList list);

    protected abstract void addAttribute(ASTNode node);

    @Override
    public void visit(Program node) {
        node.getActorDefinitionList().accept(this);
    }

    @Override
    public void visit(ActorDefinition node) {
        node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);
    }

    @Override
    public void visit(Variable node) {
        addVariable(node);
    }

    @Override
    public void visit(ScratchList node) {
        addList(node);
    }

    @Override
    public void visit(Backdrop node) {
        addAttribute(node);
    }

    @Override
    public void visit(Costume node) {
        addAttribute(node);
    }

    @Override
    public void visit(Direction node) {
        addAttribute(node);
    }

    @Override
    public void visit(Loudness node) {
        addAttribute(node);
    }

    @Override
    public void visit(PositionX node) {
        addAttribute(node);
    }

    @Override
    public void visit(PositionY node) {
        addAttribute(node);
    }

    @Override
    public void visit(Size node) {
        addAttribute(node);
    }

    @Override
    public void visit(Volume node) {
        addAttribute(node);
    }

    // not directly attributes but external values:
    // included as they can be used like variables and attributes within expressions and therefore are of interest
    // especially for COMPUTED_FROM and GUARDED_BY edges

    @Override
    public void visit(Answer node) {
        addAttribute(node);
    }

    @Override
    public void visit(AttributeOf node) {
        addAttribute(node);
    }

    @Override
    public void visit(Current node) {
        addAttribute(node);
    }

    @Override
    public void visit(DaysSince2000 node) {
        addAttribute(node);
    }

    @Override
    public void visit(DistanceTo node) {
        addAttribute(node);
    }

    @Override
    public void visit(MouseX node) {
        addAttribute(node);
    }

    @Override
    public void visit(MouseY node) {
        addAttribute(node);
    }

    @Override
    public void visit(Timer node) {
        addAttribute(node);
    }

    @Override
    public void visit(Username node) {
        addAttribute(node);
    }
}
