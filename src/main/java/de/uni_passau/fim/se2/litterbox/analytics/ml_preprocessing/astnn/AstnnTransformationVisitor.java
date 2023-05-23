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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.*;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.BaseTokenVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
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
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;

import java.util.*;
import java.util.logging.Logger;

class AstnnTransformationVisitor implements
        ScratchVisitor, MusicExtensionVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor,
        TranslateExtensionVisitor {

    private static final Logger log = Logger.getLogger(AstnnTransformationVisitor.class.getName());

    /**
     * Remove the parameters from the procedure definition and call block names.
     */
    private static final String PROCEDURE_PARAM_REPLACEMENT = "";

    private final BaseTokenVisitor tokenVisitor = TokenVisitorFactory.getDefaultTokenVisitor(true);

    private final ProcedureDefinitionNameMapping procedureNameMapping;
    private final boolean abstractTokens;

    /**
     * Emulates the return type of the visit methods since the visitor is
     * defined to use {@code void} methods instead of being generic on the
     * return type.
     */
    private Optional<AstnnNode> nodeTracker = Optional.empty();

    AstnnTransformationVisitor(final ProcedureDefinitionNameMapping procedureNameMapping, boolean abstractTokens) {
        this.procedureNameMapping = procedureNameMapping;
        this.abstractTokens = abstractTokens;
    }

    AstnnNode getResult() {
        return nodeTracker.orElseThrow();
    }

    @Override
    public void visit(ASTNode node) {
        for (final var child : node.getChildren()) {
            if (!AstNodeUtil.isMetadata(child)) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Script node) {
        final AstnnNode event = transformNode(node.getEvent());
        final AstnnNode stmtBlock = transformStmtListBlock(node.getStmtList());

        finishVisit(AstnnAstNodeFactory.blockStatement(StatementType.SCRIPT, List.of(event), stmtBlock));
    }

    @Override
    public void visit(ActorDefinition node) {
        final String name = getActorName(node);
        final List<AstnnNode> procedures = transformNodes(node.getProcedureDefinitionList().getList());
        final List<AstnnNode> scripts = transformNodes(node.getScripts().getScriptList());

        finishVisit(AstnnAstNodeFactory.actorDefinition(name, procedures, scripts));
    }

    private String getActorName(final ActorDefinition node) {
        if (abstractTokens) {
            if (node.isStage()) {
                return AbstractToken.STAGE.name();
            } else {
                return AbstractToken.SPRITE.name();
            }
        } else {
            return NodeNameUtil.normalizeSpriteName(node).orElse(NodeType.EMPTY_STRING.toString());
        }
    }

    @Override
    public void visit(UnspecifiedStmt node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.UNKNOWN_STMT));
    }

    @Override
    public void visit(UnspecifiedExpression node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.UNKNOWN));
    }

    // region motion

    @Override
    public void visit(MoveSteps node) {
        final AstnnNode steps = transformNode(node.getSteps());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_MOVESTEPS, steps));
    }

    @Override
    public void visit(TurnLeft node) {
        final AstnnNode degrees = transformNode(node.getDegrees());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_TURNLEFT, degrees));
    }

    @Override
    public void visit(TurnRight node) {
        final AstnnNode degrees = transformNode(node.getDegrees());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_TURNRIGHT, degrees));
    }

    @Override
    public void visit(GoToPos node) {
        final AstnnNode pos = transformNode(node.getPosition());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_GOTO, pos));
    }

    @Override
    public void visit(GoToPosXY node) {
        final AstnnNode x = transformNode(node.getX());
        final AstnnNode y = transformNode(node.getY());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_GOTOXY, x, y));
    }

    @Override
    public void visit(GlideSecsTo node) {
        final AstnnNode pos = transformNode(node.getPosition());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_GLIDETO, pos));
    }

    @Override
    public void visit(GlideSecsToXY node) {
        final AstnnNode x = transformNode(node.getX());
        final AstnnNode y = transformNode(node.getY());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_GLIDETOXY, x, y));
    }

    @Override
    public void visit(PointInDirection node) {
        final AstnnNode direction = transformNode(node.getDirection());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_POINTINDIRECTION, direction));
    }

    @Override
    public void visit(PointTowards node) {
        final AstnnNode direction = transformNode(node.getPosition());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_POINTTOWARDS, direction));
    }

    @Override
    public void visit(ChangeXBy node) {
        final AstnnNode by = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_CHANGEXBY, by));
    }

    @Override
    public void visit(ChangeYBy node) {
        final AstnnNode by = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_CHANGEYBY, by));
    }

    @Override
    public void visit(SetXTo node) {
        final AstnnNode to = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_SETX, to));
    }

    @Override
    public void visit(SetYTo node) {
        final AstnnNode to = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_SETY, to));
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_IFONEDGEBOUNCE));
    }

    @Override
    public void visit(SetRotationStyle node) {
        final AstnnNode style = transformNode(node.getRotation());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MOTION_SETROTATIONSTYLE, style));
    }

    @Override
    public void visit(PositionX node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_XPOSITION));
    }

    @Override
    public void visit(PositionY node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_YPOSITION));
    }

    @Override
    public void visit(Direction node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_DIRECTION));
    }

    @Override
    public void visit(RandomPos node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_RANDOMPOSITION));
    }

    @Override
    public void visit(MousePos node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_MOUSEPOINTER));
    }

    @Override
    public void visit(MousePointer node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MOTION_MOUSEPOINTER));
    }

    @Override
    public void visit(RotationStyle node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(DragMode node) {
        visitFixedChoice(node);
    }

    // endregion motion

    // region looks

    @Override
    public void visit(Say node) {
        final AstnnNode text = transformNode(node.getString());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SAY, text));
    }

    @Override
    public void visit(SayForSecs node) {
        final AstnnNode text = transformNode(node.getString());
        final AstnnNode duration = transformNode(node.getSecs());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SAYFORSECS, text, duration));
    }

    @Override
    public void visit(Think node) {
        final AstnnNode text = transformNode(node.getThought());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_THINK, text));
    }

    @Override
    public void visit(ThinkForSecs node) {
        final AstnnNode text = transformNode(node.getThought());
        final AstnnNode duration = transformNode(node.getSecs());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_THINKFORSECS, text, duration));
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        final AstnnNode costume = transformNode(node.getCostumeChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SWITCHCOSTUMETO, costume));
    }

    @Override
    public void visit(NextCostume node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_NEXTCOSTUME));
    }

    @Override
    public void visit(SwitchBackdrop node) {
        final AstnnNode backdrop = transformNode(node.getElementChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SWITCHBACKDROPTO, backdrop));
    }

    @Override
    public void visit(NextBackdrop node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_NEXTBACKDROP));
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        final AstnnNode backdrop = transformNode(node.getElementChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SWITCHBACKDROPTOANDWAIT, backdrop));
    }

    @Override
    public void visit(ChangeSizeBy node) {
        final AstnnNode by = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_CHANGESIZEBY, by));
    }

    @Override
    public void visit(SetSizeTo node) {
        final AstnnNode to = transformNode(node.getPercent());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SETSIZETO, to));
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        final AstnnNode effect = transformNode(node.getEffect());
        final AstnnNode by = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_CHANGEEFFECTBY, effect, by));
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        final AstnnNode effect = transformNode(node.getEffect());
        final AstnnNode to = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SETEFFECTTO, effect, to));
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_CLEARGRAPHICEFFECTS));
    }

    @Override
    public void visit(Show node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_SHOW));
    }

    @Override
    public void visit(Hide node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_HIDE));
    }

    @Override
    public void visit(ChangeLayerBy node) {
        final AstnnNode direction = transformNode(node.getForwardBackwardChoice());
        final AstnnNode by = transformNode(node.getNum());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_GOFORWARDBACKWARDLAYERS, direction, by));
    }

    @Override
    public void visit(GoToLayer node) {
        final AstnnNode direction = transformNode(node.getLayerChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.LOOKS_GOTOFRONTBACK, direction));
    }

    @Override
    public void visit(Costume node) {
        final AstnnNode nameNum = transformNode(node.getType());
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_COSTUMENUMBERNAME, nameNum));
    }

    @Override
    public void visit(Backdrop node) {
        final AstnnNode nameNum = transformNode(node.getType());
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_BACKDROPNUMBERNAME, nameNum));
    }

    @Override
    public void visit(Size node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_SIZE));
    }

    @Override
    public void visit(GraphicEffect node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(LayerChoice node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(Next node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_NEXTBACKDROP));
    }

    @Override
    public void visit(Prev node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_PREVBACKDROP));

    }

    @Override
    public void visit(Random node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.LOOKS_RANDOMBACKDROP));
    }

    // endregion looks

    // region sound

    @Override
    public void visit(PlaySoundUntilDone node) {
        final AstnnNode sound = transformNode(node.getElementChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_PLAYUNTILDONE, sound));
    }

    @Override
    public void visit(StartSound node) {
        final AstnnNode sound = transformNode(node.getElementChoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_PLAY, sound));
    }

    @Override
    public void visit(StopAllSounds node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_STOPALLSOUNDS));
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        final AstnnNode effect = transformNode(node.getEffect());
        final AstnnNode by = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_CHANGEEFFECTBY, effect, by));
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        final AstnnNode effect = transformNode(node.getEffect());
        final AstnnNode to = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_SETEFFECTTO, effect, to));
    }

    @Override
    public void visit(ClearSoundEffects node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_CLEAREFFECTS));
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        final AstnnNode by = transformNode(node.getVolumeValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_CHANGEVOLUMEBY, by));
    }

    @Override
    public void visit(SetVolumeTo node) {
        final AstnnNode to = transformNode(node.getVolumeValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SOUND_SETVOLUMETO, to));
    }

    @Override
    public void visit(Volume node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SOUND_VOLUME));
    }

    @Override
    public void visit(SoundEffect node) {
        visitFixedChoice(node);
    }

    // endregion sound

    // region events

    @Override
    public void visit(GreenFlag node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENFLAGCLICKED));
    }

    @Override
    public void visit(KeyPressed node) {
        final AstnnNode key = transformNode(node.getKey());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENKEYPRESSED, key));
    }

    @Override
    public void visit(StageClicked node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENSTAGECLICKED));
    }

    @Override
    public void visit(SpriteClicked node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENTHISSPRITECLICKED));
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        final AstnnNode backdrop = transformNode(node.getBackdrop());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENBACKDROPSWITCHESTO, backdrop));
    }

    @Override
    public void visit(AttributeAboveValue node) {
        final AstnnNode attribute = transformNode(node.getAttribute());
        final AstnnNode value = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENGREATERTHAN, attribute, value));
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        final AstnnNode message = transformNode(node.getMsg());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_WHENBROADCASTRECEIVED, message));
    }

    @Override
    public void visit(Broadcast node) {
        final AstnnNode message = transformNode(node.getMessage());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_BROADCAST, message));
    }

    @Override
    public void visit(BroadcastAndWait node) {
        final AstnnNode message = transformNode(node.getMessage());
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_BROADCASTANDWAIT, message));
    }

    @Override
    public void visit(Never node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.EVENT_NEVER));
    }

    @Override
    public void visit(EventAttribute node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(Message node) {
        final AstnnNode message = transformNode(node.getMessage());
        finishVisit(AstnnAstNodeFactory.build(NodeType.EVENT_MESSAGE, message));
    }

    // endregion events

    // region control

    @Override
    public void visit(WaitSeconds node) {
        final AstnnNode seconds = transformNode(node.getSeconds());
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_WAIT, seconds));
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        final AstnnNode times = transformNode(node.getTimes());
        final AstnnNode stmts = transformStmtListBlock(node.getStmtList());

        finishVisit(AstnnAstNodeFactory.blockStatement(StatementType.CONTROL_REPEAT_TIMES, List.of(times), stmts));
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        final AstnnNode stmts = transformStmtListBlock(node.getStmtList());

        finishVisit(AstnnAstNodeFactory.blockStatement(StatementType.CONTROL_REPEAT, stmts));
    }

    @Override
    public void visit(IfThenStmt node) {
        final AstnnNode ifStmt = transformIf(node);
        finishVisit(AstnnAstNodeFactory.build("if", List.of(ifStmt)));
    }

    @Override
    public void visit(IfElseStmt node) {
        final AstnnNode ifStmt = transformIf(node);

        final AstnnNode elseStmtBlock = transformStmtListBlock(node.getElseStmts());
        final AstnnNode elseStmt = AstnnAstNodeFactory.blockStatement(StatementType.CONTROL_ELSE, elseStmtBlock);

        finishVisit(AstnnAstNodeFactory.build("if", List.of(ifStmt, elseStmt)));
    }

    private AstnnNode transformIf(final IfStmt node) {
        final AstnnNode condition = transformNode(node.getBoolExpr());
        final AstnnNode stmtBlock = transformStmtListBlock(node.getThenStmts());

        return AstnnAstNodeFactory.blockStatement(StatementType.CONTROL_IF, List.of(condition), stmtBlock);
    }

    @Override
    public void visit(WaitUntil node) {
        final AstnnNode condition = transformNode(node.getUntil());
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_WAIT_UNTIL, condition));
    }

    @Override
    public void visit(UntilStmt node) {
        final AstnnNode condition = transformNode(node.getBoolExpr());
        final AstnnNode stmtBlock = transformStmtListBlock(node.getStmtList());

        final AstnnNode result = AstnnAstNodeFactory.blockStatement(
                StatementType.CONTROL_REPEAT_UNTIL,
                List.of(condition),
                stmtBlock
        );
        finishVisit(result);
    }

    @Override
    public void visit(StopAll node) {
        visitStop("ALL");
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop("OTHER_SCRIPTS");
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop("THIS_SCRIPT");
    }

    private void visitStop(final String choice) {
        final AstnnNode child = AstnnAstNodeFactory.build(choice);
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_STOP, child));
    }

    @Override
    public void visit(StartedAsClone node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_START_AS_CLONE));
    }

    @Override
    public void visit(CreateCloneOf node) {
        final AstnnNode of = transformNode(node.getStringExpr());
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_CREATE_CLONE_OF, of));
    }

    @Override
    public void visit(DeleteClone node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.CONTROL_DELETE_THIS_CLONE));
    }

    // endregion control

    // region sensing

    @Override
    public void visit(Touching node) {
        final AstnnNode touchable = transformNode(node.getTouchable());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_TOUCHINGOBJECT, touchable));
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        final AstnnNode color = transformNode(node.getColor());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_TOUCHINGCOLOR, color));
    }

    @Override
    public void visit(ColorTouchingColor node) {
        final AstnnNode color1 = transformNode(node.getOperand1());
        final AstnnNode color2 = transformNode(node.getOperand2());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_COLORISTOUCHINGCOLOR, color1, color2));
    }

    @Override
    public void visit(DistanceTo node) {
        final AstnnNode touchable = transformNode(node.getPosition());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_DISTANCETO, touchable));
    }

    @Override
    public void visit(AskAndWait node) {
        final AstnnNode question = transformNode(node.getQuestion());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SENSING_ASKANDWAIT, question));
    }

    @Override
    public void visit(Answer node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_ANSWER));
    }

    @Override
    public void visit(IsKeyPressed node) {
        final AstnnNode key = transformNode(node.getKey());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_KEYPRESSED, key));
    }

    @Override
    public void visit(IsMouseDown node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_MOUSEDOWN));
    }

    @Override
    public void visit(MouseX node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_MOUSEX));
    }

    @Override
    public void visit(MouseY node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_MOUSEY));
    }

    @Override
    public void visit(SetDragMode node) {
        final AstnnNode mode = transformNode(node.getDrag());
        finishVisit(AstnnAstNodeFactory.build(StatementType.SENSING_SETDRAGMODE, mode));
    }

    @Override
    public void visit(Loudness node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_LOUDNESS));
    }

    @Override
    public void visit(Timer node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_TIMER));
    }

    @Override
    public void visit(ResetTimer node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.SENSING_RESETTIMER));
    }

    @Override
    public void visit(AttributeOf node) {
        final AstnnNode attribute = transformNode(node.getAttribute());
        final AstnnNode sprite = transformNode(node.getElementChoice());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_OF, attribute, sprite));
    }

    @Override
    public void visit(Current node) {
        final AstnnNode item = transformNode(node.getTimeComp());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_CURRENT, item));
    }

    @Override
    public void visit(DaysSince2000 node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_DAYSSINCE2000));
    }

    @Override
    public void visit(Username node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_USERNAME));
    }

    @Override
    public void visit(TimeComp node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(Key node) {
        final AstnnNode key = transformNode(node.getKey());
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_KEY, key));
    }

    @Override
    public void visit(Edge node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.SENSING_EDGE));
    }

    // endregion sensing

    // region operators

    @Override
    public void visit(Add node) {
        visitBinaryExpr(NodeType.OPERATOR_ADD, node);
    }

    @Override
    public void visit(Minus node) {
        visitBinaryExpr(NodeType.OPERATOR_SUBTRACT, node);
    }

    @Override
    public void visit(Mult node) {
        visitBinaryExpr(NodeType.OPERATOR_MULTIPLY, node);
    }

    @Override
    public void visit(Div node) {
        visitBinaryExpr(NodeType.OPERATOR_DIVIDE, node);
    }

    @Override
    public void visit(PickRandom node) {
        visitBinaryExpr(NodeType.OPERATOR_RANDOM, node);
    }

    @Override
    public void visit(BiggerThan node) {
        visitBinaryExpr(NodeType.OPERATOR_GT, node);
    }

    @Override
    public void visit(LessThan node) {
        visitBinaryExpr(NodeType.OPERATOR_LT, node);
    }

    @Override
    public void visit(Equals node) {
        visitBinaryExpr(NodeType.OPERATOR_EQUALS, node);
    }

    @Override
    public void visit(And node) {
        visitBinaryExpr(NodeType.OPERATOR_AND, node);
    }

    @Override
    public void visit(Or node) {
        visitBinaryExpr(NodeType.OPERATOR_OR, node);
    }

    @Override
    public void visit(Not node) {
        visitUnaryExpr(NodeType.OPERATOR_NOT, node);
    }

    @Override
    public void visit(Join node) {
        visitBinaryExpr(NodeType.OPERATOR_JOIN, node);
    }

    @Override
    public void visit(LetterOf node) {
        final AstnnNode letterPos = transformNode(node.getNum());
        final AstnnNode string = transformNode(node.getStringExpr());
        finishVisit(AstnnAstNodeFactory.build(NodeType.OPERATOR_LETTER_OF, letterPos, string));
    }

    @Override
    public void visit(LengthOfString node) {
        final AstnnNode string = transformNode(node.getStringExpr());
        finishVisit(AstnnAstNodeFactory.build(NodeType.OPERATOR_LENGTH, string));
    }

    @Override
    public void visit(StringContains node) {
        final AstnnNode containing = transformNode(node.getContaining());
        final AstnnNode contained = transformNode(node.getContained());
        finishVisit(AstnnAstNodeFactory.build(NodeType.OPERATOR_CONTAINS, containing, contained));
    }

    @Override
    public void visit(Mod node) {
        visitBinaryExpr(NodeType.OPERATOR_MOD, node);
    }

    @Override
    public void visit(Round node) {
        visitUnaryExpr(NodeType.OPERATOR_ROUND, node);
    }

    @Override
    public void visit(NumFunctOf node) {
        visitBinaryExpr(NodeType.OPERATOR_MATHOP, node);
    }

    @Override
    public void visit(NumFunct node) {
        visitFixedChoice(node);
    }

    private void visitBinaryExpr(final NodeType nodeType, final BinaryExpression<?, ?> expr) {
        final AstnnNode left = transformNode(expr.getOperand1());
        final AstnnNode right = transformNode(expr.getOperand2());
        finishVisit(AstnnAstNodeFactory.build(nodeType, left, right));
    }

    private void visitUnaryExpr(final NodeType nodeType, final UnaryExpression<?> expr) {
        final AstnnNode child = transformNode(expr.getOperand1());
        finishVisit(AstnnAstNodeFactory.build(nodeType, child));
    }



    // endregion operators

    // region variables

    @Override
    public void visit(SetVariableTo node) {
        final AstnnNode variable = transformNode(node.getIdentifier());
        final AstnnNode value = transformNode(node.getExpr());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_SETVARIABLETO, variable, value));
    }

    @Override
    public void visit(ChangeVariableBy node) {
        final AstnnNode variable = transformNode(node.getIdentifier());
        final AstnnNode value = transformNode(node.getExpr());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_CHANGEVARIABLEBY, variable, value));
    }

    @Override
    public void visit(ShowVariable node) {
        final AstnnNode variable = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_SHOWVARIABLE, variable));
    }

    @Override
    public void visit(HideVariable node) {
        final AstnnNode variable = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_HIDEVARIABLE, variable));
    }

    @Override
    public void visit(AddTo node) {
        final AstnnNode addition = transformNode(node.getString());
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_ADDTOLIST, addition, list));
    }

    @Override
    public void visit(DeleteOf node) {
        final AstnnNode deletionIdx = transformNode(node.getNum());
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_DELETEOFLIST, deletionIdx, list));
    }

    @Override
    public void visit(DeleteAllOf node) {
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_DELETEALLOFLIST, list));
    }

    @Override
    public void visit(InsertAt node) {
        final AstnnNode item = transformNode(node.getString());
        final AstnnNode index = transformNode(node.getIndex());
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_INSERTATLIST, item, index, list));
    }

    @Override
    public void visit(ReplaceItem node) {
        final AstnnNode index = transformNode(node.getIndex());
        final AstnnNode list = transformNode(node.getIdentifier());
        final AstnnNode newItem = transformNode(node.getString());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_REPLACEITEMOFLIST, index, list, newItem));
    }

    @Override
    public void visit(ItemOfVariable node) {
        final AstnnNode index = transformNode(node.getNum());
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_ITEMOFLIST, index, list));
    }

    @Override
    public void visit(IndexOf node) {
        final AstnnNode item = transformNode(node.getExpr());
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_ITEMNUMOFLIST, item, list));
    }

    @Override
    public void visit(LengthOfVar node) {
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_LENGTHOFLIST, list));
    }

    @Override
    public void visit(ListContains node) {
        final AstnnNode list = transformNode(node.getIdentifier());
        final AstnnNode item = transformNode(node.getElement());
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_LISTCONTAINSITEM, list, item));
    }

    @Override
    public void visit(ShowList node) {
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_SHOWLIST, list));
    }

    @Override
    public void visit(HideList node) {
        final AstnnNode list = transformNode(node.getIdentifier());
        finishVisit(AstnnAstNodeFactory.build(StatementType.DATA_HIDELIST, list));
    }

    // endregion variables

    // region custom blocks

    @Override
    public void visit(CallStmt node) {
        final List<AstnnNode> children = new ArrayList<>();

        final String procedureName = getProcedureName(node);
        final String called = AstNodeUtil.replaceProcedureParams(procedureName, PROCEDURE_PARAM_REPLACEMENT);
        final AstnnNode name = AstnnAstNodeFactory.build(StringUtil.normaliseString(called));

        children.add(name);
        children.addAll(transformExpressionList(node.getExpressions()));

        finishVisit(AstnnAstNodeFactory.build(StatementType.PROCEDURES_CALL, children));
    }

    private String getProcedureName(final CallStmt node) {
        if (abstractTokens) {
            return AbstractToken.CUSTOM_BLOCK.name();
        } else {
            return node.getIdent().getName();
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        final String name = getProcedureName(node);
        final List<AstnnNode> params = transformParameterDefinitions(node.getParameterDefinitionList());
        final AstnnNode body = transformStmtListBlock(node.getStmtList());

        finishVisit(AstnnAstNodeFactory.procedureDeclaration(name, params, body));
    }

    private String getProcedureName(final ProcedureDefinition procedureDefinition) {
        if (abstractTokens) {
            return AbstractToken.PROCEDURE_DEFINITION.name();
        } else {
            final String name = procedureNameMapping.getProcedureInfo(procedureDefinition).getName();
            final String actualName = AstNodeUtil.replaceProcedureParams(name, PROCEDURE_PARAM_REPLACEMENT);
            return StringUtil.normaliseString(actualName);
        }
    }

    private List<AstnnNode> transformParameterDefinitions(final ParameterDefinitionList params) {
        return transformNodes(params.getParameterDefinitions());
    }

    @Override
    public void visit(ParameterDefinition node) {
        final AstnnNode type = transformNode(node.getType());
        final AstnnNode name;
        if (abstractTokens) {
            name = AstnnAstNodeFactory.build(AbstractToken.PARAMETER.name());
        } else {
            name = transformNode(node.getIdent());
        }
        finishVisit(AstnnAstNodeFactory.build(NodeType.PROCEDURE_PARAMETER, type, name));
    }

    @Override
    public void visit(BooleanType node) {
        finishVisit(AstnnAstNodeFactory.build("BOOLEAN"));
    }

    @Override
    public void visit(ListType node) {
        finishVisit(AstnnAstNodeFactory.build("LIST"));
    }

    @Override
    public void visit(NumberType node) {
        finishVisit(AstnnAstNodeFactory.build("NUMBER"));
    }

    @Override
    public void visit(StringType node) {
        finishVisit(AstnnAstNodeFactory.build("STRING"));
    }

    // endregion custom blocks

    // region expressions

    @Override
    public void visit(NumberLiteral node) {
        finishVisit(AbstractToken.LITERAL_NUMBER, getNormalisedToken(node));
    }

    @Override
    public void visit(StringLiteral node) {
        finishVisit(AbstractToken.LITERAL_STRING, getNormalisedToken(node));
    }

    @Override
    public void visit(BoolLiteral node) {
        finishVisit(AbstractToken.LITERAL_BOOL, getNormalisedToken(node));
    }

    @Override
    public void visit(ColorLiteral node) {
        finishVisit(AbstractToken.LITERAL_COLOR, getNormalisedToken(node));
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.EMPTY_BOOL));
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.EMPTY_NUMBER));
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.EMPTY_STRING));
    }

    @Override
    public void visit(NameNum node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(FixedAttribute node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(StrId node) {
        finishVisit(AbstractToken.NODE_IDENTIFIER, getNormalisedToken(node));
    }

    @Override
    public void visit(Qualified node) {
        node.getSecond().accept(this);
    }

    @Override
    public void visit(ScratchList node) {
        final String label = StringUtil.normaliseString(node.getName().getName());
        final AstnnNode name = ifAbstractElseLabel(AbstractToken.LIST, label);
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_LIST, name));
    }

    @Override
    public void visit(Variable node) {
        final String label = StringUtil.normaliseString(node.getName().getName());
        final AstnnNode name = ifAbstractElseLabel(AbstractToken.VAR, label);
        finishVisit(AstnnAstNodeFactory.build(NodeType.DATA_VARIABLE, name));
    }

    // endregion expressions

    // region pens

    @Override
    public void visit(PenClearStmt node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_CLEAR));
    }

    @Override
    public void visit(PenStampStmt node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_STAMP));
    }

    @Override
    public void visit(PenDownStmt node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_PENDOWN));
    }

    @Override
    public void visit(PenUpStmt node) {
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_PENUP));
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        final AstnnNode color = transformNode(node.getColorExpr());
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_SETPENCOLORTOCOLOR, color));
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        final AstnnNode color = transformNode(node.getParam());
        final AstnnNode by = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_CHANGECOLORBY, color, by));
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        final AstnnNode color = transformNode(node.getParam());
        final AstnnNode to = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_SETCOLORTO, color, to));
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        final AstnnNode by = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_CHANGEPENSIZEBY, by));
    }

    @Override
    public void visit(SetPenSizeTo node) {
        final AstnnNode to = transformNode(node.getValue());
        finishVisit(AstnnAstNodeFactory.build(StatementType.PEN_SETPENSIZETO, to));
    }

    // endregion pens

    // region text to speech

    @Override
    public void visit(FixedLanguage node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(ExprLanguage node) {
        final AstnnNode language = transformNode(node.getExpr());
        finishVisit(AstnnAstNodeFactory.build(NodeType.TTS_LANGUAGE, language));
    }

    @Override
    public void visit(FixedVoice node) {
        visitFixedChoice(node);
    }

    @Override
    public void visit(ExprVoice node) {
        final AstnnNode voice = transformNode(node.getExpr());
        finishVisit(AstnnAstNodeFactory.build(NodeType.TTS_VOICE, voice));
    }

    @Override
    public void visit(SetLanguage node) {
        final AstnnNode text = transformNode(node.getLanguage());
        finishVisit(AstnnAstNodeFactory.build(StatementType.TTS_SETLANGUAGE, text));
    }

    @Override
    public void visit(SetVoice node) {
        final AstnnNode text = transformNode(node.getVoice());
        finishVisit(AstnnAstNodeFactory.build(StatementType.TTS_SETVOICE, text));
    }

    @Override
    public void visit(Speak node) {
        final AstnnNode text = transformNode(node.getText());
        finishVisit(AstnnAstNodeFactory.build(StatementType.TTS_SPEAK, text));
    }

    // endregion text to speech

    // region music

    @Override
    public void visit(Tempo node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.MUSIC_TEMPO));
    }

    @Override
    public void visit(ChangeTempoBy node) {
        final AstnnNode tempo = transformNode(node.getTempo());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_CHANGETEMPOBY, tempo));
    }

    @Override
    public void visit(SetTempoTo node) {
        final AstnnNode tempo = transformNode(node.getTempo());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_SETTEMPOTO, tempo));
    }

    @Override
    public void visit(SetInstrumentTo node) {
        final AstnnNode instrument = transformNode(node.getInstrument());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_SETINSTRUMENTTO, instrument));
    }

    @Override
    public void visit(PlayDrumForBeats node) {
        final AstnnNode drum = transformNode(node.getDrum());
        final AstnnNode beats = transformNode(node.getBeats());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_PLAYDRUMFORBEATS, drum, beats));
    }

    @Override
    public void visit(PlayNoteForBeats node) {
        final AstnnNode note = transformNode(node.getNote());
        final AstnnNode beats = transformNode(node.getBeats());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_PLAYDRUMFORBEATS, note, beats));
    }

    @Override
    public void visit(RestForBeats node) {
        final AstnnNode beats = transformNode(node.getBeats());
        finishVisit(AstnnAstNodeFactory.build(StatementType.MUSIC_RESTFORBEATS, beats));
    }

    @Override
    public void visit(FixedNote node) {
        finishVisit(AbstractToken.LITERAL_NUMBER, getNormalisedToken(node));
    }

    @Override
    public void visit(FixedInstrument node) {
        final AstnnNode voice = AstnnAstNodeFactory.build(getNormalisedToken(node));
        finishVisit(AstnnAstNodeFactory.build(NodeType.MUSIC_INSTRUMENT, voice));
    }

    @Override
    public void visit(FixedDrum node) {
        final AstnnNode voice = AstnnAstNodeFactory.build(getNormalisedToken(node));
        finishVisit(AstnnAstNodeFactory.build(NodeType.MUSIC_DRUM, voice));
    }

    // endregion music

    // region translate

    @Override
    public void visit(TranslateTo node) {
        final AstnnNode text = transformNode(node.getText());
        final AstnnNode language = transformNode(node.getLanguage());
        finishVisit(AstnnAstNodeFactory.build(StatementType.TRANSLATE_TO, text, language));
    }

    @Override
    public void visit(ViewerLanguage node) {
        finishVisit(AstnnAstNodeFactory.build(NodeType.TRANSLATE_VIEWER_LANGUAGE));
    }

    @Override
    public void visit(TExprLanguage node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(TFixedLanguage node) {
        visitFixedChoice(node);
    }

    // endregion translate

    // region helper methods

    private void visitFixedChoice(final FixedNodeOption option) {
        final AstnnNode node = AstnnAstNodeFactory.build(getNormalisedToken(option));
        finishVisit(node);
    }

    private AstnnNode ifAbstractElseLabel(final AbstractToken token, final String label) {
        if (abstractTokens) {
            return AstnnAstNodeFactory.build(token);
        } else {
            return AstnnAstNodeFactory.build(label);
        }
    }

    private void finishVisit(final AstnnNode node) {
        nodeTracker = Optional.of(node);
    }

    private void finishVisit(final AbstractToken token, final String label) {
        finishVisit(ifAbstractElseLabel(token, label));
    }

    private AstnnNode getNode() {
        final AstnnNode node = nodeTracker.orElseGet(() -> {
            log.warning("Could not parse a block! Returning placeholder node.");
            return AstnnAstNodeFactory.build(NodeType.UNKNOWN);
        });
        nodeTracker = Optional.empty();
        return node;
    }

    private List<AstnnNode> transformExpressionList(final ExpressionList expressionList) {
        return transformNodes(expressionList.getExpressions());
    }

    private List<AstnnNode> transformStmtList(final StmtList stmtList) {
        return transformNodes(stmtList.getStmts());
    }

    private AstnnNode transformStmtListBlock(final StmtList stmtList) {
        return AstnnAstNodeFactory.block(transformStmtList(stmtList));
    }

    private AstnnNode transformNode(final ASTNode node) {
        node.accept(this);
        return getNode();
    }

    private <T extends ASTNode> List<AstnnNode> transformNodes(final List<T> nodes) {
        final List<AstnnNode> result = new ArrayList<>();
        for (final ASTNode node : nodes) {
            node.accept(this);
            result.add(getNode());
        }
        return result;
    }

    private String getNormalisedToken(ASTNode node) {
        return TokenVisitorFactory.getToken(tokenVisitor, node);
    }

    // endregion helper methods
}
