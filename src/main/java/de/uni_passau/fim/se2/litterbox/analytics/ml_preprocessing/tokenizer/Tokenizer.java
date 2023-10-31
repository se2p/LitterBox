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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.BaseTokenVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingType;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Tokenizer
        implements ScratchVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor, MusicExtensionVisitor {

    private static final Logger log = Logger.getLogger(Tokenizer.class.getName());

    private final BaseTokenVisitor tokenVisitor = TokenVisitorFactory.getDefaultTokenVisitor(true);

    private final List<String> tokens = new ArrayList<>();

    private final boolean abstractTokens;

    private final boolean abstractFixedNodeOptions;

    private final MaskingStrategy maskingStrategy;

    private final ProcedureDefinitionNameMapping procedureNameMapping;

    private Tokenizer(final ProcedureDefinitionNameMapping procedureNameMapping,
                      final boolean abstractTokens,
                      final boolean abstractFixedNodeOptions,
                      final MaskingStrategy maskingStrategy) {
        Preconditions.checkNotNull(procedureNameMapping);

        this.procedureNameMapping = procedureNameMapping;
        this.abstractTokens = abstractTokens;
        this.abstractFixedNodeOptions = abstractFixedNodeOptions;
        this.maskingStrategy = maskingStrategy;
    }

    public static List<String> tokenize(final Program program,
                                        final ASTNode node,
                                        final boolean abstractTokens,
                                        final boolean abstractFixedNodeOptions,
                                        final MaskingStrategy maskingStrategy) {
        return tokenize(program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions,
                maskingStrategy);
    }

    private static List<String> tokenize(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final ASTNode node,
            final boolean abstractTokens,
            final boolean abstractFixedNodeOptions,
            final MaskingStrategy maskingStrategy
    ) {
        final Tokenizer v = new Tokenizer(procedureNameMapping, abstractTokens, abstractFixedNodeOptions,
                maskingStrategy);
        node.accept(v);
        return v.tokens;
    }

    private void addToken(final Token opcode) {
        tokens.add(opcode.getStrRep());
    }

    private void addToken(final AbstractToken token) {
        tokens.add(token.name());
    }

    private void visit(final ASTNode node, final Token opcode) {
        if (MaskingType.Expression.equals(maskingStrategy.getMaskingType())
                && maskingStrategy.getBlockId().equals(getBlockId(node))) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
            visitChildren(node);
        }
    }

    private void visit(final ASTNode node, final String token) {
        tokens.add(token);
        visitChildren(node);
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

    @Override
    public void visit(PositionX node) {
        visit(node, Token.MOTION_XPOSITION);
    }

    @Override
    public void visit(PositionY node) {
        visit(node, Token.MOTION_YPOSITION);
    }

    @Override
    public void visit(Direction node) {
        visit(node, Token.MOTION_DIRECTION);
    }

    @Override
    public void visit(RandomPos node) {
        visit(node, Token.MOTION_RANDOMPOS);
    }

    @Override
    public void visit(MousePos node) {
        visit(node, Token.MOTION_MOUSEPOS);
    }

    @Override
    public void visit(MousePointer node) {
        visit(node, Token.MOTION_MOUSEPOINTER);
    }

    @Override
    public void visit(RotationStyle node) {
        visitFixedNodeOption(node, Token.MOTION_ROTATIONSTYLE);
    }

    @Override
    public void visit(DragMode node) {
        visitFixedNodeOption(node, Token.MOTION_DRAGMODE);
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

    @Override
    public void visit(Costume node) {
        visit(node, Token.LOOKS_COSTUMENUMBERNAME);
    }

    @Override
    public void visit(Backdrop node) {
        visit(node, Token.LOOKS_BACKDROP);
    }

    @Override
    public void visit(Size node) {
        visit(node, Token.LOOKS_SIZE);
    }

    @Override
    public void visit(GraphicEffect node) {
        visitFixedNodeOption(node, Token.LOOKS_GRAPHICEFFECT);
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_FORWARDBACKWARD);
    }

    @Override
    public void visit(LayerChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_LAYERCHOICE);
    }

    @Override
    public void visit(Next node) {
        visit(node, Token.LOOKS_NEXTBACKDROPCHOICE);
    }

    @Override
    public void visit(Prev node) {
        visit(node, Token.LOOKS_PREVBACKDROPCHOICE);
    }

    @Override
    public void visit(Random node) {
        visit(node, Token.LOOKS_RANDOMBACKDROPCHOICE);
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

    @Override
    public void visit(Volume node) {
        visit(node, Token.SOUND_VOLUME);
    }

    @Override
    public void visit(SoundEffect node) {
        visitFixedNodeOption(node, Token.SOUND_EFFECT);
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

    @Override
    public void visit(EventAttribute node) {
        visitFixedNodeOption(node, Token.EVENT_ATTRIBUTE);
    }

    @Override
    public void visit(Message node) {
        visit(node, Token.EVENT_MESSAGE);
    }

    // endregion events

    // region control

    @Override
    public void visit(WaitSeconds node) {
        visit(node, Token.CONTROL_WAIT);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        visit(node, Token.CONTROL_REPEAT);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        visit(node, Token.CONTROL_FOREVER);
    }

    @Override
    public void visit(IfThenStmt node) {
        visit(node, Token.CONTROL_IF);
    }

    @Override
    public void visit(IfElseStmt node) {
        visit(node, Token.CONTROL_IF_ELSE);
    }

    @Override
    public void visit(WaitUntil node) {
        visit(node, Token.CONTROL_WAIT_UNTIL);
    }

    @Override
    public void visit(UntilStmt node) {
        visit(node, Token.CONTROL_REPEAT_UNTIL);
    }

    @Override
    public void visit(StopAll node) {
        visitStop("all");
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop("other_scripts");
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop("this_script");
    }

    private void visitStop(final String target) {
        // TODO: Use 'abstractFixedNodeOptions' parameter here
        addToken(Token.CONTROL_STOP);
        if (!abstractTokens) {
            tokens.add(target);
        }
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
    public void visit(Touching node) {
        visit(node, Token.SENSING_TOUCHINGOBJECT);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        visit(node, Token.SENSING_TOUCHINGCOLOR);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        visit(node, Token.SENSING_COLORISTOUCHINGCOLOR);
    }

    @Override
    public void visit(DistanceTo node) {
        visit(node, Token.SENSING_DISTANCETO);
    }

    @Override
    public void visit(AskAndWait node) {
        visit(node, Token.SENSING_ASKANDWAIT);
    }

    @Override
    public void visit(Answer node) {
        visit(node, Token.SENSING_ANSWER);
    }

    @Override
    public void visit(IsKeyPressed node) {
        visit(node, Token.SENSING_KEYPRESSED);
    }

    @Override
    public void visit(IsMouseDown node) {
        visit(node, Token.SENSING_MOUSEDOWN);
    }

    @Override
    public void visit(MouseX node) {
        visit(node, Token.SENSING_MOUSEX);
    }

    @Override
    public void visit(MouseY node) {
        visit(node, Token.SENSING_MOUSEY);
    }

    @Override
    public void visit(SetDragMode node) {
        visit(node, Token.SENSING_SETDRAGMODE);
    }

    @Override
    public void visit(Loudness node) {
        visit(node, Token.SENSING_LOUDNESS);
    }

    @Override
    public void visit(Timer node) {
        visit(node, Token.SENSING_TIMER);
    }

    @Override
    public void visit(ResetTimer node) {
        visit(node, Token.SENSING_RESETTIMER);
    }

    @Override
    public void visit(AttributeOf node) {
        visit(node, Token.SENSING_OF);
    }

    @Override
    public void visit(Current node) {
        visit(node, Token.SENSING_CURRENT);
    }

    @Override
    public void visit(DaysSince2000 node) {
        visit(node, Token.SENSING_DAYSSINCE2000);
    }

    @Override
    public void visit(Username node) {
        visit(node, Token.SENSING_USERNAME);
    }

    @Override
    public void visit(TimeComp node) {
        visitFixedNodeOption(node, Token.TIME_COMP);
    }

    @Override
    public void visit(Key node) {
        // TODO: Use 'abstractFixedNodeOptions' parameter here
        if (abstractTokens) {
            addToken(Token.KEY);
        } else {
            visit(node, Token.KEY);
        }
    }

    @Override
    public void visit(Edge node) {
        visit(node, Token.SENSING_EDGE);
    }

    // endregion sensing

    // region operators

    @Override
    public void visit(Add node) {
        visit(node, Token.OPERATOR_ADD);
    }

    @Override
    public void visit(Minus node) {
        visit(node, Token.OPERATOR_SUBTRACT);
    }

    @Override
    public void visit(Mult node) {
        visit(node, Token.OPERATOR_MULTIPLY);
    }

    @Override
    public void visit(Div node) {
        visit(node, Token.OPERATOR_DIVIDE);
    }

    @Override
    public void visit(PickRandom node) {
        visit(node, Token.OPERATOR_RANDOM);
    }

    @Override
    public void visit(BiggerThan node) {
        visit(node, Token.OPERATOR_GT);
    }

    @Override
    public void visit(LessThan node) {
        visit(node, Token.OPERATOR_LT);
    }

    @Override
    public void visit(Equals node) {
        visit(node, Token.OPERATOR_EQUALS);
    }

    @Override
    public void visit(And node) {
        visit(node, Token.OPERATOR_AND);
    }

    @Override
    public void visit(Or node) {
        visit(node, Token.OPERATOR_OR);
    }

    @Override
    public void visit(Not node) {
        visit(node, Token.OPERATOR_NOT);
    }

    @Override
    public void visit(Join node) {
        visit(node, Token.OPERATOR_JOIN);
    }

    @Override
    public void visit(LetterOf node) {
        visit(node, Token.OPERATOR_LETTER_OF);
    }

    @Override
    public void visit(LengthOfString node) {
        visit(node, Token.OPERATOR_LENGTH);
    }

    @Override
    public void visit(StringContains node) {
        visit(node, Token.OPERATOR_CONTAINS);
    }

    @Override
    public void visit(Mod node) {
        visit(node, Token.OPERATOR_MOD);
    }

    @Override
    public void visit(Round node) {
        visit(node, Token.OPERATOR_ROUND);
    }

    @Override
    public void visit(NumFunctOf node) {
        visit(node, Token.OPERATOR_MATHOP);
    }

    @Override
    public void visit(NumFunct node) {
        visitFixedNodeOption(node, Token.NUM_FUNCT);
    }

    // endregion operators

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
    public void visit(ItemOfVariable node) {
        visit(node, Token.DATA_ITEMOFLIST);
    }

    @Override
    public void visit(IndexOf node) {
        visit(node, Token.DATA_ITEMNUMOFLIST);
    }

    @Override
    public void visit(LengthOfVar node) {
        visit(node, Token.DATA_LENGTHOFLIST);
    }

    @Override
    public void visit(ListContains node) {
        visit(node, Token.DATA_LISTCONTAINSITEM);
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

    // region expressions

    @Override
    public void visit(NumberLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_NUMBER, getNormalisedToken(node));
    }

    @Override
    public void visit(StringLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_STRING, getNormalisedToken(node));
    }

    @Override
    public void visit(BoolLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_BOOL, getNormalisedToken(node));
    }

    @Override
    public void visit(ColorLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_COLOR, getNormalisedToken(node));
    }

    @Override
    public void visit(UnspecifiedExpression node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(NameNum node) {
        visitFixedNodeOption(node, Token.LOOKS_BACKDROPNUMBERNAME);
    }

    @Override
    public void visit(FixedAttribute node) {
        visitFixedNodeOption(node, Token.ATTRIBUTE);
    }

    @Override
    public void visit(StrId node) {
        ifAbstractElse(AbstractToken.NODE_IDENTIFIER, getNormalisedToken(node));
    }

    @Override
    public void visit(Parameter node) {
        if (abstractTokens) {
            addToken(AbstractToken.PARAMETER);
        } else {
            node.getName().accept(this);
        }
    }

    @Override
    public void visit(Variable node) {
        final String name = StringUtil.normaliseString(node.getName().getName());
        ifAbstractElse(AbstractToken.VAR, name);
    }

    @Override
    public void visit(Qualified node) {
        // we don't care about the scope of variables
        node.getSecond().accept(this);
    }

    @Override
    public void visit(ScratchList node) {
        final String name = StringUtil.normaliseString(node.getName().getName());
        ifAbstractElse(AbstractToken.LIST, name);
    }

    // endregion expressions

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
    public void visit(Language node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(FixedLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(ExprLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(Voice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(FixedVoice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(ExprVoice node) {
        visit(node, Token.TTS_VOICE);
    }

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
    public void visit(Tempo node) {
        visit(node, Token.MUSIC_TEMPO);
    }

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

    @Override
    public void visit(FixedNote node) {
        ifAbstractElse(Token.MUSIC_LITERAL_NOTE, getNormalisedToken(node));
    }

    @Override
    public void visit(FixedInstrument node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_INSTRUMENT);
    }

    @Override
    public void visit(FixedDrum node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_DRUM);
    }

    // endregion music

    // region helper methods

    private String getBlockId(final ASTNode node) {
        try {
        if (node.getMetadata() instanceof DataBlockMetadata block) {
            return block.getBlockId();
        } else if (node.getMetadata() instanceof NonDataBlockMetadata block) {
            return block.getBlockId();
        } else if (node instanceof AttributeFromFixed attribute) {
            return getBlockId(attribute.getParentNode());
        }

        return null;
    }

    private void visitFixedNodeOption(final FixedNodeOption option, final Token opcode) {
        if (MaskingType.FixedOption.equals(maskingStrategy.getMaskingType())
                && maskingStrategy.getBlockId().equals(getBlockId(option.getParentNode()))) {
            addToken(Token.MASK);
        } else {
            if (abstractFixedNodeOptions) {
                visit(option, opcode);
            } else {
                visit(option, getNormalisedToken(option));
            }
        }
    }

    private void ifAbstractElse(final Token opcode, final String token) {
        if (abstractTokens) {
            addToken(opcode);
        } else {
            tokens.add(token);
        }
    }

    private void ifAbstractElse(final AbstractToken abstractToken, final String token) {
        if (abstractTokens) {
            addToken(abstractToken);
        } else {
            tokens.add(token);
        }
    }

    private String getNormalisedToken(final ASTNode node) {
        return TokenVisitorFactory.getToken(tokenVisitor, node);
    }

    // endregion helper methods
}
