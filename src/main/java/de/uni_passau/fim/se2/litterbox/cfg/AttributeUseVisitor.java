/*
 * Copyright (C) 2019 LitterBox contributors
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

package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

public class AttributeUseVisitor  implements ScratchVisitor {

    private Set<Attribute> uses = new LinkedHashSet<>();

    private ActorDefinition currentActor;

    public AttributeUseVisitor(ActorDefinition currentActor) {
        this.currentActor = currentActor;
    }

    public Set<Attribute> getAttributeUses() {
        return uses;
    }

    @Override
    public void visit(ControlStmt node) {
        // Don't visit child statements
    }

    @Override
    public void visit(ChangeXBy node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(ChangeYBy node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(MoveSteps node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(PositionX node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(PositionY node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(DistanceTo node) {
        uses.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(TurnLeft node) {
        uses.add(Attribute.rotationOf(currentActor));
    }

    @Override
    public void visit(TurnRight node) {
        uses.add(Attribute.rotationOf(currentActor));
    }

    @Override
    public void visit(Direction node) {
        uses.add(Attribute.rotationOf(currentActor));
    }


    //---------------------------------------------------------------
    // Costume

    @Override
    public void visit(NextCostume node) {
        uses.add(Attribute.costumeOf(currentActor));
    }

    @Override
    public void visit(Costume node) {
        uses.add(Attribute.costumeOf(currentActor));
    }

    //---------------------------------------------------------------
    // Size

    @Override
    public void visit(ChangeSizeBy node) {
        uses.add(Attribute.sizeOf(currentActor));
    }

    @Override
    public void visit(Size node) {
        uses.add(Attribute.sizeOf(currentActor));
    }


}
