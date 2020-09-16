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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class AttributeUseVisitor implements DefinableCollector<Attribute> {

    private Set<Attribute> uses = new LinkedHashSet<>();

    private ActorDefinition currentActor;

    public AttributeUseVisitor(ActorDefinition currentActor) {
        this.currentActor = currentActor;
    }

    @Override
    public Set<Attribute> getDefineables() {
        return uses;
    }

    @Override
    public void visit(IfThenStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        // Nop
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        node.getTimes().accept(this);
    }

    @Override
    public void visit(UntilStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(ChangeXBy node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeYBy node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(MoveSteps node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(PositionX node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(PositionY node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(DistanceTo node) {
        uses.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(TurnLeft node) {
        uses.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    @Override
    public void visit(TurnRight node) {
        uses.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Direction node) {
        uses.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Costume

    @Override
    public void visit(NextCostume node) {
        uses.add(Attribute.costumeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Costume node) {
        uses.add(Attribute.costumeOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Size

    @Override
    public void visit(ChangeSizeBy node) {
        uses.add(Attribute.sizeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Size node) {
        uses.add(Attribute.sizeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(AttributeAboveValue node) {
        // TODO: Handle use of timer and volume attributes here once implemented (#210)
        node.getValue().accept(this);
    }

    @Override
    public void visit(AttributeOf node) {
        // TODO: Handle this

        // Name of var or attribute
        de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute attribute
                = node.getAttribute();
        // Name of owner
        Expression owner = ((WithExpr) node.getElementChoice()).getExpression();

        // Can only handle LocalIdentifier hier (i.e. value selected in dropdown)
        // We lose precision here because it could also be a Parameter or else
        // but we don't know the value of that statically
        if (owner instanceof LocalIdentifier) {
            LocalIdentifier localIdentifier = (LocalIdentifier) owner;

            if (attribute instanceof AttributeFromFixed) {
                AttributeFromFixed fixedAttribute = (AttributeFromFixed) attribute;
                FixedAttribute at = fixedAttribute.getAttribute();
                switch (at) {
                    case X_POSITION:
                    case Y_POSITION:
                        uses.add(Attribute.positionOf(localIdentifier));
                        break;
                    case SIZE:
                        uses.add(Attribute.sizeOf(localIdentifier));
                        break;
                    case DIRECTION:
                        uses.add(Attribute.rotationOf(localIdentifier));
                        break;
                    case COSTUME_NUMBER:
                        uses.add(Attribute.costumeOf(localIdentifier));
                        break;
                    case VOLUME:
                    case COSTUME_NAME:
                    case BACKDROP_NAME:
                    case BACKDROP_NUMBER:
                        // Not handled yet
                        break;
                    default:
                        // TODO: What should happen in the default case?
                }
            }
            // TODO: Once we handle parameters:
            //        } else if (owner instanceof Parameter) {
            //            Parameter parameter = (Parameter) owner;
            //            if (attribute instanceof AttributeFromFixed) {
            //                AttributeFromFixed fixedAttribute = (AttributeFromFixed) attribute;
            //
            //            }
            // }
        }
    }
}
