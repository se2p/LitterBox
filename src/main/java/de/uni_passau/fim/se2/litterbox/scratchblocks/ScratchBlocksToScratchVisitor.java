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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksGrammarBaseVisitor;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksGrammarParser;

import java.util.List;

class ScratchBlocksToScratchVisitor extends ScratchBlocksGrammarBaseVisitor<ASTNode> {

    private static final String SPECIAL_WITHOUT_BSLASH = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~]";

    @Override
    public ScriptEntity visitScript(ScratchBlocksGrammarParser.ScriptContext ctx) {
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
            return (ScriptEntity) visit(ctx.customBlock());
        } else {
            return (ScriptEntity) super.visitScript(ctx);
        }
    }

    @Override
    public StmtList visitStmtList(ScratchBlocksGrammarParser.StmtListContext ctx) {
        final List<Stmt> stmts = ctx.stmt().stream().map(this::visitStmt).toList();
        return new StmtList(stmts);
    }

    // region: statements

    @Override
    public Stmt visitStmt(ScratchBlocksGrammarParser.StmtContext ctx) {
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
            // todo: other cases
        } else {
            return (Stmt) super.visitStmt(ctx);
        }
    }

    @Override
    public Stmt visitExpressionStmt(ScratchBlocksGrammarParser.ExpressionStmtContext ctx) {
        return new ExpressionStmt(visitExpression(ctx.expression()));
    }

    // begin subregion: motion blocks
    @Override
    public MoveSteps visitMoveSteps(ScratchBlocksGrammarParser.MoveStepsContext ctx) {
        return new MoveSteps(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public TurnRight visitTurnRight(ScratchBlocksGrammarParser.TurnRightContext ctx) {
        return new TurnRight(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public TurnLeft visitTurnLeft(ScratchBlocksGrammarParser.TurnLeftContext ctx) {
        return new TurnLeft(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public GoToPos visitGoToPos(ScratchBlocksGrammarParser.GoToPosContext ctx) {
        Position position = visitPosition(ctx.position());
        return new GoToPos(position, new NoBlockMetadata());
    }

    @Override
    public Position visitPosition(ScratchBlocksGrammarParser.PositionContext ctx) {
        if (ctx.fixedPosition() != null) {
            return visitFixedPosition(ctx.fixedPosition());
        }
        return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Position visitFixedPosition(ScratchBlocksGrammarParser.FixedPositionContext ctx) {
        if (ctx.getText().equals("random position")) {
            return new RandomPos(new NoBlockMetadata());
        } else if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        }
    }

    @Override
    public GoToPosXY visitGoToPosXY(ScratchBlocksGrammarParser.GoToPosXYContext ctx) {
        return new GoToPosXY(makeNumExpr(ctx.x), makeNumExpr(ctx.y), new NoBlockMetadata());
    }

    @Override
    public GlideSecsTo visitGlideToPos(ScratchBlocksGrammarParser.GlideToPosContext ctx) {
        Position position = visitPosition(ctx.position());
        return new GlideSecsTo(makeNumExpr(ctx.time), position, new NoBlockMetadata());
    }

    @Override
    public GlideSecsToXY visitGlideToPosXY(ScratchBlocksGrammarParser.GlideToPosXYContext ctx) {
        return new GlideSecsToXY(makeNumExpr(ctx.time), makeNumExpr(ctx.x), makeNumExpr(ctx.y), new NoBlockMetadata());
    }

    @Override
    public PointInDirection visitPointInDir(ScratchBlocksGrammarParser.PointInDirContext ctx) {
        return new PointInDirection(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public PointTowards visitPointTowards(ScratchBlocksGrammarParser.PointTowardsContext ctx) {
        return new PointTowards(visitPosition(ctx.position()), new NoBlockMetadata());
    }

    @Override
    public ChangeXBy visitChangeX(ScratchBlocksGrammarParser.ChangeXContext ctx) {
        return new ChangeXBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetXTo visitSetX(ScratchBlocksGrammarParser.SetXContext ctx) {
        return new SetXTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ChangeYBy visitChangeY(ScratchBlocksGrammarParser.ChangeYContext ctx) {
        return new ChangeYBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetYTo visitSetY(ScratchBlocksGrammarParser.SetYContext ctx) {
        return new SetYTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public IfOnEdgeBounce visitOnEdge(ScratchBlocksGrammarParser.OnEdgeContext ctx) {
        return new IfOnEdgeBounce(new NoBlockMetadata());
    }

    @Override
    public SetRotationStyle visitSetRotation(ScratchBlocksGrammarParser.SetRotationContext ctx) {
        return new SetRotationStyle(visitRotation(ctx.rotation()), new NoBlockMetadata());
    }

    @Override
    public RotationStyle visitRotation(ScratchBlocksGrammarParser.RotationContext ctx) {
        return new RotationStyle(ctx.getText());
    }

    //end subregion: motion blocks

    // begin subregion: looks blocks
    @Override
    public Say visitSay(ScratchBlocksGrammarParser.SayContext ctx) {
        return new Say(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SayForSecs visitSaySeconds(ScratchBlocksGrammarParser.SaySecondsContext ctx) {
        return new SayForSecs(makeStringExpr(ctx.text), makeNumExpr(ctx.time), new NoBlockMetadata());
    }

    @Override
    public Think visitThink(ScratchBlocksGrammarParser.ThinkContext ctx) {
        return new Think(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ThinkForSecs visitThinkSeconds(ScratchBlocksGrammarParser.ThinkSecondsContext ctx) {
        return new ThinkForSecs(makeStringExpr(ctx.text), makeNumExpr(ctx.time), new NoBlockMetadata());
    }

    @Override
    public SwitchCostumeTo visitSwitchCostume(ScratchBlocksGrammarParser.SwitchCostumeContext ctx) {
        return new SwitchCostumeTo(visitCostumeSelect(ctx.costumeSelect()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitCostumeSelect(ScratchBlocksGrammarParser.CostumeSelectContext ctx) {
        if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public NextCostume visitNextCostume(ScratchBlocksGrammarParser.NextCostumeContext ctx) {
        return new NextCostume(new NoBlockMetadata());
    }

    @Override
    public SwitchBackdrop visitSwitchBackdrop(ScratchBlocksGrammarParser.SwitchBackdropContext ctx) {
        return new SwitchBackdrop(visitBackdropSelect(ctx.backdropSelect()), new NoBlockMetadata());
    }

    @Override
    public SwitchBackdropAndWait visitSwitchBackdropWait(ScratchBlocksGrammarParser.SwitchBackdropWaitContext ctx) {
        return new SwitchBackdropAndWait(visitBackdropSelect(ctx.backdropSelect()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitBackdropSelect(ScratchBlocksGrammarParser.BackdropSelectContext ctx) {
        if (ctx.fixedBackdrop() != null) {
            return visitFixedBackdrop(ctx.fixedBackdrop());
        } else if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public ElementChoice visitFixedBackdrop(ScratchBlocksGrammarParser.FixedBackdropContext ctx) {
        return switch (ctx.getText()) {
            case "next backdrop" -> new Next(new NoBlockMetadata());
            case "previous backdrop" -> new Prev(new NoBlockMetadata());
            default -> new Random(new NoBlockMetadata());
        };
    }

    @Override
    public NextBackdrop visitNextBackdrop(ScratchBlocksGrammarParser.NextBackdropContext ctx) {
        return new NextBackdrop(new NoBlockMetadata());
    }

    @Override
    public ChangeSizeBy visitChangeSize(ScratchBlocksGrammarParser.ChangeSizeContext ctx) {
        return new ChangeSizeBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetSizeTo visitSetSize(ScratchBlocksGrammarParser.SetSizeContext ctx) {
        return new SetSizeTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ChangeGraphicEffectBy visitChangeColorEffect(ScratchBlocksGrammarParser.ChangeColorEffectContext ctx) {
        return new ChangeGraphicEffectBy(
                visitColorEffect(ctx.colorEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public GraphicEffect visitColorEffect(ScratchBlocksGrammarParser.ColorEffectContext ctx) {
        return new GraphicEffect(ctx.getText());
    }

    @Override
    public SetGraphicEffectTo visitSetColorEffect(ScratchBlocksGrammarParser.SetColorEffectContext ctx) {
        return new SetGraphicEffectTo(
                visitColorEffect(ctx.colorEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public ClearGraphicEffects visitClearColorEffect(ScratchBlocksGrammarParser.ClearColorEffectContext ctx) {
        return new ClearGraphicEffects(new NoBlockMetadata());
    }

    @Override
    public Show visitShow(ScratchBlocksGrammarParser.ShowContext ctx) {
        return new Show(new NoBlockMetadata());
    }

    @Override
    public Hide visitHide(ScratchBlocksGrammarParser.HideContext ctx) {
        return new Hide(new NoBlockMetadata());
    }

    @Override
    public GoToLayer visitGoToLayer(ScratchBlocksGrammarParser.GoToLayerContext ctx) {
        return new GoToLayer(visitLayerChoice(ctx.layerChoice()), new NoBlockMetadata());
    }

    @Override
    public LayerChoice visitLayerChoice(ScratchBlocksGrammarParser.LayerChoiceContext ctx) {
        return new LayerChoice(ctx.getText());
    }

    @Override
    public ChangeLayerBy visitGoForwardBackwardLayer(ScratchBlocksGrammarParser.GoForwardBackwardLayerContext ctx) {
        return new ChangeLayerBy(
                makeNumExpr(ctx.exprOrLiteral()),
                visitForwardBackwardChoice(ctx.forwardBackwardChoice()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ForwardBackwardChoice visitForwardBackwardChoice(
            ScratchBlocksGrammarParser.ForwardBackwardChoiceContext ctx
    ) {
        return new ForwardBackwardChoice(ctx.getText());
    }

    //end subregion: looks blocks

    //begin subregion: sound blocks

    @Override
    public PlaySoundUntilDone visitPlaySoundDone(ScratchBlocksGrammarParser.PlaySoundDoneContext ctx) {
        return new PlaySoundUntilDone(visitSoundChoice(ctx.soundChoice()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitSoundChoice(ScratchBlocksGrammarParser.SoundChoiceContext ctx) {
        if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public StartSound visitPlaySound(ScratchBlocksGrammarParser.PlaySoundContext ctx) {
        return new StartSound(visitSoundChoice(ctx.soundChoice()), new NoBlockMetadata());
    }

    @Override
    public StopAllSounds visitStopSound(ScratchBlocksGrammarParser.StopSoundContext ctx) {
        return new StopAllSounds(new NoBlockMetadata());
    }

    @Override
    public ChangeSoundEffectBy visitChangeSoundEffect(ScratchBlocksGrammarParser.ChangeSoundEffectContext ctx) {
        return new ChangeSoundEffectBy(
                visitSoundEffect(ctx.soundEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public SetSoundEffectTo visitSetSoundEffect(ScratchBlocksGrammarParser.SetSoundEffectContext ctx) {
        return new SetSoundEffectTo(
                visitSoundEffect(ctx.soundEffect()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public SoundEffect visitSoundEffect(ScratchBlocksGrammarParser.SoundEffectContext ctx) {
        return new SoundEffect(ctx.getText());
    }

    @Override
    public ClearSoundEffects visitClearSoundEffect(ScratchBlocksGrammarParser.ClearSoundEffectContext ctx) {
        return new ClearSoundEffects(new NoBlockMetadata());
    }

    @Override
    public ChangeVolumeBy visitChangeVolume(ScratchBlocksGrammarParser.ChangeVolumeContext ctx) {
        return new ChangeVolumeBy(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetVolumeTo visitSetVolume(ScratchBlocksGrammarParser.SetVolumeContext ctx) {
        return new SetVolumeTo(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    //end subregion: sound blocks

    //begin subregion: event statement blocks

    @Override
    public Broadcast visitBroadcast(ScratchBlocksGrammarParser.BroadcastContext ctx) {
        return new Broadcast(visitMessage(ctx.message()), new NoBlockMetadata());
    }

    @Override
    public ASTNode visitBroadcastWait(ScratchBlocksGrammarParser.BroadcastWaitContext ctx) {
        return new BroadcastAndWait(visitMessage(ctx.message()), new NoBlockMetadata());
    }

    @Override
    public Message visitMessage(ScratchBlocksGrammarParser.MessageContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new Message(makeStringExpr(ctx.exprOrLiteral()));
        } else {
            return new Message(visitStringArgument(ctx.stringArgument()));
        }
    }

    //end subregion: event statement blocks

    //begin subregion: control blocks

    @Override
    public WaitSeconds visitWaitSeconds(ScratchBlocksGrammarParser.WaitSecondsContext ctx) {
        return new WaitSeconds(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public RepeatTimesStmt visitRepeat(ScratchBlocksGrammarParser.RepeatContext ctx) {
        return new RepeatTimesStmt(
                makeNumExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public RepeatForeverStmt visitForever(ScratchBlocksGrammarParser.ForeverContext ctx) {
        return new RepeatForeverStmt(makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata());
    }

    @Override
    public IfThenStmt visitIf(ScratchBlocksGrammarParser.IfContext ctx) {
        return new IfThenStmt(
                makeBoolExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public IfElseStmt visitIfElse(ScratchBlocksGrammarParser.IfElseContext ctx) {
        return new IfElseStmt(
                makeBoolExpr(ctx.exprOrLiteral()),
                makeInnerStmtList(ctx.then),
                makeInnerStmtList(ctx.else_),
                new NoBlockMetadata()
        );
    }

    @Override
    public WaitUntil visitWaitUntil(ScratchBlocksGrammarParser.WaitUntilContext ctx) {
        return new WaitUntil(makeBoolExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public UntilStmt visitRepeatUntil(ScratchBlocksGrammarParser.RepeatUntilContext ctx) {
        return new UntilStmt(
                makeBoolExpr(ctx.exprOrLiteral()), makeInnerStmtList(ctx.stmtList()), new NoBlockMetadata()
        );
    }

    @Override
    public Stmt visitStop(ScratchBlocksGrammarParser.StopContext ctx) {
        if (ctx.stopChoice().getText().equals("all")) {
            return new StopAll(new NoBlockMetadata());
        } else if (ctx.stopChoice().getText().equals("this script")) {
            return new StopThisScript(new NoBlockMetadata());
        } else {
            return new StopOtherScriptsInSprite(new NoBlockMetadata());
        }
    }

    @Override
    public CreateCloneOf visitCreateClone(ScratchBlocksGrammarParser.CreateCloneContext ctx) {
        return new CreateCloneOf(visitCloneChoice(ctx.cloneChoice()), new NoBlockMetadata());
    }

    @Override
    public StringExpr visitCloneChoice(ScratchBlocksGrammarParser.CloneChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return makeStringExpr(ctx.exprOrLiteral());
        } else if (ctx.stringArgument() != null) {
            return new AsString(new StrId(visitStringArgument(ctx.stringArgument())));
        } else {
            return new AsString(new StrId(new StringLiteral("myself")));
        }
    }

    @Override
    public DeleteClone visitDeleteClone(ScratchBlocksGrammarParser.DeleteCloneContext ctx) {
        return new DeleteClone(new NoBlockMetadata());
    }

    //end subregion: control blocks

    //begin subregion: sensing blocks

    @Override
    public AskAndWait visitAsk(ScratchBlocksGrammarParser.AskContext ctx) {
        return new AskAndWait(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SetDragMode visitSetDragMode(ScratchBlocksGrammarParser.SetDragModeContext ctx) {
        return new SetDragMode(visitDragmode(ctx.dragmode()), new NoBlockMetadata());
    }

    @Override
    public DragMode visitDragmode(ScratchBlocksGrammarParser.DragmodeContext ctx) {
        return new DragMode(ctx.getText());
    }

    @Override
    public ResetTimer visitResetTimer(ScratchBlocksGrammarParser.ResetTimerContext ctx) {
        return new ResetTimer(new NoBlockMetadata());
    }

    //end subregion: sensing blocks

    //begin subregion: variable blocks

    @Override
    public SetVariableTo visitSetVar(ScratchBlocksGrammarParser.SetVarContext ctx) {
        return new SetVariableTo(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ChangeVariableBy visitChangeVar(ScratchBlocksGrammarParser.ChangeVarContext ctx) {
        return new ChangeVariableBy(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ShowVariable visitShowVar(ScratchBlocksGrammarParser.ShowVarContext ctx) {
        return new ShowVariable(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public HideVariable visitHideVar(ScratchBlocksGrammarParser.HideVarContext ctx) {
        return new HideVariable(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public AddTo visitAddToList(ScratchBlocksGrammarParser.AddToListContext ctx) {
        return new AddTo(
                makeStringExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public DeleteOf visitDeleteFromList(ScratchBlocksGrammarParser.DeleteFromListContext ctx) {
        return new DeleteOf(
                makeNumExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public DeleteAllOf visitDeleteAllOfList(ScratchBlocksGrammarParser.DeleteAllOfListContext ctx) {
        return new DeleteAllOf(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public InsertAt visitInsertToList(ScratchBlocksGrammarParser.InsertToListContext ctx) {
        return new InsertAt(
                makeStringExpr(ctx.insertion),
                makeNumExpr(ctx.location),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public ReplaceItem visitReplaceItemInList(ScratchBlocksGrammarParser.ReplaceItemInListContext ctx) {
        return new ReplaceItem(
                makeStringExpr(ctx.newItem),
                makeNumExpr(ctx.oldItem),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public ShowList visitShowList(ScratchBlocksGrammarParser.ShowListContext ctx) {
        return new ShowList(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public HideList visitHideList(ScratchBlocksGrammarParser.HideListContext ctx) {
        return new HideList(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }
    //end subregion: variable blocks

    // endregion: statements

    // region: expressions

    // subregion: bool expressions

    @Override
    public Expression visitExprOrLiteral(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        if (ctx.numLiteral() != null) {
            return visitNumLiteral(ctx.numLiteral());
        } else if (ctx.stringLiteral() != null) {
            return visitStringLiteral(ctx.stringLiteral());
        } else {
            return visitExpression(ctx.expression());
        }
    }

    @Override
    public Touching visitTouching(ScratchBlocksGrammarParser.TouchingContext ctx) {
        Touchable touchable = visitTouchingChoice(ctx.touchingChoice());
        return new Touching(touchable, new NoBlockMetadata());
    }

    @Override
    public Touchable visitTouchingChoice(ScratchBlocksGrammarParser.TouchingChoiceContext ctx) {
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
    public Touchable visitFixedTouching(ScratchBlocksGrammarParser.FixedTouchingContext ctx) {
        if (ctx.getText().equals("mouse-pointer")) {
            return new MousePointer(new NoBlockMetadata());
        } else {
            return new Edge(new NoBlockMetadata());
        }
    }

    @Override
    public SpriteTouchingColor visitTouchingColor(ScratchBlocksGrammarParser.TouchingColorContext ctx) {
        Touchable color = visitTouchingColorChoice(ctx.touchingColorChoice());
        return new SpriteTouchingColor(color, new NoBlockMetadata());
    }

    @Override
    public ColorTouchingColor visitColorTouchingColor(ScratchBlocksGrammarParser.ColorTouchingColorContext ctx) {
        return new ColorTouchingColor(
                (Color) visitTouchingColorChoice(ctx.firstColor),
                (Color) visitTouchingColorChoice(ctx.secondColor),
                new NoBlockMetadata()
        );
    }

    @Override
    public Touchable visitTouchingColorChoice(ScratchBlocksGrammarParser.TouchingColorChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new AsTouchable(visitExprOrLiteral(ctx.exprOrLiteral()));
        } else {
            String rgbCode = ctx.HEX().getText();
            return ColorLiteral.tryFromRgbHexString(rgbCode);
        }
    }

    @Override
    public IsKeyPressed visitKeyPressed(ScratchBlocksGrammarParser.KeyPressedContext ctx) {
        return new IsKeyPressed(visitKeySelect(ctx.keySelect()), new NoBlockMetadata());
    }

    @Override
    public Key visitKeySelect(ScratchBlocksGrammarParser.KeySelectContext ctx) {
        if (ctx.key() != null) {
            return visitKey(ctx.key());
        } else {
            return new Key(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Key visitKey(ScratchBlocksGrammarParser.KeyContext ctx) {
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
    public IsMouseDown visitMouseDown(ScratchBlocksGrammarParser.MouseDownContext ctx) {
        return new IsMouseDown(new NoBlockMetadata());
    }

    @Override
    public BiggerThan visitGreaterThan(ScratchBlocksGrammarParser.GreaterThanContext ctx) {
        return new BiggerThan(
                (ComparableExpr) visitExprOrLiteral(ctx.firstExpr),
                (ComparableExpr) visit(ctx.secondExpr),
                new NoBlockMetadata()
        );
    }

    @Override
    public LessThan visitLessThan(ScratchBlocksGrammarParser.LessThanContext ctx) {
        return new LessThan(
                (ComparableExpr) visitExprOrLiteral(ctx.firstExpr),
                (ComparableExpr) visit(ctx.secondExpr),
                new NoBlockMetadata()
        );
    }

    @Override
    public Equals visitEqual(ScratchBlocksGrammarParser.EqualContext ctx) {
        return new Equals(
                (ComparableExpr) visitExprOrLiteral(ctx.firstExpr),
                (ComparableExpr) visit(ctx.secondExpr),
                new NoBlockMetadata()
        );
    }

    @Override
    public Not visitNot(ScratchBlocksGrammarParser.NotContext ctx) {
        return new Not(makeBoolExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public And visitAnd(ScratchBlocksGrammarParser.AndContext ctx) {
        return new And(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Or visitOr(ScratchBlocksGrammarParser.OrContext ctx) {
        return new Or(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public StringContains visitContains(ScratchBlocksGrammarParser.ContainsContext ctx) {
        return new StringContains(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public ListContains visitListContains(ScratchBlocksGrammarParser.ListContainsContext ctx) {
        return new ListContains(
                new StrId(visitStringArgument(ctx.stringArgument())),
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new NoBlockMetadata()
        );
    }

    //end subregion: bool expressions

    //subregion num expression

    @Override
    public PositionX visitXPosition(ScratchBlocksGrammarParser.XPositionContext ctx) {
        return new PositionX(new NoBlockMetadata());
    }

    @Override
    public PositionY visitYPosition(ScratchBlocksGrammarParser.YPositionContext ctx) {
        return new PositionY(new NoBlockMetadata());
    }

    @Override
    public Direction visitDirection(ScratchBlocksGrammarParser.DirectionContext ctx) {
        return new Direction(new NoBlockMetadata());
    }

    @Override
    public Costume visitNumCostume(ScratchBlocksGrammarParser.NumCostumeContext ctx) {
        return new Costume(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public NameNum visitNameNum(ScratchBlocksGrammarParser.NameNumContext ctx) {
        return new NameNum(ctx.getText());
    }

    @Override
    public Backdrop visitNumBackdrop(ScratchBlocksGrammarParser.NumBackdropContext ctx) {
        return new Backdrop(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public Size visitSize(ScratchBlocksGrammarParser.SizeContext ctx) {
        return new Size(new NoBlockMetadata());
    }

    @Override
    public Volume visitVolume(ScratchBlocksGrammarParser.VolumeContext ctx) {
        return new Volume(new NoBlockMetadata());
    }

    @Override
    public DistanceTo visitDistanceTo(ScratchBlocksGrammarParser.DistanceToContext ctx) {
        return new DistanceTo(visitDistanceChoice(ctx.distanceChoice()), new NoBlockMetadata());
    }

    @Override
    public Position visitDistanceChoice(ScratchBlocksGrammarParser.DistanceChoiceContext ctx) {
        if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else if (ctx.stringArgument() != null) {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Answer visitAnswer(ScratchBlocksGrammarParser.AnswerContext ctx) {
        return new Answer(new NoBlockMetadata());
    }

    @Override
    public MouseX visitMouseX(ScratchBlocksGrammarParser.MouseXContext ctx) {
        return new MouseX(new NoBlockMetadata());
    }

    @Override
    public MouseY visitMouseY(ScratchBlocksGrammarParser.MouseYContext ctx) {
        return new MouseY(new NoBlockMetadata());
    }

    @Override
    public Loudness visitLoudness(ScratchBlocksGrammarParser.LoudnessContext ctx) {
        return new Loudness(new NoBlockMetadata());
    }

    @Override
    public Timer visitTimer(ScratchBlocksGrammarParser.TimerContext ctx) {
        return new Timer(new NoBlockMetadata());
    }

    @Override
    public AttributeOf visitActorAttribute(ScratchBlocksGrammarParser.ActorAttributeContext ctx) {
        return new AttributeOf(
                visitAttributeChoice(ctx.attributeChoice()),
                visitElement(ctx.element()),
                new NoBlockMetadata()
        );
    }

    @Override
    public ElementChoice visitElement(ScratchBlocksGrammarParser.ElementContext ctx) {
        if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Attribute visitAttributeChoice(ScratchBlocksGrammarParser.AttributeChoiceContext ctx) {
        if (ctx.fixedAttribute() != null) {
            return new AttributeFromFixed(visitFixedAttribute(ctx.fixedAttribute()));
        } else {
            return new AttributeFromVariable(new Variable(new StrId(visitStringArgument(ctx.stringArgument()))));
        }
    }

    @Override
    public FixedAttribute visitFixedAttribute(ScratchBlocksGrammarParser.FixedAttributeContext ctx) {
        return new FixedAttribute(ctx.getText());
    }

    @Override
    public Current visitCurrentTime(ScratchBlocksGrammarParser.CurrentTimeContext ctx) {
        return new Current(visitCurrentChoice(ctx.currentChoice()), new NoBlockMetadata());
    }

    @Override
    public TimeComp visitCurrentChoice(ScratchBlocksGrammarParser.CurrentChoiceContext ctx) {
        if (ctx.getText().equals("day of the week")) {
            return new TimeComp("dayofweek");
        }
        return new TimeComp(ctx.getText());
    }

    @Override
    public DaysSince2000 visitDaysSince(ScratchBlocksGrammarParser.DaysSinceContext ctx) {
        return new DaysSince2000(new NoBlockMetadata());
    }

    @Override
    public Username visitUserName(ScratchBlocksGrammarParser.UserNameContext ctx) {
        return new Username(new NoBlockMetadata());
    }

    @Override
    public Add visitAddition(ScratchBlocksGrammarParser.AdditionContext ctx) {
        return new Add(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Minus visitSubtraction(ScratchBlocksGrammarParser.SubtractionContext ctx) {
        return new Minus(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Mult visitMultiplication(ScratchBlocksGrammarParser.MultiplicationContext ctx) {
        return new Mult(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Div visitDivision(ScratchBlocksGrammarParser.DivisionContext ctx) {
        return new Div(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public PickRandom visitPickRandom(ScratchBlocksGrammarParser.PickRandomContext ctx) {
        return new PickRandom(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Join visitJoin(ScratchBlocksGrammarParser.JoinContext ctx) {
        return new Join(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LetterOf visitGetLetterAtIndex(ScratchBlocksGrammarParser.GetLetterAtIndexContext ctx) {
        return new LetterOf(makeNumExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LengthOfString visitLengthOf(ScratchBlocksGrammarParser.LengthOfContext ctx) {
        return new LengthOfString(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Mod visitModulo(ScratchBlocksGrammarParser.ModuloContext ctx) {
        return new Mod(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Round visitRound(ScratchBlocksGrammarParser.RoundContext ctx) {
        return new Round(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public NumFunctOf visitMathFunction(ScratchBlocksGrammarParser.MathFunctionContext ctx) {
        return new NumFunctOf(
                visitMathChoice(ctx.mathChoice()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata()
        );
    }

    @Override
    public NumFunct visitMathChoice(ScratchBlocksGrammarParser.MathChoiceContext ctx) {
        return new NumFunct(ctx.getText());
    }

    @Override
    public ItemOfVariable visitItemAtIndex(ScratchBlocksGrammarParser.ItemAtIndexContext ctx) {
        return new ItemOfVariable(
                makeNumExpr(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public IndexOf visitIndexOfItem(ScratchBlocksGrammarParser.IndexOfItemContext ctx) {
        return new IndexOf(
                visitExprOrLiteral(ctx.exprOrLiteral()),
                new StrId(visitStringArgument(ctx.stringArgument())),
                new NoBlockMetadata()
        );
    }

    @Override
    public LengthOfVar visitLengthOfList(ScratchBlocksGrammarParser.LengthOfListContext ctx) {
        return new LengthOfVar(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    //end subregion: num expressions

    @Override
    public NumberLiteral visitNumLiteral(ScratchBlocksGrammarParser.NumLiteralContext ctx) {
        final String value;
        if (ctx.DIGIT() != null) {
            value = ctx.DIGIT().getText();
        } else {
            value = ctx.NUMBER().getText();
        }

        return new NumberLiteral(Double.parseDouble(value));
    }

    @Override
    public StringLiteral visitStringLiteral(ScratchBlocksGrammarParser.StringLiteralContext ctx) {
        return visitStringArgument(ctx.stringArgument());
    }

    @Override
    public Expression visitExpression(ScratchBlocksGrammarParser.ExpressionContext ctx) {
        if (ctx.stringArgument() != null) {
            return new Variable(new StrId(visitStringArgument(ctx.stringArgument())));
        } else if (ctx.boolExpr() != null) {
            return (Expression) visitBoolExpr(ctx.boolExpr());
        } else if (ctx.numExpr() != null) {
            return (Expression) visitNumExpr(ctx.numExpr());
        } else {
            return (Expression) super.visitExpression(ctx);
        }
    }

    @Override
    public StringLiteral visitStringArgument(ScratchBlocksGrammarParser.StringArgumentContext ctx) {
        return new StringLiteral(ctx.getText()
                .replaceAll("\\\\(?=[\\w" + SPECIAL_WITHOUT_BSLASH + "])", "") // Remove superfluous \
                .replace("\\\\", "\\")); // Handle double backslash
    }

    private NumExpr makeNumExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        NumExpr numExpr;

        if (expr instanceof NumExpr num) {
            numExpr = num;
        } else {
            numExpr = new AsNumber(expr);
        }
        return numExpr;
    }

    private StringExpr makeStringExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        StringExpr stringExpr;

        if (expr instanceof StringExpr str) {
            stringExpr = str;
        } else {
            stringExpr = new AsString(expr);
        }
        return stringExpr;
    }

    private BoolExpr makeBoolExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
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

    private StmtList makeInnerStmtList(ScratchBlocksGrammarParser.StmtListContext ctx) {
        StmtList stmt;

        if (ctx != null) {
            stmt = visitStmtList(ctx);
        } else {
            stmt = new StmtList();
        }
        return stmt;
    }
}
