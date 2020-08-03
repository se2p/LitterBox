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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class AttributeDefinitionVisitor implements DefinableCollector<Attribute> {

    private Set<Attribute> definitions = new LinkedHashSet<>();

    // TODO: Store LocalIdentifier instead of actor directly?
    private ActorDefinition currentActor;

    public AttributeDefinitionVisitor(ActorDefinition currentActor) {
        this.currentActor = currentActor;
    }

    @Override
    public Set<Attribute> getDefineables() {
        return definitions;
    }

    @Override
    public void visit(ControlStmt node) {
        // Don't visit child statements
    }

    //---------------------------------------------------------------
    // Position
    @Override
    public void visit(ChangeXBy node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeYBy node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetXTo node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetYTo node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GoToPos node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GoToPosXY node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(MoveSteps node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GlideSecsToXY node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GlideSecsTo node) {
        definitions.add(Attribute.positionOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Rotation

    @Override
    public void visit(TurnLeft node) {
        definitions.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    @Override
    public void visit(TurnRight node) {
        definitions.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    @Override
    public void visit(PointInDirection node) {
        definitions.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    @Override
    public void visit(PointTowards node) {
        definitions.add(Attribute.rotationOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Costume

    @Override
    public void visit(NextCostume node) {
        definitions.add(Attribute.costumeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        definitions.add(Attribute.costumeOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Size

    @Override
    public void visit(ChangeSizeBy node) {
        definitions.add(Attribute.sizeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetSizeTo node) {
        definitions.add(Attribute.sizeOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Backdrop
    //
    //    @Override
    //    public void visit(NextBackdrop node) {
    //        definitions.add(Attribute.backdropOf(stageActor));
    //    }
    //
    //    @Override
    //    public void visit(BackdropSwitchTo node) {
    //        definitions.add(Attribute.backdropOf(stageActor));
    //    }
}
