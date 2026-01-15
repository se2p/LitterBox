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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect.GraphicEffectType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect.SoundEffectType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

public class AttributeDefinitionVisitor implements DefinableCollector<Attribute>, VideoSensingExtensionVisitor {

    private final Set<Attribute> definitions = new LinkedHashSet<>();

    private final ActorDefinition currentActor;

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
    // Visibility
    @Override
    public void visit(Hide node) {
        definitions.add(Attribute.visibilityOf(currentActor.getIdent()));
    }

    @Override
    public void visit(Show node) {
        definitions.add(Attribute.visibilityOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Effect
    @Override
    public void visit(ChangeGraphicEffectBy node) {
        String effect = getEffectName(node.getEffect());
        if (effect != null) {
            definitions.add(Attribute.graphicEffectOf(currentActor.getIdent(), effect));
        } else {
            definitions.add(Attribute.graphicEffectOf(currentActor.getIdent()));
        }
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        String effect = getEffectName(node.getEffect());
        if (effect != null) {
            definitions.add(Attribute.graphicEffectOf(currentActor.getIdent(), effect));
        } else {
            definitions.add(Attribute.graphicEffectOf(currentActor.getIdent()));
        }
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        // Clears ALL graphic effects
        definitions.add(Attribute.graphicEffectOf(currentActor.getIdent()));
        addUnknownEffectDefinitions();
    }

    private void addUnknownEffectDefinitions() {
        // We know the standard scratch effects
        for (GraphicEffectType type : GraphicEffectType.values()) {
            definitions.add(Attribute.graphicEffectOf(currentActor.getIdent(), type.getToken()));
        }
        // Also add the generic one
        definitions.add(Attribute.graphicEffectOf(currentActor.getIdent()));
    }

    private String getEffectName(GraphicEffect effect) {
        return effect.getTypeName();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        String effect = getSoundEffectName(node.getEffect());
        if (effect != null) {
            definitions.add(Attribute.soundEffectOf(currentActor.getIdent(), effect));
        } else {
            definitions.add(Attribute.soundEffectOf(currentActor.getIdent()));
        }
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        String effect = getSoundEffectName(node.getEffect());
        if (effect != null) {
            definitions.add(Attribute.soundEffectOf(currentActor.getIdent(), effect));
        } else {
            definitions.add(Attribute.soundEffectOf(currentActor.getIdent()));
        }
    }

    @Override
    public void visit(ClearSoundEffects node) {
        // Standard sound effects: PITCH, PAN
        for (SoundEffectType type : SoundEffectType.values()) {
            definitions.add(Attribute.soundEffectOf(currentActor.getIdent(), type.getToken()));
        }
        definitions.add(Attribute.soundEffectOf(currentActor.getIdent()));
    }

    private String getSoundEffectName(SoundEffect effect) {
        return effect.getTypeName();
    }

    @Override
    public void visit(SetVolumeTo node) {
        definitions.add(Attribute.volumeOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        definitions.add(Attribute.volumeOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Layer
    @Override
    public void visit(ChangeLayerBy node) {
        definitions.add(Attribute.layerOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GoToLayer node) {
        definitions.add(Attribute.layerOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Position
    @Override
    public void visit(ChangeXBy node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
    }

    @Override
    public void visit(ChangeYBy node) {
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetXTo node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetYTo node) {
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GoToPos node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GoToPosXY node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(MoveSteps node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GlideSecsToXY node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(GlideSecsTo node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        definitions.add(Attribute.positionXOf(currentActor.getIdent()));
        definitions.add(Attribute.positionYOf(currentActor.getIdent()));
        definitions.add(Attribute.rotationOf(currentActor.getIdent()));
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

    @Override
    public void visit(SetRotationStyle node) {
        definitions.add(Attribute.rotationStyleOf(currentActor.getIdent()));
    }

    @Override
    public void visit(SetDragMode node) {
        definitions.add(Attribute.dragModeOf(currentActor.getIdent()));
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

    @Override
    public void visit(NextBackdrop node) {
        definitions.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }

    @Override
    public void visit(SwitchBackdrop node) {
        definitions.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        definitions.add(Attribute.backdropOf(getActorSprite(currentActor).getIdent()));
    }

    //---------------------------------------------------------------
    // Timer

    @Override
    public void visit(ResetTimer node) {
        definitions.add(Attribute.timerOf(getActorSprite(currentActor).getIdent()));
    }

    //---------------------------------------------------------------
    // Sound State

    @Override
    public void visit(StopAllSounds node) {
        definitions.add(Attribute.soundStateOf(currentActor.getIdent()));
    }

    //---------------------------------------------------------------
    // Video Sensing

    @Override
    public void visit(TurnVideo node) {
        definitions.add(Attribute.videoStateOf(getActorSprite(currentActor).getIdent()));
    }

    @Override
    public void visit(SetVideoTransparency node) {
        definitions.add(Attribute.videoTransparencyOf(getActorSprite(currentActor).getIdent()));
    }
}
