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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;

public class IdVisitor
        implements ScratchVisitor,
        PenExtensionVisitor,
        TextToSpeechExtensionVisitor,
        MusicExtensionVisitor,
        VideoSensingExtensionVisitor,
        TranslateExtensionVisitor,
        QuantumExtensionVisitor {
    private String id;

    public String getBlockId(Stmt stmt) {
        // Reset id to prevent stale value from previous visit being returned
        // for nodes without a specific visit method (e.g., UnspecifiedStmt)
        id = null;
        stmt.accept(this);
        return id;
    }

    // Video Sensing Extension

    @Override
    public void visit(WhenVideoMotionGreaterThan node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(VideoOn node) {
        // Expression, no ID usually needed for Stmt ID visitor but good for completeness if checked
    }

    @Override
    public void visit(TurnVideo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetVideoTransparency node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(CreateCloneOf node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(IfElseStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(IfThenStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(WaitUntil node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(UntilStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Broadcast node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(CallStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(DeleteClone node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(StopAll node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(NextBackdrop node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(MoveSteps node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeXBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeYBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetXTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetYTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(GoToPos node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(GoToPosXY node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetAttributeTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(StartSound node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(StopAllSounds node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ResetTimer node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetVariableTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(WaitSeconds node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(AddTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(DeleteAllOf node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(DeleteOf node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(InsertAt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ReplaceItem node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(GoToLayer node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Hide node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(HideVariable node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(HideList node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ShowList node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Say node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SayForSecs node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetSizeTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Show node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ShowVariable node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(NextCostume node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Think node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ThinkForSecs node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(GlideSecsTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PointInDirection node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PointTowards node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(TurnLeft node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(TurnRight node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(StopThisScript node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetVolumeTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetRotationStyle node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetDragMode node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(AskAndWait node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        id = node.getMetadata().getBlockId();
    }

    //Pen

    @Override
    public void visit(SetPenSizeTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PenDownStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PenUpStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PenClearStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PenStampStmt node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        id = node.getMetadata().getBlockId();
    }

    //Text to Speech

    @Override
    public void visit(FixedLanguage node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(FixedVoice node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ExprLanguage node) {
        id = (node.getExpr().getMetadata()).getBlockId();
    }

    @Override
    public void visit(ExprVoice node) {
        id = (node.getExpr().getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetLanguage node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetVoice node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Speak node) {
        id = node.getMetadata().getBlockId();
    }

    // Music Extension

    @Override
    public void visit(ChangeTempoBy node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetTempoTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(SetInstrumentTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PlayDrumForBeats node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(PlayNoteForBeats node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(RestForBeats node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(FixedNote node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(FixedDrum node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(FixedInstrument node) {
        id = node.getMetadata().getBlockId();
    }

    // Translate Extension

    @Override
    public void visit(TranslateTo node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(ViewerLanguage node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(TFixedLanguage node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(TExprLanguage node) {
        id = node.getMetadata().getBlockId();
    }

    // region: quantum extension

    @Override
    public void visit(WhenMeasured node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(WhenEntanglementStarted node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(WhenSuperpositionStarted node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(Measure node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumSuperpositionPossibilities node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumSuperpositionList node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumSuperpositionPossibilitiesList node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumEntanglementPossibilities node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumEntanglementList node) {
        id = node.getMetadata().getBlockId();
    }

    @Override
    public void visit(QuantumEntanglementPossibilitiesList node) {
        id = node.getMetadata().getBlockId();
    }

    // endregion
}
