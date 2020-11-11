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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

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

    public static final String SCRATCHBLOCKS_START = "[scratchblocks]";
    public static final String SCRATCHBLOCKS_END = "[/scratchblocks]";
    public static final String BUG_NOTE = "⇦  \uD83D\uDC1B   ";

    private boolean inScript = false;

    private boolean hasContent = false;

    private Program program = null;

    private ActorDefinition currentActor = null;

    private ByteArrayOutputStream byteStream = null;

    private boolean lineWrapped = true;

    private final Set<Issue> issues = new LinkedHashSet<>();

    private final Set<String> issueNote = new LinkedHashSet<>();

    public ScratchBlocksVisitor() {
        super(null);
        byteStream = new ByteArrayOutputStream();
        printStream = new PrintStream(byteStream);
    }

    public ScratchBlocksVisitor(PrintStream stream) {
        super(stream);
    }

    public ScratchBlocksVisitor(Issue issue) {
        this();
        this.issues.add(issue);
        this.program = issue.getProgram();
        this.currentActor = issue.getActor();
    }

    public ScratchBlocksVisitor(Collection<Issue> issues) {
        this();
        this.issues.addAll(issues);
        if (!issues.isEmpty()) {
            // TODO: This assumes all issues are reported on the same program
            this.program = issues.iterator().next().getProgram();

            // TODO: Probably we should use preconditions to ensure that the program
            //       itself is used as entry point for the visitor when using multiple
            //       issues, as this is an implicit assumption here. Otherwise
            //       variables such as currentActor may not be initialised
        }
    }

    public void setCurrentActor(ActorDefinition node) {
        currentActor = node;
    }

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        super.visit(node);
        currentActor = null;
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        for (ProcedureDefinition procedureDefinition : node.getList()) {
            if (hasContent) {
                newLine();
            }
            procedureDefinition.accept(this);
            hasContent = true;
        }
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            if (hasContent) {
                newLine();
            }
            script.accept(this);
            hasContent = true;
        }
    }

    @Override
    public void visit(Program program) {
        this.program = program;
        super.visit(program);
    }


    @Override
    public void visit(Script script) {
        inScript = true;
        super.visit(script);
        storeNotesForIssue(script);
        inScript = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        inScript = true;
        emitNoSpace("define ");
        String actorName = currentActor.getIdent().getName();
        String procedureName = program.getProcedureMapping().getProcedures().get(actorName).get(node.getIdent()).getName();

        List<ParameterDefinition> parameters = node.getParameterDefinitionList().getParameterDefinitions();
        for (ParameterDefinition param : parameters) {
            int nextIndex = procedureName.indexOf('%');
            procedureName = procedureName.substring(0, nextIndex)
                    + getParameterName(param)
                    + procedureName.substring(nextIndex + 2);
        }

        emitNoSpace(procedureName);
        storeNotesForIssue(node);
        newLine();
        visit(node.getStmtList());
        inScript = false;
    }

    //---------------------------------------------------------------
    // Event blocks

    @Override
    public void visit(Never never) {
        // No-op
    }

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("when green flag clicked");
        storeNotesForIssue(greenFlag);
        newLine();
    }

    @Override
    public void visit(Clicked clicked) {
        emitNoSpace("when this sprite clicked");
        storeNotesForIssue(clicked);
        newLine();
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        emitNoSpace("when [");
        Key key = keyPressed.getKey();
        assert (key.getKey() instanceof NumberLiteral);
        NumberLiteral num = (NumberLiteral) key.getKey();
        emitNoSpace(BlockJsonCreatorHelper.getKeyValue((int) num.getValue()));
        storeNotesForIssue(key);
        emitNoSpace(" v] key pressed");
        storeNotesForIssue(keyPressed);
        newLine();
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitToken("when I start as a clone");
        storeNotesForIssue(startedAsClone);
        newLine();
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitNoSpace("when I receive [");
        Message message = receptionOfMessage.getMsg();
        assert (message.getMessage() instanceof StringLiteral);
        StringLiteral literal = (StringLiteral) message.getMessage();
        emitNoSpace(literal.getText());
        storeNotesForIssue(message);
        emitNoSpace(" v]");
        storeNotesForIssue(receptionOfMessage);
        newLine();
    }

    public void visit(BackdropSwitchTo backdrop) {
        emitNoSpace("when backdrop switches to [");
        backdrop.getBackdrop().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(backdrop);
        newLine();
    }

    public void visit(AttributeAboveValue node) {
        emitNoSpace("when [");
        node.getAttribute().accept(this);
        emitNoSpace(" v] > ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Broadcast node) {
        emitNoSpace("broadcast ");
        node.getMessage().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        emitNoSpace("broadcast ");
        node.getMessage().accept(this);
        emitNoSpace(" and wait");
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Control blocks

    @Override
    public void visit(WaitSeconds node) {
        emitNoSpace("wait ");
        node.getSeconds().accept(this);
        emitNoSpace(" seconds");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(WaitUntil node) {
        emitNoSpace("wait until ");
        node.getUntil().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopAll node) {
        emitNoSpace("stop [all v]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        emitToken("stop [other scripts in sprite v]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopThisScript node) {
        emitToken("stop [this script v]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(CreateCloneOf node) {
        emitNoSpace("create clone of ");
        node.getStringExpr().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(DeleteClone node) {
        emitToken("delete this clone");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitToken("forever");
        storeNotesForIssue(repeatForeverStmt);
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
        storeNotesForIssue(untilStmt);
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
        storeNotesForIssue(repeatTimesStmt);
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
        storeNotesForIssue(ifThenStmt);
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
        storeNotesForIssue(ifElseStmt);
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
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnLeft node) {
        emitNoSpace("turn left ");
        node.getDegrees().accept(this);
        emitNoSpace(" degrees");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnRight node) {
        emitNoSpace("turn right ");
        node.getDegrees().accept(this);
        emitNoSpace(" degrees");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(GoToPos node) {
        emitNoSpace("go to ");
        node.getPosition().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(GoToPosXY node) {
        emitNoSpace("go to x: ");
        node.getX().accept(this);
        emitNoSpace(" y: ");
        node.getY().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(GlideSecsTo node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(" secs to ");
        node.getPosition().accept(this);
        storeNotesForIssue(node);
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
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PointInDirection node) {
        emitNoSpace("point in direction ");
        node.getDirection().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PointTowards node) {
        emitNoSpace("point towards ");
        node.getPosition().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeXBy node) {
        emitNoSpace("change x by ");
        node.getNum().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetXTo node) {
        emitNoSpace("set x to ");
        node.getNum().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeYBy node) {
        emitNoSpace("change y by ");
        node.getNum().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetYTo node) {
        emitNoSpace("set y to ");
        node.getNum().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        emitNoSpace("if on edge, bounce");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetRotationStyle node) {
        emitNoSpace("set rotation style [");
        node.getRotation().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
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
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Say node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ThinkForSecs node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(" for ");
        node.getSecs().accept(this);
        emitNoSpace(" seconds");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Think node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        emitNoSpace("switch costume to ");
        node.getCostumeChoice().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(NextCostume node) {
        emitNoSpace("next costume");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        emitNoSpace("switch backdrop to ");
        if (node.getElementChoice() instanceof Next) {
            emitNoSpace("(next backdrop v)");
        } else if (node.getElementChoice() instanceof Prev) {
            emitNoSpace("(previous backdrop v)");
        } else if (node.getElementChoice() instanceof Random) {
            emitNoSpace("(random backdrop v)");
        } else {
            node.getElementChoice().accept(this);
        }
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(NextBackdrop node) {
        emitNoSpace("next backdrop");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        emitNoSpace("change size by ");
        node.getNum().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetSizeTo node) {
        emitNoSpace("set size to ");
        node.getPercent().accept(this);
        emitNoSpace(" %");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        emitNoSpace("change [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect by ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        emitNoSpace("set [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect to ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        emitNoSpace("clear graphic effects");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Show node) {
        emitNoSpace("show");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Hide node) {
        emitNoSpace("hide");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(GoToLayer node) {
        emitNoSpace("go to [");
        node.getLayerChoice().accept(this);
        emitNoSpace(" v] layer");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        emitNoSpace("go [");
        node.getForwardBackwardChoice().accept(this);
        emitNoSpace(" v] ");
        node.getNum().accept(this);
        emitNoSpace(" layers");
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Sound blocks

    @Override
    public void visit(PlaySoundUntilDone node) {
        emitNoSpace("play sound ");
        node.getElementChoice().accept(this);
        emitNoSpace(" until done");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StartSound node) {
        emitNoSpace("start sound ");
        node.getElementChoice().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopAllSounds node) {
        emitNoSpace("stop all sounds");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        emitNoSpace("change [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect by ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        emitNoSpace("set [");
        node.getEffect().accept(this);
        emitNoSpace(" v] effect to ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        emitNoSpace("clear sound effects");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        emitNoSpace("change volume by ");
        node.getVolumeValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetVolumeTo node) {
        emitNoSpace("set volume to ");
        node.getVolumeValue().accept(this);
        emitNoSpace(" %");
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Sensing blocks

    @Override
    public void visit(AskAndWait node) {
        emitNoSpace("ask ");
        node.getQuestion().accept(this);
        emitNoSpace(" and wait");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetDragMode node) {
        emitNoSpace("set drag mode [");
        node.getDrag().accept(this);
        emitNoSpace(" v");
        storeNotesForIssue(node);
        emitNoSpace("]");
        newLine();
    }

    @Override
    public void visit(ResetTimer node) {
        emitNoSpace("reset timer");
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Pen blocks
    @Override
    public void visit(PenClearStmt node) {
        emitNoSpace("erase all");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PenStampStmt node) {
        emitNoSpace("stamp");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PenDownStmt node) {
        emitNoSpace("pen down");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PenUpStmt node) {
        emitNoSpace("pen up");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        emitNoSpace("set pen color to ");
        node.getColorExpr().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        emitNoSpace("change pen ");

        if (node.getParam() instanceof StringLiteral) {
            emitNoSpace("(");
            StringLiteral literal = (StringLiteral) node.getParam();
            emitNoSpace(literal.getText());
            emitNoSpace(" v)");
        } else {
            node.getParam().accept(this);
        }
        emitNoSpace(" by ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        emitNoSpace("set pen ");
        if (node.getParam() instanceof StringLiteral) {
            emitNoSpace("(");
            StringLiteral literal = (StringLiteral) node.getParam();
            emitNoSpace(literal.getText());
            emitNoSpace(" v)");
        } else {
            node.getParam().accept(this);
        }
        emitNoSpace(" to ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        emitNoSpace("change pen size by ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetPenSizeTo node) {
        emitNoSpace("set pen size to ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Variables blocks

    @Override
    public void visit(SetVariableTo node) {
        if (!inScript) {
            return;
        }
        emitNoSpace("set [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] to ");
        if (node.getExpr() instanceof Qualified) {
            emitNoSpace("(");
        }
        node.getExpr().accept(this);
        if (node.getExpr() instanceof Qualified) {
            emitNoSpace(")");
        }
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        emitNoSpace("change [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] by ");
        if (node.getExpr() instanceof AsNumber && !(((AsNumber)node.getExpr()).getOperand1() instanceof Qualified)) {
            ((AsNumber)node.getExpr()).getOperand1().accept(this);
        } else {
            //
            node.getExpr().accept(this);
        }
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ShowVariable node) {
        emitNoSpace("show variable [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(HideVariable node) {
        emitNoSpace("hide variable [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(AddTo node) {
        emitNoSpace("add ");
        node.getString().accept(this);
        emitNoSpace(" to [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(DeleteOf node) {
        emitNoSpace("delete ");
        node.getNum().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(DeleteAllOf node) {
        emitNoSpace("delete all of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
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
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
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
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ShowList node) {
        emitNoSpace("show list [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(HideList node) {
        emitNoSpace("hide list [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v");
        emitNoSpace("]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ItemOfVariable node) {
        emitNoSpace("(item ");
        node.getNum().accept(this);
        emitNoSpace(" of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(IndexOf node) {
        emitNoSpace("(item # of ");
        node.getExpr().accept(this);
        emitNoSpace(" in [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(LengthOfVar node) {
        emitNoSpace("(length of [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(ListContains node) {
        emitNoSpace("<[");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] contains ");
        node.getElement().accept(this);
        emitNoSpace(" ?");
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(NumberLiteral number) {
        if (!inScript) {
            return;
        }

        emitNoSpace("(");
        double num = number.getValue();
        if (num % 1 == 0) {
            emitNoSpace(Integer.toString((int) num));
        } else {
            emitNoSpace(String.valueOf(num));
        }
        storeNotesForIssue(number);
        emitNoSpace(")");
    }

    @Override
    public void visit(AttributeOf node) {
        emitNoSpace("([");
        node.getAttribute().accept(this);
        emitNoSpace(" v] of ");
        node.getElementChoice().accept(this);

        emitNoSpace("?");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(WithExpr node) {
        if (node.getExpression() instanceof StrId) {
            emitNoSpace("(");
            StrId literal = (StrId) node.getExpression();
            if (literal.getName().equals("_stage_")) {
                emitNoSpace("Stage");
            } else {
                node.getExpression().accept(this);
            }
            emitNoSpace(" v)");
        } else if (node.getExpression() instanceof Qualified) {
            emitNoSpace("(");
            node.getExpression().accept(this);
            emitNoSpace(")");
        } else {
            node.getExpression().accept(this);
        }
    }

    @Override
    public void visit(FixedAttribute node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Volume node) {
        emitNoSpace("(volume");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Timer node) {
        emitNoSpace("(timer");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Answer node) {
        emitNoSpace("(answer");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(MousePos node) {
        emitNoSpace("(mouse-pointer v)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RandomPos node) {
        emitNoSpace("(random position v)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RotationStyle node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ExpressionStmt node) {
        if (node.getExpression() instanceof Qualified) {
            DataExpr dataExpr = ((Qualified)node.getExpression()).getSecond();
            if (dataExpr instanceof Variable || dataExpr instanceof ScratchList) {
                emitNoSpace("(");
            }
        }
        node.getExpression().accept(this);
        if (node.getExpression() instanceof Qualified) {
            DataExpr dataExpr = ((Qualified)node.getExpression()).getSecond();
            if (dataExpr instanceof Variable) {
                emitNoSpace(")");
            } else if (dataExpr instanceof ScratchList) {
                emitNoSpace(" :: list");
                emitNoSpace(")");
            }
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ScratchList node) {
        if (!inScript) {
            return;
        }
        node.getName().accept(this);
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Variable node) {
        if (!inScript) {
            return;
        }
        node.getName().accept(this);
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Backdrop node) {
        emitNoSpace("(backdrop [");
        node.getType().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(NameNum node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (inScript) {
            emitNoSpace("[");
            emitNoSpace(stringLiteral.getText());
            emitNoSpace("]");
            storeNotesForIssue(stringLiteral);
        }
    }

    @Override
    public void visit(ColorLiteral colorLiteral) {
        emitNoSpace("[#");
        emitNoSpace(Long.toHexString(colorLiteral.getRed()));
        emitNoSpace(Long.toHexString(colorLiteral.getGreen()));
        emitNoSpace(Long.toHexString(colorLiteral.getBlue()));
        emitNoSpace("]");
        storeNotesForIssue(colorLiteral);
    }

    @Override
    public void visit(GraphicEffect node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SoundEffect node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(LayerChoice node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DragMode node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Message node) {
        StringExpr message = node.getMessage();
        if (message instanceof StringLiteral) {
            emitNoSpace("(");
            StringLiteral literal = (StringLiteral) message;
            emitNoSpace(literal.getText());
            emitNoSpace(" v)");
            storeNotesForIssue(node);
        } else {
            node.getMessage().accept(this);
            storeNotesForIssue(node);
        }
    }

    @Override
    public void visit(EventAttribute node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Qualified node) {
        node.getSecond().accept(this);
        storeNotesForIssue(node);
    }

    @Override
    public void visit(PositionX node) {
        emitNoSpace("(x position");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(PositionY node) {
        emitNoSpace("(y position");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Direction node) {
        emitNoSpace("(direction");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Size node) {
        emitNoSpace("(size");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(MouseX node) {
        emitNoSpace("(mouse x");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(MouseY node) {
        emitNoSpace("(mouse y");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(DaysSince2000 node) {
        emitNoSpace("(days since 2000");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Username node) {
        emitNoSpace("(username");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Loudness node) {
        emitNoSpace("(loudness");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(DistanceTo node) {
        emitNoSpace("(distance to ");
        node.getPosition().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Current node) {
        emitNoSpace("(current (");
        node.getTimeComp().accept(this);
        emitNoSpace(" v)");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(TimeComp node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Costume node) {
        emitNoSpace("(costume [");
        node.getType().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(AsNumber node) {
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace("(");
        }
        node.getOperand1().accept(this);
        storeNotesForIssue(node);
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(AsTouchable node) {
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace("(");
        }
        node.getOperand1().accept(this);
        storeNotesForIssue(node);
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(AsBool node) {
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace("(");
        }
        node.getOperand1().accept(this);
        storeNotesForIssue(node);
        if (node.getOperand1() instanceof Qualified) {
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(AsString node) {
        if (node.getOperand1() instanceof SingularExpression) {
            node.getOperand1().accept(this);
        } else if (node.getOperand1() instanceof BoolExpr) {
            node.getOperand1().accept(this);
        } else if (node.getOperand1() instanceof NumExpr) {
            node.getOperand1().accept(this);
        } else if (node.getOperand1() instanceof Parameter) {
            node.getOperand1().accept(this);
        } else if (node.getOperand1() instanceof StrId) {
            emitNoSpace("(");
            final String spriteName = ((StrId) node.getOperand1()).getName();
            if (spriteName.equals("_myself_")) {
                emitNoSpace("myself");
            } else {
                emitNoSpace(spriteName);
            }
            emitNoSpace(" v)");
        } else {
            emitNoSpace("(");
            node.getOperand1().accept(this);
            storeNotesForIssue(node);
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(Add node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("+");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("-");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("*");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace("/");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Round node) {
        emitNoSpace("(round ");
        node.getOperand1().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(PickRandom node) {
        emitNoSpace("(pick random ");
        node.getOperand1().accept(this);
        emitNoSpace(" to ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mod node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" mod ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(NumFunctOf node) {
        emitNoSpace("([");
        node.getOperand1().accept(this);
        emitNoSpace(" v] of ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(NumFunct node) {
        emitNoSpace(node.getTypeName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(BiggerThan node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" > ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" < ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" = ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Not node) {
        emitNoSpace("<not ");
        node.getOperand1().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(And node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" and ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Or node) {
        emitNoSpace("<");
        node.getOperand1().accept(this);
        emitNoSpace(" or ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(StringContains node) {
        emitNoSpace("<");
        node.getContaining().accept(this);
        emitNoSpace(" contains ");
        node.getContained().accept(this);
        emitNoSpace("?");
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Touching node) {
        emitNoSpace("<touching ");
        node.getTouchable().accept(this);
        emitNoSpace(" ?");
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Edge node) {
        emitNoSpace("(edge v)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(MousePointer node) {
        emitNoSpace("(mouse-pointer v)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        emitNoSpace("<touching color ");
        node.getColor().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(" ?>");
    }

    @Override
    public void visit(SpriteTouchable node) {
        emitNoSpace("(");
        // TODO: Why is the signature a StringExpr if it is always set to a StringLiteral
        StringLiteral literal = (StringLiteral)node.getStringExpr();
        emitNoSpace(literal.getText());
        emitNoSpace(" v)");
    }

    @Override
    public void visit(ColorTouchingColor node) {
        emitNoSpace("<color ");
        node.getOperand1().accept(this);
        emitNoSpace(" is touching ");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(" ?>");
    }

    @Override
    public void visit(IsKeyPressed node) {
        emitNoSpace("<key ");
        node.getKey().accept(this);
        emitNoSpace(" pressed?");
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Key node) {
        if (node.getKey() instanceof NumberLiteral) {
            emitNoSpace("(");
            NumberLiteral num = (NumberLiteral) node.getKey();
            emitNoSpace(BlockJsonCreatorHelper.getKeyValue((int) num.getValue()));
            emitNoSpace(" v)");
        } else {
            node.getKey().accept(this);
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        emitNoSpace("<mouse down?");
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        emitNoSpace("<>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        emitNoSpace("[]");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        emitNoSpace("()");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Join node) {
        emitNoSpace("(join ");
        node.getOperand1().accept(this);
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(LetterOf node) {
        emitNoSpace("(letter ");
        node.getNum().accept(this);
        emitNoSpace(" of ");
        node.getStringExpr().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(LengthOfString node) {
        emitNoSpace("(length of ");
        node.getStringExpr().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Parameter node) {
        NonDataBlockMetadata metaData = (NonDataBlockMetadata)node.getMetadata();
        if (metaData.getOpcode().equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            emitNoSpace("<");
        } else {
            emitNoSpace("(");
        }
        visitChildren(node);
        storeNotesForIssue(node);
        if (metaData.getOpcode().equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            emitNoSpace(">");
        } else {
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(StrId strId) {
        if (!inScript) {
            return;
        }
        emitNoSpace(strId.getName());
        storeNotesForIssue(strId);
    }

    @Override
    public void visit(CallStmt node) {
        String procedureName = node.getIdent().getName();
        List<Expression> parameters = node.getExpressions().getExpressions();
        for (Expression param : parameters) {
            int nextIndex = procedureName.indexOf('%');
            String type = procedureName.substring(nextIndex, nextIndex + 1); // Todo: Unused variable?
            procedureName = procedureName.substring(0, nextIndex)
                    + getParameterName(param)
                    + procedureName.substring(nextIndex + 2);
        }
        emitNoSpace(procedureName);
        storeNotesForIssue(node);
        newLine();
    }

    public void begin() {
        emitNoSpace(SCRATCHBLOCKS_START);
        newLine();
        lineWrapped = true;
    }

    public void end() {
        if (!lineWrapped) {
            newLine();
        }
        emitNoSpace(SCRATCHBLOCKS_END);
        newLine();
        lineWrapped = true;
    }

    protected void emitNoSpace(String string) {
        printStream.append(string);
        lineWrapped = false;
    }

    // FIXME: Hacky because of PrintVisitor interface, maybe this shouldn't be extended after all?
    public String getScratchBlocks() {
        if (byteStream == null) {
            throw new IllegalArgumentException("To access this function, do not set a PrintStream in the constructor");
        }
        return byteStream.toString();
    }

    protected void newLine() {
        if (issueNote.size() == 1) {
            emitNoSpace(" // ");
            emitNoSpace(issueNote.iterator().next());
            issueNote.clear();
        } else if (issueNote.size() > 1) {
            emitNoSpace(" // ");
            emitNoSpace(String.join(", ", issueNote));
            issueNote.clear();
        }
        emitNoSpace(System.lineSeparator());
        lineWrapped = true;
    }

    private void storeNotesForIssue(ASTNode node) {
        boolean hasIssue = false;
        for (Issue issue : issues) {
            if (issue.getCodeLocation() == node) {
                if (!hasIssue) {
                    emitNoSpace(":: #ff0000");
                }
                hasIssue = true;
                issueNote.add(BUG_NOTE);
                // TODO: In theory there could be multiple messages here...
                // issueNote.add(issue.getTranslatedFinderName());
            }
        }
    }

    private String getParameterName(ParameterDefinition node) {
        // FIXME: Terrible hack
        PrintStream origStream = printStream;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        printStream = new PrintStream(os);

        if (node.getType() instanceof StringType) {
            emitNoSpace("[");
            node.getIdent().accept(this);
            storeNotesForIssue(node);
            emitNoSpace("]");
        } else if (node.getType() instanceof NumberType) {
            emitNoSpace("(");
            node.getIdent().accept(this);
            storeNotesForIssue(node);
            emitNoSpace(")");
        } else if (node.getType() instanceof BooleanType) {
            emitNoSpace("<");
            node.getIdent().accept(this);
            storeNotesForIssue(node);
            emitNoSpace(">");
        }
        String name = os.toString();
        printStream = origStream;
        return name;
    }

    private String getParameterName(Expression node) {
        // FIXME: Terrible hack
        PrintStream origStream = printStream;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        printStream = new PrintStream(os);
        node.accept(this);
        String name = os.toString();
        printStream = origStream;
        return name;
    }
}
