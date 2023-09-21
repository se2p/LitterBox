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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.tokenizer;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingType;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class StatementLevelTokenizer
        implements ScratchVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor, MusicExtensionVisitor {
    private final List<String> tokens = new ArrayList<>();

    private final boolean abstractTokens;

    private final MaskingStrategy maskingStrategy;

    private final ProcedureDefinitionNameMapping procedureNameMapping;

    private StatementLevelTokenizer(final ProcedureDefinitionNameMapping procedureNameMapping,
                                    final boolean abstractTokens,
                                    final MaskingStrategy maskingStrategy) {
        Preconditions.checkNotNull(procedureNameMapping);

        this.procedureNameMapping = procedureNameMapping;
        this.abstractTokens = abstractTokens;
        this.maskingStrategy = maskingStrategy;
    }

    public static List<String> tokenize(final Program program,
                                        final ASTNode node,
                                        final boolean abstractTokens,
                                        final MaskingStrategy maskingStrategy) {
        return tokenize(program.getProcedureMapping(), node, abstractTokens, maskingStrategy);
    }

    private static List<String> tokenize(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final ASTNode node,
            final boolean abstractTokens,
            final MaskingStrategy maskingStrategy
    ) {
        final StatementLevelTokenizer v =
                new StatementLevelTokenizer(procedureNameMapping, abstractTokens, maskingStrategy);
        node.accept(v);
        return v.tokens;
    }

    private void addToken(final Token opcode) {
        tokens.add(opcode.getStrRep());
    }

    private void addToken(final AbstractToken token) {
        tokens.add(token.name());
    }

    private String getStatementId(final ASTNode node) {
        try {
            if (node.getMetadata() instanceof DataBlockMetadata block) {
                return block.getBlockId();
            }
            else if (node.getMetadata() instanceof NonDataBlockMetadata block) {
                return block.getBlockId();
            }
        }
        catch (Exception ignored) { }
        return null;
    }

    private void visitControlBlock(final ASTNode node, final Token opcode) {
        if (MaskingType.Statement.equals(maskingStrategy.getMaskingType()) &&
                maskingStrategy.getBlockId().equals(getStatementId(node))) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
            visitChildren(node);
        }
    }

    private void visit(final ASTNode node, final Token opcode) {
        if (MaskingType.Statement.equals(maskingStrategy.getMaskingType()) &&
                maskingStrategy.getBlockId().equals(getStatementId(node))) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
        }
    }

    @Override
    public void visitChildren(ASTNode node) {
        for (final ASTNode child : node.getChildren()) {
            if (AstNodeUtil.isMetadata(child)) {
                continue;
            }
            child.accept(this);
        }
    }

    @Override
    public void visit(Script node) {
        addToken(Token.BEGIN_SCRIPT);
        visitChildren(node);
        addToken(Token.END_SCRIPT);
    }

    @Override
    public void visit(ActorDefinition node) {
        addToken(Token.BEGIN);
        visitChildren(node.getProcedureDefinitionList());
        visitChildren(node.getScripts());
        addToken(Token.END);
    }

    @Override
    public void visit(UnspecifiedStmt node) {
        addToken(Token.NOTHING);
    }

    // region motion

    @Override
    public void visit(MoveSteps node) {
        visit(node, Token.MOTION_MOVESTEPS);
    }

    @Override
    public void visit(TurnLeft node) {
        visit(node, Token.MOTION_TURNLEFT);
    }

    @Override
    public void visit(TurnRight node) {
        visit(node, Token.MOTION_TURNRIGHT);
    }

    @Override
    public void visit(GoToPos node) {
        visit(node, Token.MOTION_GOTO);
    }

    @Override
    public void visit(GoToPosXY node) {
        visit(node, Token.MOTION_GOTOXY);
    }

    @Override
    public void visit(GlideSecsTo node) {
        visit(node, Token.MOTION_GLIDESECSTO);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        visit(node, Token.MOTION_GLIDESECSTOXY);
    }

    @Override
    public void visit(PointInDirection node) {
        visit(node, Token.MOTION_POINTINDIRECTION);
    }

    @Override
    public void visit(PointTowards node) {
        visit(node, Token.MOTION_POINTTOWARDS);
    }

    @Override
    public void visit(ChangeXBy node) {
        visit(node, Token.MOTION_CHANGEXBY);
    }

    @Override
    public void visit(ChangeYBy node) {
        visit(node, Token.MOTION_CHANGEYBY);
    }

    @Override
    public void visit(SetXTo node) {
        visit(node, Token.MOTION_SETX);
    }

    @Override
    public void visit(SetYTo node) {
        visit(node, Token.MOTION_SETY);
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        visit(node, Token.MOTION_IFONEDGEBOUNCE);
    }

    @Override
    public void visit(SetRotationStyle node) {
        visit(node, Token.MOTION_SETROTATIONSTYLE);
    }

    // endregion motion

    // region looks

    @Override
    public void visit(Say node) {
        visit(node, Token.LOOKS_SAY);
    }

    @Override
    public void visit(SayForSecs node) {
        visit(node, Token.LOOKS_SAYFORSECS);
    }

    @Override
    public void visit(Think node) {
        visit(node, Token.LOOKS_THINK);
    }

    @Override
    public void visit(ThinkForSecs node) {
        visit(node, Token.LOOKS_THINKFORSECS);
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        visit(node, Token.LOOKS_SWITCHCOSTUMETO);
    }

    @Override
    public void visit(NextCostume node) {
        visit(node, Token.LOOKS_NEXTCOSTUME);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        visit(node, Token.LOOKS_SWITCHBACKDROPTO);
    }

    @Override
    public void visit(NextBackdrop node) {
        visit(node, Token.LOOKS_NEXTBACKDROP);
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        visit(node, Token.LOOKS_SWITCHBACKDROPTOANDWAIT);
    }

    @Override
    public void visit(ChangeSizeBy node) {
        visit(node, Token.LOOKS_CHANGESIZEBY);
    }

    @Override
    public void visit(SetSizeTo node) {
        visit(node, Token.LOOKS_SETSIZETO);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        visit(node, Token.LOOKS_CHANGEEFFECTBY);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        visit(node, Token.LOOKS_SETEFFECTTO);
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        visit(node, Token.LOOKS_CLEARGRAPHICEFFECTS);
    }

    @Override
    public void visit(Show node) {
        visit(node, Token.LOOKS_SHOW);
    }

    @Override
    public void visit(Hide node) {
        visit(node, Token.LOOKS_HIDE);
    }

    @Override
    public void visit(ChangeLayerBy node) {
        visit(node, Token.LOOKS_GOFORWARDBACKWARDLAYERS);
    }

    @Override
    public void visit(GoToLayer node) {
        visit(node, Token.LOOKS_GOTOFRONTBACK);
    }

    // endregion looks

    // region sound

    @Override
    public void visit(PlaySoundUntilDone node) {
        visit(node, Token.SOUND_PLAYUNTILDONE);
    }

    @Override
    public void visit(StartSound node) {
        visit(node, Token.SOUND_PLAY);
    }

    @Override
    public void visit(StopAllSounds node) {
        visit(node, Token.SOUND_STOPALLSOUNDS);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        visit(node, Token.SOUND_CHANGEEFFECTBY);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        visit(node, Token.SOUND_SETEFFECTTO);
    }

    @Override
    public void visit(ClearSoundEffects node) {
        visit(node, Token.SOUND_CLEAREFFECTS);
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        visit(node, Token.SOUND_CHANGEVOLUMEBY);
    }

    @Override
    public void visit(SetVolumeTo node) {
        visit(node, Token.SOUND_SETVOLUMETO);
    }

    // endregion sound

    // region events

    @Override
    public void visit(GreenFlag node) {
        visit(node, Token.EVENT_WHENFLAGCLICKED);
    }

    @Override
    public void visit(KeyPressed node) {
        visit(node, Token.EVENT_WHENKEYPRESSED);
    }

    @Override
    public void visit(StageClicked node) {
        visit(node, Token.EVENT_WHENSTAGECLICKED);
    }

    @Override
    public void visit(SpriteClicked node) {
        visit(node, Token.EVENT_WHENTHISSPRITECLICKED);
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        visit(node, Token.EVENT_WHENBACKDROPSWITCHESTO);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        visit(node, Token.EVENT_WHENGREATERTHAN);
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        visit(node, Token.EVENT_WHENBROADCASTRECEIVED);
    }

    @Override
    public void visit(Broadcast node) {
        visit(node, Token.EVENT_BROADCAST);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        visit(node, Token.EVENT_BROADCASTANDWAIT);
    }

    @Override
    public void visit(Never node) {
        visit(node, Token.EVENT_NEVER);
    }

    // endregion events

    // region control

    @Override
    public void visit(WaitSeconds node) {
        visit(node, Token.CONTROL_WAIT);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        visitControlBlock(node, Token.CONTROL_FOREVER);
    }

    @Override
    public void visit(IfThenStmt node) {
        visitControlBlock(node, Token.CONTROL_IF);
    }

    @Override
    public void visit(IfElseStmt node) {
        visitControlBlock(node, Token.CONTROL_IF_ELSE);
    }

    @Override
    public void visit(WaitUntil node) {
        visit(node, Token.CONTROL_WAIT_UNTIL);
    }

    @Override
    public void visit(UntilStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT_UNTIL);
    }

    @Override
    public void visit(StopAll node) {
        visitStop(node);
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop(node);
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop(node);
    }

    private void visitStop(ASTNode node) {
        visit(node, Token.CONTROL_STOP);
    }

    @Override
    public void visit(StartedAsClone node) {
        visit(node, Token.CONTROL_START_AS_CLONE);
    }

    @Override
    public void visit(CreateCloneOf node) {
        visit(node, Token.CONTROL_CREATE_CLONE_OF);
    }

    @Override
    public void visit(DeleteClone node) {
        visit(node, Token.CONTROL_DELETE_THIS_CLONE);
    }

    // endregion control

    // region sensing

    @Override
    public void visit(AskAndWait node) {
        visit(node, Token.SENSING_ASKANDWAIT);
    }

    @Override
    public void visit(SetDragMode node) {
        visit(node, Token.SENSING_SETDRAGMODE);
    }

    @Override
    public void visit(ResetTimer node) {
        visit(node, Token.SENSING_RESETTIMER);
    }

    // endregion sensing

    // region variables

    @Override
    public void visit(SetVariableTo node) {
        visit(node, Token.DATA_SETVARIABLETO);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        visit(node, Token.DATA_CHANGEVARIABLEBY);
    }

    @Override
    public void visit(ShowVariable node) {
        visit(node, Token.DATA_SHOWVARIABLE);
    }

    @Override
    public void visit(HideVariable node) {
        visit(node, Token.DATA_HIDEVARIABLE);
    }

    @Override
    public void visit(AddTo node) {
        visit(node, Token.DATA_ADDTOLIST);
    }

    @Override
    public void visit(DeleteOf node) {
        visit(node, Token.DATA_DELETEOFLIST);
    }

    @Override
    public void visit(DeleteAllOf node) {
        visit(node, Token.DATA_DELETEALLOFLIST);
    }

    @Override
    public void visit(InsertAt node) {
        visit(node, Token.DATA_INSERTATLIST);
    }

    @Override
    public void visit(ReplaceItem node) {
        visit(node, Token.DATA_REPLACEITEMOFLIST);
    }

    @Override
    public void visit(ShowList node) {
        visit(node, Token.DATA_SHOWLIST);
    }

    @Override
    public void visit(HideList node) {
        visit(node, Token.DATA_HIDELIST);
    }

    // endregion variables

    // region custom blocks

    @Override
    public void visit(CallStmt node) {
        if (abstractTokens) {
            addToken(AbstractToken.CUSTOM_BLOCK);
        } else {
            final String fullProcedureName = node.getIdent().getName();
            final String name = AstNodeUtil.replaceProcedureParams(fullProcedureName, "");
            tokens.add(StringUtil.normaliseString(name));
        }

        node.getExpressions().accept(this);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        addToken(Token.BEGIN_PROCEDURE);

        if (abstractTokens) {
            addToken(AbstractToken.PROCEDURE_DEFINITION);
        } else {
            final String fullName = procedureNameMapping.getProcedureInfo(node).getName();
            final String name = AstNodeUtil.replaceProcedureParams(fullName, "");
            tokens.add(StringUtil.normaliseString(name));
        }

        node.getStmtList().accept(this);

        addToken(Token.END_PROCEDURE);
    }

    // endregion custom blocks

    // region pen

    @Override
    public void visit(PenDownStmt node) {
        visit(node, Token.PEN_PENDOWN);
    }

    @Override
    public void visit(PenUpStmt node) {
        visit(node, Token.PEN_PENUP);
    }

    @Override
    public void visit(PenClearStmt node) {
        visit(node, Token.PEN_CLEAR);
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        visit(node, Token.PEN_SETPENCOLORTOCOLOR);
    }

    @Override
    public void visit(PenStampStmt node) {
        visit(node, Token.PEN_STAMP);
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        visit(node, Token.PEN_CHANGECOLORBY);
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        visit(node, Token.PEN_SETCOLORTO);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        visit(node, Token.PEN_SETPENSIZETO);
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        visit(node, Token.PEN_CHANGEPENSIZEBY);
    }

    // endregion pen

    // region tts

    @Override
    public void visit(SetLanguage node) {
        visit(node, Token.TTS_SETLANGUAGE);
    }

    @Override
    public void visit(SetVoice node) {
        visit(node, Token.TTS_SETVOICE);
    }

    @Override
    public void visit(Speak node) {
        visit(node, Token.TTS_SPEAK);
    }

    // endregion tts

    // region music

    @Override
    public void visit(ChangeTempoBy node) {
        visit(node, Token.MUSIC_CHANGETEMPOBY);
    }

    @Override
    public void visit(SetTempoTo node) {
        visit(node, Token.MUSIC_SETTEMPOTO);
    }

    @Override
    public void visit(SetInstrumentTo node) {
        visit(node, Token.MUSIC_SETINSTRUMENTTO);
    }

    @Override
    public void visit(PlayDrumForBeats node) {
        visit(node, Token.MUSIC_PLAYDRUMFORBEATS);
    }

    @Override
    public void visit(PlayNoteForBeats node) {
        visit(node, Token.MUSIC_PLAYNOTEFORBEATS);
    }

    @Override
    public void visit(RestForBeats node) {
        visit(node, Token.MUSIC_RESTFORBEATS);
    }

    // endregion music
}
