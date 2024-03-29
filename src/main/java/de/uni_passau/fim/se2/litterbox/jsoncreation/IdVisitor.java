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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

public class IdVisitor implements ScratchVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor {
    private String id;

    public String getBlockId(Stmt stmt) {
        stmt.accept(this);
        return id;
    }

    @Override
    public void visit(CreateCloneOf node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(IfElseStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(IfThenStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(WaitUntil node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(UntilStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Broadcast node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(CallStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteClone node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopAll node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextBackdrop node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(MoveSteps node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeXBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeYBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetXTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetYTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GoToPos node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GoToPosXY node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetAttributeTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StartSound node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopAllSounds node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ResetTimer node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetVariableTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(WaitSeconds node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(AddTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteAllOf node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteOf node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(InsertAt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ReplaceItem node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GoToLayer node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Hide node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(HideVariable node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(HideList node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ShowList node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Say node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SayForSecs node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetSizeTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Show node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ShowVariable node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextCostume node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Think node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ThinkForSecs node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GlideSecsTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PointInDirection node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PointTowards node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(TurnLeft node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(TurnRight node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopThisScript node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetVolumeTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetRotationStyle node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetDragMode node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(AskAndWait node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    //Pen

    @Override
    public void visit(PenStmt node) {
        node.accept((PenExtensionVisitor) this);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenDownStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenUpStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenClearStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenStampStmt node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visitParentVisitor(PenStmt node) {
        visitDefaultVisitor(node);
    }

    //Text to Speech

    @Override
    public void visitParentVisitor(TextToSpeechBlock node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(TextToSpeechBlock node) {
        node.accept((TextToSpeechExtensionVisitor) this);
    }

    @Override
    public void visit(FixedLanguage node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(FixedVoice node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ExprLanguage node) {
        id = ((NonDataBlockMetadata) node.getExpr().getMetadata()).getBlockId();
    }

    @Override
    public void visit(ExprVoice node) {
        id = ((NonDataBlockMetadata) node.getExpr().getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetLanguage node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetVoice node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Speak node) {
        id = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }
}
