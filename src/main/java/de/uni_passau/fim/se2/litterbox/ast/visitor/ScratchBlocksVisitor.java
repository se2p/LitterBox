/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;


import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper;

import java.io.PrintStream;

/*
 * Documentation of syntax:
 * https://en.scratch-wiki.info/wiki/Block_Plugin/Syntax
 *
 * Every scratch block goes on a new line.
 * Example:
 *
 * [scratchblocks]
 * when green flag clicked
 * forever
 *     turn cw (15) degrees
 *     say [Hello!] for (2) secs
 *     if <mouse down?> then
 *         change [mouse clicks v] by (1)
 *     end
 * end
 * [/scratchblocks]
 */
public class ScratchBlocksVisitor extends PrintVisitor {

    private boolean inScript = false;

    public ScratchBlocksVisitor(PrintStream stream) {
        super(stream);
    }

    @Override
    public void visit(Script script) {
        emitNoSpace("[scratchblocks]");
        inScript = true;
        newLine();
        super.visit(script);
        emitNoSpace("[/scratchblocks]");
        inScript = false;
        newLine();
    }

    //---------------------------------------------------------------
    // Event blocks

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("when green flag clicked");
        newLine();
    }


    @Override
    public void visit(Clicked clicked) {
        emitNoSpace("when this sprite clicked");
        newLine();
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        emitNoSpace("when ");
        keyPressed.getKey().accept(this);
        emitNoSpace(" key pressed");
        newLine();
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitToken("when I start as a clone");
        newLine();
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitNoSpace("when I receive [");
        receptionOfMessage.getMsg().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    public void visit(BackdropSwitchTo backdrop) {
        emitNoSpace("when backdrop switches to [");
        backdrop.getBackdrop().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    public void visit(AttributeAboveValue node) {
        emitNoSpace("when [");
        node.getAttribute().accept(this);
        emitNoSpace(" v] > ");
        node.getValue().accept(this);
        newLine();
    }


    @Override
    public void visit(Broadcast node) {
        emitNoSpace("broadcast (");
        node.getMessage().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        emitNoSpace("broadcast (");
        node.getMessage().accept(this);
        emitNoSpace(" v) and wait");
        newLine();
    }



    //---------------------------------------------------------------
    // Control blocks

    @Override
    public void visit(WaitSeconds node) {
        emitNoSpace("wait ");
        node.getSeconds().accept(this);
        emitNoSpace(" seconds");
        newLine();
    }

    @Override
    public void visit(WaitUntil node) {
        emitNoSpace("wait until ");
        node.getUntil().accept(this);
        newLine();
    }

    @Override
    public void visit(StopAll node) {
        emitNoSpace("stop [all v]");
        newLine();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        emitToken("stop [other scripts in sprite v]");
        newLine();
    }

    @Override
    public void visit(StopThisScript node) {
        emitToken("stop [this script v]");
        newLine();
    }


    @Override
    public void visit(CreateCloneOf node) {
        emitNoSpace("create clone of (");

        // TODO: There must be a nicer way to do this...
        if (node.getStringExpr() instanceof AsString
                && ((AsString) node.getStringExpr()).getOperand1() instanceof StrId) {

            final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();
            if (spriteName.equals("_myself_")) {
                emitNoSpace("myself");
            } else {
                emitNoSpace(spriteName);
            }
        } else {
            node.getStringExpr().accept(this);
        }
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(DeleteClone node) {
        emitToken("delete this clone");
        newLine();
    }


    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitToken("forever");
        newLine();
        beginIndentation();
        repeatForeverStmt.getStmtList().accept(this);
        endIndentation();
        emitNoSpace("end");
        newLine();
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitNoSpace("repeat until ");
        untilStmt.getBoolExpr().accept(this);
        newLine();
        beginIndentation();
        untilStmt.getStmtList().accept(this);
        endIndentation();
        emitNoSpace("end");
        newLine();
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitNoSpace("repeat ");
        repeatTimesStmt.getTimes().accept(this);
        newLine();
        beginIndentation();
        repeatTimesStmt.getStmtList().accept(this);
        endIndentation();
        emitNoSpace("end");
        newLine();
    }

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        emitNoSpace("if ");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(" then");
        newLine();
        beginIndentation();
        ifThenStmt.getThenStmts().accept(this);
        endIndentation();
        emitNoSpace("end");
        newLine();
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        emitNoSpace("if ");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(" then");
        newLine();
        beginIndentation();
        ifElseStmt.getStmtList().accept(this);
        endIndentation();
        emitNoSpace("else");
        newLine();
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
        emitNoSpace("end");
        newLine();
    }

    //---------------------------------------------------------------
    // Motion blocks

    @Override
    public void visit(MoveSteps node) {
        emitNoSpace("move ");
        node.getSteps().accept(this);
        emitNoSpace(" steps");
        newLine();
    }

    @Override
    public void visit(TurnLeft node) {
        emitNoSpace("turn left ");
        node.getDegrees().accept(this);
        emitNoSpace(" degrees");
        newLine();
    }

    @Override
    public void visit(TurnRight node) {
        emitNoSpace("turn right ");
        node.getDegrees().accept(this);
        emitNoSpace(" degrees");
        newLine();
    }

    @Override
    public void visit(GoToPos node) {
        emitNoSpace("go to (");
        node.getPosition().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(GoToPosXY node) {
        emitNoSpace("go to x: ");
        node.getX().accept(this);
        emitNoSpace(" y: ");
        node.getY().accept(this);
        newLine();
    }

    @Override
    public void visit(GlideSecsTo node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(" secs to (");
        node.getPosition().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(" secs to x: ");
        node.getX().accept(this);
        emitNoSpace(" y: ");
        node.getY().accept(this);
        newLine();
    }

    @Override
    public void visit(PointInDirection node) {
        emitNoSpace("point in direction ");
        node.getDirection().accept(this);
        newLine();
    }

    @Override
    public void visit(PointTowards node) {
        emitNoSpace("point towards (");
        node.getPosition().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(ChangeXBy node) {
        emitNoSpace("change x by ");
        node.getNum().accept(this);
        newLine();
    }

    @Override
    public void visit(SetXTo node) {
        emitNoSpace("set x to ");
        node.getNum().accept(this);
        newLine();
    }

    @Override
    public void visit(ChangeYBy node) {
        emitNoSpace("change y by ");
        node.getNum().accept(this);
        newLine();
    }

    @Override
    public void visit(SetYTo node) {
        emitNoSpace("set y to ");
        node.getNum().accept(this);
        newLine();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        emitNoSpace("if on edge, bounce");
        newLine();
    }

    @Override
    public void visit(SetRotationStyle node) {
        emitNoSpace("set rotation style [");
        node.getRotation().accept(this);
        emitNoSpace(" v]");
        newLine();
    }


    //---------------------------------------------------------------
    // Looks blocks

    @Override
    public void visit(SayForSecs node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        emitNoSpace(" for ");
        node.getSecs().accept(this);
        emitNoSpace(" seconds");
        newLine();
    }

    @Override
    public void visit(Say node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        newLine();
    }

    @Override
    public void visit(ThinkForSecs node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(" for ");
        node.getSecs().accept(this);
        emitNoSpace(" seconds");
        newLine();
    }

    @Override
    public void visit(Think node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        newLine();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        emitNoSpace("switch costume to (");
        node.getCostumeChoice().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(NextCostume node) {
        emitNoSpace("next costume");
        newLine();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        emitNoSpace("switch backdrop to (");
        if(node.getElementChoice() instanceof Next) {
            emitNoSpace("next backdrop");
        } else if(node.getElementChoice() instanceof Prev) {
            emitNoSpace("previous backdrop");
        } else if(node.getElementChoice() instanceof Random) {
            emitNoSpace("random backdrop");
        } else {
            node.getElementChoice().accept(this);
        }
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(NextBackdrop node) {
        emitNoSpace("next backdrop");
        newLine();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        emitNoSpace("change size by ");
        node.getNum().accept(this);
        newLine();
    }

    @Override
    public void visit(SetSizeTo node) {
        emitNoSpace("set size to ");
        node.getPercent().accept(this);
        emitNoSpace(" %");
        newLine();
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        emitNoSpace("change [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect by ");
        node.getValue().accept(this);
        newLine();
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        emitNoSpace("set [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect to ");
        node.getValue().accept(this);
        newLine();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        emitNoSpace("clear graphic effects");
        newLine();
    }

    @Override
    public void visit(Show node) {
        emitNoSpace("show");
        newLine();
    }

    @Override
    public void visit(Hide node) {
        emitNoSpace("hide");
        newLine();
    }

    @Override
    public void visit(GoToLayer node) {
        emitNoSpace("go to [");
        node.getLayerChoice().accept(this);
        emitNoSpace(" v] layer");
        newLine();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        emitNoSpace("go [");
        node.getForwardBackwardChoice().accept(this);
        emitNoSpace(" v] ");
        node.getNum().accept(this);
        emitNoSpace(" layers");
        newLine();
    }


    //---------------------------------------------------------------
    // Sound blocks

    @Override
    public void visit(PlaySoundUntilDone node) {
        emitNoSpace("play sound (");
        node.getElementChoice().accept(this);
        emitNoSpace(" v) until done");
        newLine();
    }

    @Override
    public void visit(StartSound node) {
        emitNoSpace("start sound (");
        node.getElementChoice().accept(this);
        emitNoSpace(" v)");
        newLine();
    }

    @Override
    public void visit(StopAllSounds node) {
        emitNoSpace("stop all sounds");
        newLine();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        emitNoSpace("change [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect by ");
        node.getValue().accept(this);
        newLine();
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        emitNoSpace("set [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect to ");
        node.getValue().accept(this);
        newLine();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        emitNoSpace("clear sound effects");
        newLine();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        emitNoSpace("change volume by ");
        node.getVolumeValue().accept(this);
        newLine();
    }

    @Override
    public void visit(SetVolumeTo node) {
        emitNoSpace("set volume to ");
        node.getVolumeValue().accept(this);
        emitNoSpace(" %");
        newLine();
    }

    //---------------------------------------------------------------
    // Sensing blocks

    @Override
    public void visit(AskAndWait node) {
        emitNoSpace("ask ");
        node.getQuestion().accept(this);
        emitNoSpace(" and wait");
        newLine();
    }

    @Override
    public void visit(SetDragMode node) {
        emitNoSpace("set drag mode [");
        node.getDrag().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(ResetTimer node) {
        emitNoSpace("reset timer");
        newLine();
    }


    //---------------------------------------------------------------
    // Variables blocks

    @Override
    public void visit(SetVariableTo node) {
        if(!inScript) {
            return;
        }
        emitNoSpace("set [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] to ");
        node.getExpr().accept(this);
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        emitNoSpace("change [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] by ");
        node.getExpr().accept(this);
        newLine();
    }

    @Override
    public void visit(ShowVariable node) {
        emitNoSpace("show variable [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(HideVariable node) {
        emitNoSpace("hide variable [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(AddTo node) {
        emitNoSpace("add ");
        node.getString().accept(this);
        emitNoSpace(" to [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(DeleteOf node) {
        emitNoSpace("delete ");
        node.getNum().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(DeleteAllOf node) {
        emitNoSpace("delete all of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(InsertAt node) {
        emitNoSpace("insert ");
        node.getString().accept(this);
        emitNoSpace(" at ");
        node.getIndex().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(ReplaceItem node) {
        emitNoSpace("replace item ");
        node.getIndex().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] with ");
        node.getString().accept(this);
        newLine();
    }

    @Override
    public void visit(ShowList node) {
        emitNoSpace("show list [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(HideList node) {
        emitNoSpace("hide list [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        newLine();
    }

    @Override
    public void visit(ItemOfVariable node) {
        emitNoSpace("(item ");
        node.getNum().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v])");
    }

    @Override
    public void visit(IndexOf node) {
        emitNoSpace("(item # of ");
        node.getExpr().accept(this);
        emitNoSpace(" in [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v])");
    }

    @Override
    public void visit(LengthOfVar node) {
        emitNoSpace("(length of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v])");
    }

    @Override
    public void visit(ListContains node) {
        emitNoSpace("<[");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] contains ");
        node.getElement().accept(this);
        emitNoSpace(" ?>");
    }

    @Override
    public void visit(NumberLiteral number) {
        if(!inScript) {
            return;
        }

        emitNoSpace("(");
        double num = number.getValue();
        if(num % 1 == 0) {
            emitNoSpace(Integer.toString((int)num));
        } else {
            emitNoSpace(String.valueOf(num));
        }
        emitNoSpace(")");
    }


    @Override
    public void visit(AttributeOf node) {
        emitNoSpace("([");
        node.getAttribute().accept(this);
        emitNoSpace(" v] of (");

        // TODO: How to do this nicer?
        if(node.getElementChoice() instanceof WithExpr && ((WithExpr)node.getElementChoice()).getExpression() instanceof StrId) {
            StrId literal = (StrId) ((WithExpr)node.getElementChoice()).getExpression();
            if(literal.getName().equals("_stage_")) {
                emitNoSpace("Stage");
            } else {
                node.getElementChoice().accept(this);
            }
        } else {
            node.getElementChoice().accept(this);
        }
        emitNoSpace(" v)?)");
    }


    @Override
    public void visit(FixedAttribute node) {
        emitNoSpace(node.getType());
    }

    @Override
    public void visit(Volume node) {
        emitNoSpace("(volume)");
    }

    @Override
    public void visit(Timer node) {
        emitNoSpace("(timer)");
    }

    @Override
    public void visit(Answer node) {
        emitNoSpace("(answer)");
    }

    @Override
    public void visit(MousePos node) {
        emitNoSpace("mouse-pointer");
    }

    @Override
    public void visit(RandomPos node) {
        emitNoSpace("random position");
    }

    @Override
    public void visit(RotationStyle node) {
        emitNoSpace(node.getToken());
    }

//    @Override
//    public void visit(Backdrop node) {
//        node.getType().accept(this);
//    }

    @Override
    public void visit(Backdrop node) {
        emitNoSpace("(backdrop [");
        node.getType().accept(this);
        emitNoSpace(" v])");
    }


    @Override
    public void visit(NameNum node) {
        emitNoSpace(node.getType());
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if(inScript) {
            emitNoSpace("[");
            emitNoSpace(stringLiteral.getText());
            emitNoSpace("]");
        }
    }

    @Override
    public void visit(ColorLiteral colorLiteral) {
        emitNoSpace("[#");
        emitNoSpace(Long.toHexString(colorLiteral.getRed()));
        emitNoSpace(Long.toHexString(colorLiteral.getGreen()));
        emitNoSpace(Long.toHexString(colorLiteral.getBlue()));
        emitNoSpace("]");
    }

    @Override
    public void visit(GraphicEffect node) {
        emitNoSpace(node.getToken());
    }

    @Override
    public void visit(SoundEffect node) {
        emitNoSpace(node.getToken());
    }

    @Override
    public void visit(LayerChoice node) {
        emitNoSpace(node.getType());
    }

    @Override
    public void visit(DragMode node) {
        emitNoSpace(node.getToken());
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        emitNoSpace(node.getType());
    }

    @Override
    public void visit(Message node) {
        StringExpr message = node.getMessage();
        if (message instanceof StringLiteral) {
            StringLiteral literal = (StringLiteral)message;
            emitNoSpace(literal.getText());
        } else {
            node.getMessage().accept(this);
        }
    }

    @Override
    public void visit(EventAttribute node) {
        emitNoSpace(node.getType());
    }

    @Override
    public void visit(Qualified node) {
        node.getSecond().accept(this);
    }

    @Override
    public void visit(PositionX node) {
        emitNoSpace("(x position)");
    }

    @Override
    public void visit(PositionY node) {
        emitNoSpace("(y position)");
    }

    @Override
    public void visit(Direction node) {
        emitNoSpace("(direction)");
    }

    @Override
    public void visit(Size node) {
        emitNoSpace("(size)");
    }

    @Override
    public void visit(MouseX node) {
        emitNoSpace("(mouse x)");
    }

    @Override
    public void visit(MouseY node) {
        emitNoSpace("(mouse y)");
    }

    @Override
    public void visit(DaysSince2000 node) {
        emitNoSpace("(days since 2000)");
    }

    @Override
    public void visit(Username node) {
        emitNoSpace("(username)");
    }

    @Override
    public void visit(Loudness node) {
        emitNoSpace("(loudness)");
    }

    @Override
    public void visit(DistanceTo node) {
        emitNoSpace("(distance to (");
        node.getPosition().accept(this);
        emitNoSpace(" v)");
    }

    @Override
    public void visit(Current node) {
        emitNoSpace("(current (");
        node.getTimeComp().accept(this);
        emitNoSpace(" v)");
    }

    @Override
    public void visit(TimeComp node) {
        emitNoSpace(node.getLabel());
    }

    @Override
    public void visit(Costume node) {
        emitNoSpace("(costume [");
        node.getType().accept(this);
        emitNoSpace(" v])");
    }

    @Override
    public void visit(AsNumber node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(AsString node) {
        if(node.getOperand1() instanceof SingularExpression) {
            node.getOperand1().accept(this);
        } else if(node.getOperand1() instanceof BoolExpr) {
            node.getOperand1().accept(this);
        } else if(node.getOperand1() instanceof NumExpr) {
            node.getOperand1().accept(this);
        } else {
            emitNoSpace("(");
            node.getOperand1().accept(this);
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(Add node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("+");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("-");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("*");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("/");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Round node) {
        emitNoSpace("(round ");
        node.getOperand1().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(PickRandom node) {
        emitNoSpace("(pick random ");
        node.getOperand1().accept(this);
        emitNoSpace(" to ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mod node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" mod ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(NumFunctOf node) {
        emitNoSpace("([");
        node.getOperand1().accept(this);
        emitNoSpace(" v] of ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(NumFunct node) {
        emitNoSpace(node.getFunction());
    }

    @Override
    public void visit(BiggerThan node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" > ");
        node.getOperand2().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" < ");
        node.getOperand2().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" = ");
        node.getOperand2().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(Not node) {
        emitNoSpace("<not ");
        node.getOperand1().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(And node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" and ");
        node.getOperand2().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(Or node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" or ");
        node.getOperand2().accept(this);
        emitNoSpace(">");
    }

    @Override
    public void visit(StringContains node) {
        emitNoSpace("<");
        node.getContaining().accept(this);
        emitNoSpace(" contains ");
        node.getContained().accept(this);
        emitNoSpace("?>");
    }

    @Override
    public void visit(Touching node) {
        emitNoSpace("<touching (");
        node.getTouchable().accept(this);
        emitNoSpace(" v) ?>");
    }

    @Override
    public void visit(Edge node) {
        emitNoSpace("edge");
    }

    @Override
    public void visit(MousePointer node) {
        emitNoSpace("mouse-pointer");
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        emitNoSpace("<touching color ");
        node.getColor().accept(this);
        emitNoSpace(" ?>");
    }

    @Override
    public void visit(ColorTouchingColor node) {
        emitNoSpace("<color ");
        node.getOperand1().accept(this);
        emitNoSpace(" is touching ");
        node.getOperand2().accept(this);
        emitNoSpace(" ?>");
    }

    @Override
    public void visit(IsKeyPressed node) {
        emitNoSpace("<key (");
        node.getKey().accept(this);
        emitNoSpace(" v) pressed?>");
    }

    @Override
    public void visit(Key node) {
        assert(node.getKey() instanceof NumberLiteral);
        NumberLiteral num = (NumberLiteral)node.getKey();
        emitNoSpace(BlockJsonCreatorHelper.getKeyValue((int)num.getValue()));
    }

    @Override
    public void visit(IsMouseDown node) {
        emitNoSpace("<mouse down?>");
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        emitNoSpace("<>");
    }

    @Override
    public void visit(Join node) {
        emitNoSpace("(join ");
        node.getOperand1().accept(this);
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(LetterOf node) {
        emitNoSpace("(letter ");
        node.getNum().accept(this);
        emitNoSpace(" of ");
        node.getStringExpr().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(LengthOfString node) {
        emitNoSpace("(length of ");
        node.getStringExpr().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(StrId strId) {
        if(!inScript) {
            return;
        }
        emitNoSpace(strId.getName());
    }
//
//    @Override
//    public void visit(BoolLiteral boolLiteral) {
//        emitToken(String.valueOf(boolLiteral.getValue()));
//    }



    // TODO: This is a dummy for now
    public String getScratchBlocks() {
        return "[scratchblocks]\n" +
                "when green flag clicked\n" +
                "todo\n" +
                "[/scratchblocks]\n";
    }
}
