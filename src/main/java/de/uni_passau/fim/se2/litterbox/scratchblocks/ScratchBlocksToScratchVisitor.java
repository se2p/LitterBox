/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksBaseVisitor;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksParser;

import java.util.Collections;
import java.util.List;

class ScratchBlocksToScratchVisitor extends ScratchBlocksBaseVisitor<ASTNode> {

    private static final String NEW_ACTOR_PREFIX = "//;Act ";

    private static final String SPECIAL_WITHOUT_BSLASH = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~]";

    private StrId currentActor = new StrId(new StringLiteral("Stage"));

    @Override
    public ASTNode visitActor(ScratchBlocksParser.ActorContext ctx) {
        final String actorName = ctx.BEGIN_ACTOR().getText().replace(NEW_ACTOR_PREFIX, "").trim();
        currentActor = new StrId(new StringLiteral(actorName));

        return super.visitActor(ctx);
    }

    @Override
    public ScriptEntity visitScript(ScratchBlocksParser.ScriptContext ctx) {
        if (ctx.expressionStmt() != null) {
            return new Script(new Never(), new StmtList(visitExpressionStmt(ctx.expressionStmt())));
        } else if (ctx.stmtList() != null) {
            if (ctx.event() != null) {
                return new Script((Event) visitEvent(ctx.event()), visitStmtList(ctx.stmtList()));
            } else {
                return new Script(new Never(), visitStmtList(ctx.stmtList()));
            }
        } else if (ctx.event() != null) {
            return new Script((Event) visitEvent(ctx.event()), new StmtList());
        } else if (ctx.customBlock() != null) {
            return visitCustomBlock(ctx.customBlock());
        } else {
            return (ScriptEntity) super.visitScript(ctx);
        }
    }

    @Override
    public ProcedureDefinition visitCustomBlock(ScratchBlocksParser.CustomBlockContext ctx) {
        final LocalIdentifier name = buildCustomBlockDefName(ctx);
        // todo: actual implementation
        final ParameterDefinitionList parameters = new ParameterDefinitionList(Collections.emptyList());
        final StmtList stmtList = makeInnerStmtList(ctx.stmtList());
        final ProcedureMetadata metadata = new ProcedureMetadata(new NoBlockMetadata(), new NoBlockMetadata());

        return new ProcedureDefinition(name, parameters, stmtList, metadata);
    }

    private LocalIdentifier buildCustomBlockDefName(final ScratchBlocksParser.CustomBlockContext ctx) {
        final StringBuilder sb = new StringBuilder();

        for (final var param : ctx.customBlockParameter()) {
            sb.append(param.getText());
        }

        if (ctx.suffix != null) {
            sb.append(ctx.suffix.getText());
        }

        return new StrId(unescape(sb.toString()));
    }

    @Override
    public StmtList visitStmtList(ScratchBlocksParser.StmtListContext ctx) {
        final List<Stmt> stmts = ctx.stmt().stream().map(this::visitStmt).toList();
        return new StmtList(stmts);
    }

    // region: events

    @Override
    public GreenFlag visitGreenFlag(ScratchBlocksParser.GreenFlagContext ctx) {
        return new GreenFlag(new NoBlockMetadata());
    }

    @Override
    public SpriteClicked visitSpriteClicked(ScratchBlocksParser.SpriteClickedContext ctx) {
        return new SpriteClicked(new NoBlockMetadata());
    }

    @Override
    public StageClicked visitStageClicked(ScratchBlocksParser.StageClickedContext ctx) {
        return new StageClicked(new NoBlockMetadata());
    }

    @Override
    public StartedAsClone visitStartAsClone(ScratchBlocksParser.StartAsCloneContext ctx) {
        return new StartedAsClone(new NoBlockMetadata());
    }

    @Override
    public KeyPressed visitKeyEvent(ScratchBlocksParser.KeyEventContext ctx) {
        return new KeyPressed(visitKey(ctx.key()), new NoBlockMetadata());
    }

    @Override
    public ReceptionOfMessage visitReceptionMessage(ScratchBlocksParser.ReceptionMessageContext ctx) {
        Message msg = new Message(visitStringArgument(ctx.stringArgument()));
        return new ReceptionOfMessage(msg, new NoBlockMetadata());
    }

    @Override
    public AttributeAboveValue visitBiggerEvent(ScratchBlocksParser.BiggerEventContext ctx) {
        EventAttribute attribute = visitEventChoice(ctx.eventChoice());
        NumExpr value = makeNumExpr(ctx.exprOrLiteral());
        return new AttributeAboveValue(attribute, value, new NoBlockMetadata());
    }

    @Override
    public BackdropSwitchTo visitBackDropSwitchEvent(ScratchBlocksParser.BackDropSwitchEventContext ctx) {
        LocalIdentifier backdrop = new StrId(visitStringArgument(ctx.stringArgument()));
        return new BackdropSwitchTo(backdrop, new NoBlockMetadata());
    }

    @Override
    public EventAttribute visitEventChoice(ScratchBlocksParser.EventChoiceContext ctx) {
        return new EventAttribute(ctx.getText());
    }

    // endregion: events

    // region: statements

    @Override
    public Stmt visitStmt(ScratchBlocksParser.StmtContext ctx) {
        if (ctx.motionStmt() != null) {
            return (Stmt) visitMotionStmt(ctx.motionStmt());
        } else if (ctx.looksStmt() != null) {
            return (Stmt) visitLooksStmt(ctx.looksStmt());
        } else if (ctx.soundStmt() != null) {
            return (Stmt) visitSoundStmt(ctx.soundStmt());
        } else if (ctx.controlStmt() != null) {
            return (Stmt) visitControlStmt(ctx.controlStmt());
        } else if (ctx.eventStmt() != null) {
            return (Stmt) visitEventStmt(ctx.eventStmt());
        } else if (ctx.sensingStmt() != null) {
            return (Stmt) visitSensingStmt(ctx.sensingStmt());
        } else if (ctx.customBlockCallStmt() != null) {
            return visitCustomBlockCallStmt(ctx.customBlockCallStmt());
        } else {
            // throw new UnsupportedOperationException instead since we are missing an implementation?
            return (Stmt) super.visitStmt(ctx);
        }
    }

    @Override
    public CallStmt visitCustomBlockCallStmt(ScratchBlocksParser.CustomBlockCallStmtContext ctx) {
        StringBuilder name = new StringBuilder(customBlockCallStmtName(ctx));
        final ExpressionList arguments = new ExpressionList(
                ctx.exprOrLiteral().stream().map(this::visitExprOrLiteral).toList()
        );

        for (final Expression expr : arguments.getExpressions()) {
            if (expr instanceof BoolExpr) {
                name.append(" %b");
            } else {
                name.append(" %s");
            }
        }

        return new CallStmt(new StrId(name.toString()), arguments, new NoBlockMetadata());
    }

    private String customBlockCallStmtName(final ScratchBlocksParser.CustomBlockCallStmtContext ctx) {
        final String name;

        if (ctx.stringArgument() != null) {
            name = visitStringArgument(ctx.stringArgument()).getText();
        } else {
            name = unescape(ctx.customBlockCallPrefix().getText());
        }

        return name.trim();
    }

    @Override
    public Stmt visitExpressionStmt(ScratchBlocksParser.ExpressionStmtContext ctx) {
        return new ExpressionStmt(visitExpression(ctx.expression()));
    }

    // begin subregion: motion blocks
    @Override
    public MoveSteps visitMoveSteps(ScratchBlocksParser.MoveStepsContext ctx) {
        return new MoveSteps(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public TurnRight visitTurnRight(ScratchBlocksParser.TurnRightContext ctx) {
        return new TurnRight(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public TurnLeft visitTurnLeft(ScratchBlocksParser.TurnLeftContext ctx) {
        return new TurnLeft(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public GoToPos visitGoToPos(ScratchBlocksParser.GoToPosContext ctx) {
        Position position = visitPosition(ctx.position());
        return new GoToPos(position, new NoBlockMetadata());
    }

    @Override
    public Position visitPosition(ScratchBlocksParser.PositionContext ctx) {
        if (ctx.fixedPosition() != null) {
            return visitFixedPosition(ctx.fixedPosition());
        }
        return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Position visitFixedPosition(ScratchBlocksParser.FixedPositionContext ctx) {
        if (ctx.getText().equals("random position")) {
            return new RandomPos(new NoBlockMetadata());
        } else if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        }
    }

    @Override
    public GoToPosXY visitGoToPosXY(ScratchBlocksParser.GoToPosXYContext ctx) {
        return new GoToPosXY(makeNumExpr(ctx.x), makeNumExpr(ctx.y), new NoBlockMetadata());
    }

    @Override
    public GlideSecsTo visitGlideToPos(ScratchBlocksParser.GlideToPosContext ctx) {
        Position position = visitPosition(ctx.position());
        return new GlideSecsTo(makeNumExpr(ctx.time), position, new NoBlockMetadata());
    }

    @Override
    public GlideSecsToXY visitGlideToPosXY(ScratchBlocksParser.GlideToPosXYContext ctx) {
        return new GlideSecsToXY(makeNumExpr(ctx.time), makeNumExpr(ctx.x), makeNumExpr(ctx.y), new NoBlockMetadata());
    }

    @Override
    public PointInDirection visitPointInDir(ScratchBlocksParser.PointInDirContext ctx) {
        return new PointInDirection(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public PointTowards visitPointTowards(ScratchBlocksParser.PointTowardsContext ctx) {
        return new PointTowards(visitPosition(ctx.position()), new NoBlockMetadata());
    }

    @Override
    public ChangeXBy visitChangeX(ScratchBlocksParser.ChangeXContext ctx) {
        return new ChangeXBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetXTo visitSetX(ScratchBlocksParser.SetXContext ctx) {
        return new SetXTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ChangeYBy visitChangeY(ScratchBlocksParser.ChangeYContext ctx) {
        return new ChangeYBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetYTo visitSetY(ScratchBlocksParser.SetYContext ctx) {
        return new SetYTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public IfOnEdgeBounce visitOnEdge(ScratchBlocksParser.OnEdgeContext ctx) {
        return new IfOnEdgeBounce(new NoBlockMetadata());
    }

    @Override
    public SetRotationStyle visitSetRotation(ScratchBlocksParser.SetRotationContext ctx) {
        return new SetRotationStyle(visitRotation(ctx.rotation()), new NoBlockMetadata());
    }

    @Override
    public RotationStyle visitRotation(ScratchBlocksParser.RotationContext ctx) {
        return new RotationStyle(ctx.getText());
    }

    //end subregion: motion blocks

    // begin subregion: looks blocks
    @Override
    public Say visitSay(ScratchBlocksParser.SayContext ctx) {
        return new Say(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SayForSecs visitSaySeconds(ScratchBlocksParser.SaySecondsContext ctx) {
        return new SayForSecs(makeStringExpr(ctx.text), makeNumExpr(ctx.time), new NoBlockMetadata());
    }

    @Override
    public Think visitThink(ScratchBlocksParser.ThinkContext ctx) {
        return new Think(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ThinkForSecs visitThinkSeconds(ScratchBlocksParser.ThinkSecondsContext ctx) {
        return new ThinkForSecs(makeStringExpr(ctx.text), makeNumExpr(ctx.time), new NoBlockMetadata());
    }

    @Override
    public SwitchCostumeTo visitSwitchCostume(ScratchBlocksParser.SwitchCostumeContext ctx) {
        return new SwitchCostumeTo(visitCostumeSelect(ctx.costumeSelect()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitCostumeSelect(ScratchBlocksParser.CostumeSelectContext ctx) {
        if (ctx.stringArgument() != null) {
            StrId costumeId = new StrId(visitStringArgument(ctx.stringArgument()));
            return new WithExpr(costumeId, new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public NextCostume visitNextCostume(ScratchBlocksParser.NextCostumeContext ctx) {
        return new NextCostume(new NoBlockMetadata());
    }

    @Override
    public SwitchBackdrop visitSwitchBackdrop(ScratchBlocksParser.SwitchBackdropContext ctx) {
        return new SwitchBackdrop(visitBackdropSelect(ctx.backdropSelect()), new NoBlockMetadata());
    }

    @Override
    public SwitchBackdropAndWait visitSwitchBackdropWait(ScratchBlocksParser.SwitchBackdropWaitContext ctx) {
        return new SwitchBackdropAndWait(visitBackdropSelect(ctx.backdropSelect()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitBackdropSelect(ScratchBlocksParser.BackdropSelectContext ctx) {
        if (ctx.fixedBackdrop() != null) {
            return visitFixedBackdrop(ctx.fixedBackdrop());
        } else if (ctx.stringArgument() != null) {
            StrId backdropId = new StrId(visitStringArgument(ctx.stringArgument()));
            return new WithExpr(backdropId, new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public ElementChoice visitFixedBackdrop(ScratchBlocksParser.FixedBackdropContext ctx) {
        return switch (ctx.getText()) {
            case "next backdrop" -> new Next(new NoBlockMetadata());
            case "previous backdrop" -> new Prev(new NoBlockMetadata());
            default -> new Random(new NoBlockMetadata());
        };
    }

    @Override
    public NextBackdrop visitNextBackdrop(ScratchBlocksParser.NextBackdropContext ctx) {
        return new NextBackdrop(new NoBlockMetadata());
    }

    @Override
    public ChangeSizeBy visitChangeSize(ScratchBlocksParser.ChangeSizeContext ctx) {
        return new ChangeSizeBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetSizeTo visitSetSize(ScratchBlocksParser.SetSizeContext ctx) {
        return new SetSizeTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ChangeGraphicEffectBy visitChangeColorEffect(ScratchBlocksParser.ChangeColorEffectContext ctx) {
        if (ctx.colorEffect() != null) {
            return new ChangeGraphicEffectBy(
                    visitColorEffect(ctx.colorEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
            );
        } else {
            return new ChangeGraphicEffectBy(
                    new GraphicEffect("color"), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
            );
        }
    }

    @Override
    public GraphicEffect visitColorEffect(ScratchBlocksParser.ColorEffectContext ctx) {
        return new GraphicEffect(ctx.getText());
    }

    @Override
    public SetGraphicEffectTo visitSetColorEffect(ScratchBlocksParser.SetColorEffectContext ctx) {
        if (ctx.colorEffect() != null) {
            return new SetGraphicEffectTo(
                    visitColorEffect(ctx.colorEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
            );
        } else {
            return new SetGraphicEffectTo(
                    new GraphicEffect("color"), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
            );
        }
    }

    @Override
    public ClearGraphicEffects visitClearColorEffect(ScratchBlocksParser.ClearColorEffectContext ctx) {
        return new ClearGraphicEffects(new NoBlockMetadata());
    }

    @Override
    public Show visitShow(ScratchBlocksParser.ShowContext ctx) {
        return new Show(new NoBlockMetadata());
    }

    @Override
    public Hide visitHide(ScratchBlocksParser.HideContext ctx) {
        return new Hide(new NoBlockMetadata());
    }

    @Override
    public GoToLayer visitGoToLayer(ScratchBlocksParser.GoToLayerContext ctx) {
        return new GoToLayer(visitLayerChoice(ctx.layerChoice()), new NoBlockMetadata());
    }

    @Override
    public LayerChoice visitLayerChoice(ScratchBlocksParser.LayerChoiceContext ctx) {
        return new LayerChoice(ctx.getText());
    }

    @Override
    public ChangeLayerBy visitGoForwardBackwardLayer(ScratchBlocksParser.GoForwardBackwardLayerContext ctx) {
        return new ChangeLayerBy(
                makeNumExpr(ctx.exprOrLiteral()),
                visitForwardBackwardChoice(ctx.forwardBackwardChoice()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ForwardBackwardChoice visitForwardBackwardChoice(
            ScratchBlocksParser.ForwardBackwardChoiceContext ctx
    ) {
        return new ForwardBackwardChoice(ctx.getText());
    }

    //end subregion: looks blocks

    //begin subregion: sound blocks

    @Override
    public PlaySoundUntilDone visitPlaySoundDone(ScratchBlocksParser.PlaySoundDoneContext ctx) {
        return new PlaySoundUntilDone(visitSoundChoice(ctx.soundChoice()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitSoundChoice(ScratchBlocksParser.SoundChoiceContext ctx) {
        if (ctx.stringArgument() != null) {
            StrId soundId = new StrId(visitStringArgument(ctx.stringArgument()));
            return new WithExpr(soundId, new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public StartSound visitPlaySound(ScratchBlocksParser.PlaySoundContext ctx) {
        return new StartSound(visitSoundChoice(ctx.soundChoice()), new NoBlockMetadata());
    }

    @Override
    public StopAllSounds visitStopSound(ScratchBlocksParser.StopSoundContext ctx) {
        return new StopAllSounds(new NoBlockMetadata());
    }

    @Override
    public ChangeSoundEffectBy visitChangeSoundEffect(ScratchBlocksParser.ChangeSoundEffectContext ctx) {
        return new ChangeSoundEffectBy(
                visitSoundEffect(ctx.soundEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public SetSoundEffectTo visitSetSoundEffect(ScratchBlocksParser.SetSoundEffectContext ctx) {
        return new SetSoundEffectTo(
                visitSoundEffect(ctx.soundEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public SoundEffect visitSoundEffect(ScratchBlocksParser.SoundEffectContext ctx) {
        return new SoundEffect(ctx.getText());
    }

    @Override
    public ClearSoundEffects visitClearSoundEffect(ScratchBlocksParser.ClearSoundEffectContext ctx) {
        return new ClearSoundEffects(new NoBlockMetadata());
    }

    @Override
    public ChangeVolumeBy visitChangeVolume(ScratchBlocksParser.ChangeVolumeContext ctx) {
        return new ChangeVolumeBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetVolumeTo visitSetVolume(ScratchBlocksParser.SetVolumeContext ctx) {
        return new SetVolumeTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    //end subregion: sound blocks

    //begin subregion: event statement blocks

    @Override
    public Broadcast visitBroadcast(ScratchBlocksParser.BroadcastContext ctx) {
        return new Broadcast(visitMessage(ctx.message()), new NoBlockMetadata());
    }

    @Override
    public ASTNode visitBroadcastWait(ScratchBlocksParser.BroadcastWaitContext ctx) {
        return new BroadcastAndWait(visitMessage(ctx.message()), new NoBlockMetadata());
    }

    @Override
    public Message visitMessage(ScratchBlocksParser.MessageContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new Message(makeStringExpr(ctx.exprOrLiteral()));
        } else {
            return new Message(visitStringArgument(ctx.stringArgument()));
        }
    }

    //end subregion: event statement blocks

    //begin subregion: control blocks

    @Override
    public WaitSeconds visitWaitSeconds(ScratchBlocksParser.WaitSecondsContext ctx) {
        return new WaitSeconds(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public RepeatTimesStmt visitRepeat(ScratchBlocksParser.RepeatContext ctx) {
        return new RepeatTimesStmt(
                makeNumExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public RepeatForeverStmt visitForever(ScratchBlocksParser.ForeverContext ctx) {
        return new RepeatForeverStmt(makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata());
    }

    @Override
    public IfThenStmt visitIf(ScratchBlocksParser.IfContext ctx) {
        return new IfThenStmt(
                makeBoolExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public IfElseStmt visitIfElse(ScratchBlocksParser.IfElseContext ctx) {
        return new IfElseStmt(
                makeBoolExpr(ctx.exprOrLiteral()),
                makeInnerStmtList(ctx.then),
                makeInnerStmtList(ctx.else_),
                new NoBlockMetadata()
        );
    }

    @Override
    public WaitUntil visitWaitUntil(ScratchBlocksParser.WaitUntilContext ctx) {
        return new WaitUntil(makeBoolExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public UntilStmt visitRepeatUntil(ScratchBlocksParser.RepeatUntilContext ctx) {
        return new UntilStmt(
                makeBoolExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public Stmt visitStop(ScratchBlocksParser.StopContext ctx) {
        if (ctx.stopChoice().getText().equals("all")) {
            return new StopAll(new NoBlockMetadata());
        } else if (ctx.stopChoice().getText().equals("this script")) {
            return new StopThisScript(new NoBlockMetadata());
        } else {
            return new StopOtherScriptsInSprite(new NoBlockMetadata());
        }
    }

    @Override
    public CreateCloneOf visitCreateClone(ScratchBlocksParser.CreateCloneContext ctx) {
        return new CreateCloneOf(visitCloneChoice(ctx.cloneChoice()), new NoBlockMetadata());
    }

    @Override
    public StringExpr visitCloneChoice(ScratchBlocksParser.CloneChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return makeStringExpr(ctx.exprOrLiteral());
        } else if (ctx.stringArgument() != null) {
            return new AsString(new StrId(visitStringArgument(ctx.stringArgument())));
        } else {
            return new AsString(new StrId(new StringLiteral("myself")));
        }
    }

    @Override
    public DeleteClone visitDeleteClone(ScratchBlocksParser.DeleteCloneContext ctx) {
        return new DeleteClone(new NoBlockMetadata());
    }

    //end subregion: control blocks

    //begin subregion: sensing blocks

    @Override
    public AskAndWait visitAsk(ScratchBlocksParser.AskContext ctx) {
        return new AskAndWait(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetDragMode visitSetDragMode(ScratchBlocksParser.SetDragModeContext ctx) {
        return new SetDragMode(visitDragmode(ctx.dragmode()), new NoBlockMetadata());
    }

    @Override
    public DragMode visitDragmode(ScratchBlocksParser.DragmodeContext ctx) {
        return new DragMode(ctx.getText());
    }

    @Override
    public ResetTimer visitResetTimer(ScratchBlocksParser.ResetTimerContext ctx) {
        return new ResetTimer(new NoBlockMetadata());
    }

    //end subregion: sensing blocks

    //begin subregion: variable blocks

    @Override
    public SetVariableTo visitSetVar(ScratchBlocksParser.SetVarContext ctx) {
        return new SetVariableTo(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ChangeVariableBy visitChangeVar(ScratchBlocksParser.ChangeVarContext ctx) {
        return new ChangeVariableBy(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ShowVariable visitShowVar(ScratchBlocksParser.ShowVarContext ctx) {
        return new ShowVariable(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public HideVariable visitHideVar(ScratchBlocksParser.HideVarContext ctx) {
        return new HideVariable(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public AddTo visitAddToList(ScratchBlocksParser.AddToListContext ctx) {
        return new AddTo(
                makeStringExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public DeleteOf visitDeleteFromList(ScratchBlocksParser.DeleteFromListContext ctx) {
        return new DeleteOf(
                makeNumExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public DeleteAllOf visitDeleteAllOfList(ScratchBlocksParser.DeleteAllOfListContext ctx) {
        return new DeleteAllOf(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public InsertAt visitInsertToList(ScratchBlocksParser.InsertToListContext ctx) {
        return new InsertAt(
                makeStringExpr(ctx.insertion),
                makeNumExpr(ctx.location),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public ReplaceItem visitReplaceItemInList(ScratchBlocksParser.ReplaceItemInListContext ctx) {
        return new ReplaceItem(
                makeStringExpr(ctx.newItem),
                makeNumExpr(ctx.oldItem),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public ShowList visitShowList(ScratchBlocksParser.ShowListContext ctx) {
        return new ShowList(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public HideList visitHideList(ScratchBlocksParser.HideListContext ctx) {
        return new HideList(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }
    //end subregion: variable blocks

    // endregion: statements

    // region: expressions

    // subregion: bool expressions

    @Override
    public Expression visitExprOrLiteral(ScratchBlocksParser.ExprOrLiteralContext ctx) {
        if (ctx.numLiteral() != null) {
            return visitNumLiteral(ctx.numLiteral());
        } else if (ctx.stringLiteral() != null) {
            return visitStringLiteral(ctx.stringLiteral());
        } else {
            return visitExpression(ctx.expression());
        }
    }

    @Override
    public Touching visitTouching(ScratchBlocksParser.TouchingContext ctx) {
        Touchable touchable = visitTouchingChoice(ctx.touchingChoice());
        return new Touching(touchable, new NoBlockMetadata());
    }

    @Override
    public Touchable visitTouchingChoice(ScratchBlocksParser.TouchingChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new AsTouchable(visitExprOrLiteral(ctx.exprOrLiteral()));
        } else if (ctx.stringArgument() != null) {
            return new SpriteTouchable(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else if (ctx.fixedTouching() != null) {
            return visitFixedTouching(ctx.fixedTouching());
        }
        return (Touchable) super.visitTouchingChoice(ctx);
    }

    @Override
    public Touchable visitFixedTouching(ScratchBlocksParser.FixedTouchingContext ctx) {
        if (ctx.getText().equals("mouse-pointer")) {
            return new MousePointer(new NoBlockMetadata());
        } else {
            return new Edge(new NoBlockMetadata());
        }
    }

    @Override
    public SpriteTouchingColor visitTouchingColor(ScratchBlocksParser.TouchingColorContext ctx) {
        Color color = visitTouchingColorChoice(ctx.touchingColorChoice());
        return new SpriteTouchingColor(color, new NoBlockMetadata());
    }

    @Override
    public ColorTouchingColor visitColorTouchingColor(ScratchBlocksParser.ColorTouchingColorContext ctx) {
        return new ColorTouchingColor(
                visitTouchingColorChoice(ctx.firstColor),
                visitTouchingColorChoice(ctx.secondColor),
                new NoBlockMetadata()
        );
    }

    @Override
    public Color visitTouchingColorChoice(ScratchBlocksParser.TouchingColorChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            // any expression can be put into the colour fields
            return new FromNumber(new AsNumber(visitExprOrLiteral(ctx.exprOrLiteral())));
        } else {
            String rgbCode = ctx.HEX().getText();
            return ColorLiteral.tryFromRgbHexString(rgbCode);
        }
    }

    @Override
    public IsKeyPressed visitKeyPressed(ScratchBlocksParser.KeyPressedContext ctx) {
        return new IsKeyPressed(visitKeySelect(ctx.keySelect()), new NoBlockMetadata());
    }

    @Override
    public Key visitKeySelect(ScratchBlocksParser.KeySelectContext ctx) {
        if (ctx.key() != null) {
            return visitKey(ctx.key());
        } else {
            return new Key(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Key visitKey(ScratchBlocksParser.KeyContext ctx) {
        return switch (ctx.getText()) {
            case "space" -> new Key(new NumberLiteral(KeyCode.SPACE.getKeycode()), new NoBlockMetadata());
            case "up arrow" -> new Key(new NumberLiteral(KeyCode.UP_ARROW.getKeycode()), new NoBlockMetadata());
            case "down arrow" -> new Key(new NumberLiteral(KeyCode.DOWN_ARROW.getKeycode()), new NoBlockMetadata());
            case "left arrow" -> new Key(new NumberLiteral(KeyCode.LEFT_ARROW.getKeycode()), new NoBlockMetadata());
            case "right arrow" -> new Key(new NumberLiteral(KeyCode.RIGHT_ARROW.getKeycode()), new NoBlockMetadata());
            case "any" -> new Key(new NumberLiteral(KeyCode.ANY_KEY.getKeycode()), new NoBlockMetadata());
            default -> new Key(new NumberLiteral(ctx.getText().charAt(0)), new NoBlockMetadata());
        };
    }

    @Override
    public IsMouseDown visitMouseDown(ScratchBlocksParser.MouseDownContext ctx) {
        return new IsMouseDown(new NoBlockMetadata());
    }

    @Override
    public BiggerThan visitGreaterThan(ScratchBlocksParser.GreaterThanContext ctx) {
        return new BiggerThan(
                ensureComparable(visitExprOrLiteral(ctx.firstExpr)),
                ensureComparable(visitExprOrLiteral(ctx.secondExpr)),
                new NoBlockMetadata()
        );
    }

    @Override
    public LessThan visitLessThan(ScratchBlocksParser.LessThanContext ctx) {
        return new LessThan(
                ensureComparable(visitExprOrLiteral(ctx.firstExpr)),
                ensureComparable(visitExprOrLiteral(ctx.secondExpr)),
                new NoBlockMetadata()
        );
    }

    @Override
    public Equals visitEqual(ScratchBlocksParser.EqualContext ctx) {
        return new Equals(
                ensureComparable(visitExprOrLiteral(ctx.firstExpr)),
                ensureComparable(visitExprOrLiteral(ctx.secondExpr)),
                new NoBlockMetadata()
        );
    }

    @Override
    public Not visitNot(ScratchBlocksParser.NotContext ctx) {
        return new Not(makeBoolExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public And visitAnd(ScratchBlocksParser.AndContext ctx) {
        return new And(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Or visitOr(ScratchBlocksParser.OrContext ctx) {
        return new Or(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public StringContains visitContains(ScratchBlocksParser.ContainsContext ctx) {
        return new StringContains(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public ListContains visitListContains(ScratchBlocksParser.ListContainsContext ctx) {
        return new ListContains(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    //end subregion: bool expressions

    //subregion num expression

    @Override
    public PositionX visitXPosition(ScratchBlocksParser.XPositionContext ctx) {
        return new PositionX(new NoBlockMetadata());
    }

    @Override
    public PositionY visitYPosition(ScratchBlocksParser.YPositionContext ctx) {
        return new PositionY(new NoBlockMetadata());
    }

    @Override
    public Direction visitDirection(ScratchBlocksParser.DirectionContext ctx) {
        return new Direction(new NoBlockMetadata());
    }

    @Override
    public Costume visitNumCostume(ScratchBlocksParser.NumCostumeContext ctx) {
        return new Costume(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public NameNum visitNameNum(ScratchBlocksParser.NameNumContext ctx) {
        return new NameNum(ctx.getText());
    }

    @Override
    public Backdrop visitNumBackdrop(ScratchBlocksParser.NumBackdropContext ctx) {
        return new Backdrop(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public Size visitSize(ScratchBlocksParser.SizeContext ctx) {
        return new Size(new NoBlockMetadata());
    }

    @Override
    public Volume visitVolume(ScratchBlocksParser.VolumeContext ctx) {
        return new Volume(new NoBlockMetadata());
    }

    @Override
    public DistanceTo visitDistanceTo(ScratchBlocksParser.DistanceToContext ctx) {
        return new DistanceTo(visitDistanceChoice(ctx.distanceChoice()), new NoBlockMetadata());
    }

    @Override
    public Position visitDistanceChoice(ScratchBlocksParser.DistanceChoiceContext ctx) {
        if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else if (ctx.stringArgument() != null) {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Answer visitAnswer(ScratchBlocksParser.AnswerContext ctx) {
        return new Answer(new NoBlockMetadata());
    }

    @Override
    public MouseX visitMouseX(ScratchBlocksParser.MouseXContext ctx) {
        return new MouseX(new NoBlockMetadata());
    }

    @Override
    public MouseY visitMouseY(ScratchBlocksParser.MouseYContext ctx) {
        return new MouseY(new NoBlockMetadata());
    }

    @Override
    public Loudness visitLoudness(ScratchBlocksParser.LoudnessContext ctx) {
        return new Loudness(new NoBlockMetadata());
    }

    @Override
    public Timer visitTimer(ScratchBlocksParser.TimerContext ctx) {
        return new Timer(new NoBlockMetadata());
    }

    @Override
    public AttributeOf visitActorAttribute(ScratchBlocksParser.ActorAttributeContext ctx) {
        return new AttributeOf(
                visitAttributeChoice(ctx.attributeChoice()),
                visitElement(ctx.element()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ElementChoice visitElement(ScratchBlocksParser.ElementContext ctx) {
        if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Attribute visitAttributeChoice(ScratchBlocksParser.AttributeChoiceContext ctx) {
        if (ctx.fixedAttribute() != null) {
            return new AttributeFromFixed(visitFixedAttribute(ctx.fixedAttribute()));
        } else {
            return new AttributeFromVariable(new Variable(new StrId(visitStringArgument(ctx.stringArgument()))));
        }
    }

    @Override
    public FixedAttribute visitFixedAttribute(ScratchBlocksParser.FixedAttributeContext ctx) {
        return new FixedAttribute(ctx.getText());
    }

    @Override
    public Current visitCurrentTime(ScratchBlocksParser.CurrentTimeContext ctx) {
        return new Current(visitCurrentChoice(ctx.currentChoice()), new NoBlockMetadata());
    }

    @Override
    public TimeComp visitCurrentChoice(ScratchBlocksParser.CurrentChoiceContext ctx) {
        if (ctx.getText().equals("day of the week")) {
            return new TimeComp("dayofweek");
        }
        return new TimeComp(ctx.getText());
    }

    @Override
    public DaysSince2000 visitDaysSince(ScratchBlocksParser.DaysSinceContext ctx) {
        return new DaysSince2000(new NoBlockMetadata());
    }

    @Override
    public Username visitUserName(ScratchBlocksParser.UserNameContext ctx) {
        return new Username(new NoBlockMetadata());
    }

    @Override
    public Add visitAddition(ScratchBlocksParser.AdditionContext ctx) {
        return new Add(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Minus visitSubtraction(ScratchBlocksParser.SubtractionContext ctx) {
        return new Minus(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Mult visitMultiplication(ScratchBlocksParser.MultiplicationContext ctx) {
        return new Mult(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Div visitDivision(ScratchBlocksParser.DivisionContext ctx) {
        return new Div(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public PickRandom visitPickRandom(ScratchBlocksParser.PickRandomContext ctx) {
        return new PickRandom(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Join visitJoin(ScratchBlocksParser.JoinContext ctx) {
        return new Join(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LetterOf visitGetLetterAtIndex(ScratchBlocksParser.GetLetterAtIndexContext ctx) {
        return new LetterOf(makeNumExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LengthOfString visitLengthOf(ScratchBlocksParser.LengthOfContext ctx) {
        return new LengthOfString(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Mod visitModulo(ScratchBlocksParser.ModuloContext ctx) {
        return new Mod(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Round visitRound(ScratchBlocksParser.RoundContext ctx) {
        return new Round(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public NumFunctOf visitMathFunction(ScratchBlocksParser.MathFunctionContext ctx) {
        return new NumFunctOf(
                visitMathChoice(ctx.mathChoice()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public NumFunct visitMathChoice(ScratchBlocksParser.MathChoiceContext ctx) {
        return new NumFunct(ctx.getText());
    }

    @Override
    public ItemOfVariable visitItemAtIndex(ScratchBlocksParser.ItemAtIndexContext ctx) {
        return new ItemOfVariable(
                makeNumExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public IndexOf visitIndexOfItem(ScratchBlocksParser.IndexOfItemContext ctx) {
        return new IndexOf(
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public LengthOfVar visitLengthOfList(ScratchBlocksParser.LengthOfListContext ctx) {
        return new LengthOfVar(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    //end subregion: num expressions

    @Override
    public NumberLiteral visitNumLiteral(ScratchBlocksParser.NumLiteralContext ctx) {
        final String value;
        if (ctx.DIGIT() != null) {
            value = ctx.DIGIT().getText();
        } else {
            value = ctx.NUMBER().getText();
        }

        return new NumberLiteral(Double.parseDouble(value));
    }

    @Override
    public StringLiteral visitStringLiteral(ScratchBlocksParser.StringLiteralContext ctx) {
        return visitStringArgument(ctx.stringArgument());
    }

    @Override
    public Expression visitExpression(ScratchBlocksParser.ExpressionContext ctx) {
        if (ctx.stringArgument() != null) {
            final Variable variable = new Variable(new StrId(visitStringArgument(ctx.stringArgument())));
            return new Qualified(currentActor, variable);
        } else if (ctx.boolExpr() != null) {
            return (Expression) visitBoolExpr(ctx.boolExpr());
        } else if (ctx.numExpr() != null) {
            return (Expression) visitNumExpr(ctx.numExpr());
        } else {
            return (Expression) super.visitExpression(ctx);
        }
    }

    @Override
    public StringLiteral visitStringArgument(ScratchBlocksParser.StringArgumentContext ctx) {
        return new StringLiteral(unescape(ctx.getText()));
    }

    private String unescape(final String s) {
        return s
                .replaceAll("\\\\(?=[\\w" + SPECIAL_WITHOUT_BSLASH + "])", "") // Remove superfluous \
                .replace("\\\\", "\\"); // Handle double backslash
    }

    private NumExpr makeNumExpr(ScratchBlocksParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        NumExpr numExpr;

        if (expr instanceof NumExpr num) {
            numExpr = num;
        } else {
            numExpr = new AsNumber(expr);
        }
        return numExpr;
    }

    private StringExpr makeStringExpr(ScratchBlocksParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        StringExpr stringExpr;

        if (expr instanceof StringExpr str) {
            stringExpr = str;
        } else {
            stringExpr = new AsString(expr);
        }
        return stringExpr;
    }

    /**
     * Boolean expressions can be dragged into the round fields of {@code <},
     * {@code >}, and {@code =}. They have to be wrapped to be comparable in
     * such instances.
     *
     * @param expression Some expression appearing inside a comparison.
     * @return The same expression, or wrapped to make it comparable.
     */
    private ComparableExpr ensureComparable(final Expression expression) {
        if (expression instanceof ComparableExpr c) {
            return c;
        } else {
            return new AsString(expression);
        }
    }

    private BoolExpr makeBoolExpr(ScratchBlocksParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        BoolExpr boolExpr;

        if (expr instanceof BoolExpr bool) {
            boolExpr = bool;
        } else {
            boolExpr = new AsBool(expr);
        }
        return boolExpr;
    }

    // endregion: expressions

    private StmtList makeInnerStmtList(ScratchBlocksParser.StmtListContext ctx) {
        StmtList stmt;

        if (ctx != null) {
            stmt = visitStmtList(ctx);
        } else {
            stmt = new StmtList();
        }
        return stmt;
    }
}
