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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeVolumeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;

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
    public void visit(Hide node) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Show node) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SpriteTouchable sprite) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
        visitChildren(sprite);
    }

    @Override
    public void visit(AskAndWait node) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
        visitChildren(node);
    }

    @Override
    public void visit(Say node) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
        visitChildren(node);
    }

    @Override
    public void visit(SayForSecs node) {
        uses.add(Attribute.visibilityOf(currentActor.getIdent()));
        visitChildren(node);
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
    // Timer

    @Override
    public void visit(Timer node) {
        uses.add(Attribute.timerOf(getActorSprite(currentActor).getIdent()));
    }

    @Override
    public void visit(AttributeAboveValue node) {
        if (node.getAttribute().getType().equals(EventAttribute.EventAttributeType.TIMER)) {
            uses.add(Attribute.timerOf(getActorSprite(currentActor).getIdent()));
        }
        // Loudness attribute is not the same as volume, so no use in that case
        node.getValue().accept(this);
    }

    //---------------------------------------------------------------
    // Effect
    @Override
    public void visit(ChangeGraphicEffectBy node) {
        uses.add(Attribute.graphicEffectOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        uses.add(Attribute.soundEffectOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        uses.add(Attribute.volumeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Volume node) {
        uses.add(Attribute.volumeOf(currentActor.getIdent()));
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
    // Layer
    @Override
    public void visit(NextBackdrop node) {
        uses.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        uses.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }


    @Override
    public void visit(Backdrop node) {
        uses.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }

    //---------------------------------------------------------------
    // Layer
    @Override
    public void visit(ChangeLayerBy node) {
        uses.add(Attribute.layerOf(currentActor.getIdent()));
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
                switch (at.getType()) {
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
                    case COSTUME_NAME:
                        uses.add(Attribute.costumeOf(localIdentifier));
                        break;
                    case VOLUME:
                        uses.add(Attribute.volumeOf(localIdentifier));
                        break;
                    case BACKDROP_NAME:
                    case BACKDROP_NUMBER:
                        uses.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
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
