package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
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
