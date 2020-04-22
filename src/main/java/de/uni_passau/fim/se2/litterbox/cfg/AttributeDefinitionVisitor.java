package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

// TODO: Update currentActor with visit?
// TODO: Throw RuntimeException if new actor is visited?
public class AttributeDefinitionVisitor implements ScratchVisitor {

    private Set<Attribute> definitions = new LinkedHashSet<>();

    private ActorDefinition currentActor;

    public AttributeDefinitionVisitor(ActorDefinition currentActor) {
        this.currentActor = currentActor;
    }

    public Set<Attribute> getAttributeDefinitions() {
        return definitions;
    }

    //---------------------------------------------------------------
    // Position
    @Override
    public void visit(ChangeXBy node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(ChangeYBy node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(SetXTo node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(SetYTo node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(GoToPos node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(GoToPosXY node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(MoveSteps node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(GlideSecsToXY node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    @Override
    public void visit(GlideSecsTo node) {
        definitions.add(Attribute.positionOf(currentActor));
    }

    //---------------------------------------------------------------
    // Rotation

    @Override
    public void visit(TurnLeft node) {
        definitions.add(Attribute.rotationOf(currentActor));
    }

    @Override
    public void visit(TurnRight node) {
        definitions.add(Attribute.rotationOf(currentActor));
    }

    @Override
    public void visit(PointInDirection node) {
        definitions.add(Attribute.rotationOf(currentActor));
    }

    @Override
    public void visit(PointTowards node) {
        definitions.add(Attribute.rotationOf(currentActor));
    }


    //---------------------------------------------------------------
    // Costume

    @Override
    public void visit(NextCostume node) {
        definitions.add(Attribute.costumeOf(currentActor));
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        definitions.add(Attribute.costumeOf(currentActor));
    }

    //---------------------------------------------------------------
    // Size

    @Override
    public void visit(ChangeSizeBy node) {
        definitions.add(Attribute.sizeOf(currentActor));
    }

    @Override
    public void visit(SetSizeTo node) {
        definitions.add(Attribute.sizeOf(currentActor));
    }


    // Hide/Show: Visibility
    //---------------------------------------------------------------
    // Backdrop

//    @Override
//    public void visit(NextBackdrop node) {
//        definitions.add(Attribute.rotationOf(currentActor));
//    }
//
//    @Override
//    public void visit(BackdropSwitchTo node) {
//        definitions.add(Attribute.rotationOf(currentActor));
//    }



}
