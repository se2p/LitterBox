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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.IRMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.LearnWithTime;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendIR;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendLearnResult;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetAxis;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetTimer2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.ExprDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.ExprInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.ExprNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
public class ScratchBlocksVisitor extends PrintVisitor implements
        PenExtensionVisitor, TextToSpeechExtensionVisitor, MBlockVisitor, MusicExtensionVisitor {

    public static final String SCRATCHBLOCKS_START = "[scratchblocks]";
    public static final String SCRATCHBLOCKS_END = "[/scratchblocks]";
    public static final String BUG_NOTE = "⇦  \uD83D\uDC1B   ";
    public static final String PERFUME_NOTE = "⇦  \uD83D\uDC4D   ";
    public static final String QUESTION_NOTE = "⇦   \u2753   ";
    public static final String SPRITE_MARKER = "//Sprite:";
    public static final String SCRIPT_ID_MARKER = "//Script:";

    private boolean inScript = false;

    private boolean hasContent = false;

    private Program program = null;

    private ActorDefinition currentActor = null;

    private ByteArrayOutputStream byteStream = null;

    private boolean lineWrapped = true;

    private final Set<Issue> issues = new LinkedHashSet<>();

    private final Set<String> issueNote = new LinkedHashSet<>();

    private boolean requireScript = true;

    private boolean ignoreLooseBlocks = false;

    private boolean addActorNames = false;

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

    /**
     * Builds a new visitor that can optionally handle standalone blocks which are not part of a script.
     *
     * @param requireScript Set this to {@code false} if you want to use this visitor on the level of single blocks,
     *                      i.e., without their context in a script.
     *                      This prevents certain blocks from not being printed as they have to be ignored if they
     *                      occur outside of scripts when printing whole programs.
     */
    public ScratchBlocksVisitor(boolean requireScript, boolean ignoreLooseBlocks) {
        this();
        this.requireScript = requireScript;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    /**
     * Converts the given node of the AST into the ScratchBlocks format.
     *
     * @param node Some node of the AST.
     * @return The ScratchBlocks representation of this node.
     */
    public static String of(final ASTNode node) {
        return of(node, false);
    }

    /**
     * Converts the given node of the AST into the ScratchBlocks format.
     *
     * <p>Any loose blocks in the program (i.e., scripts without hat block) are ignored.
     *
     * @param node Some node of the AST.
     * @return The ScratchBlocks representation of this node.
     */
    public static String ofIgnoringLooseBlocks(final ASTNode node) {
        return of(node, true);
    }

    private static String of(final ASTNode node, final boolean ignoreLooseBlocks) {
        final ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(false, ignoreLooseBlocks);
        visitor.setProgram(AstNodeUtil.findParent(node, Program.class));
        visitor.setCurrentActor(AstNodeUtil.findParent(node, ActorDefinition.class));
        visitor.setAddActorNames(true);

        node.accept(visitor);

        return visitor.getScratchBlocks();
    }

    private boolean isIgnoredBlock() {
        return !inScript && requireScript;
    }

    public void setCurrentActor(ActorDefinition node) {
        currentActor = node;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        if (addActorNames) {
            if (!node.getProcedureDefinitionList().getList().isEmpty() || node.getScripts().getSize() > 0) {
                if (hasContent) {
                    newLine();
                }
                emitNoSpace(SPRITE_MARKER + " " + node.getIdent().getName());
                newLine();
                hasContent = true;
            }
        }
        node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);
        currentActor = null;
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        for (ProcedureDefinition procedureDefinition : node.getList()) {
            if (hasContent) {
                newLine();
            }
            if (addActorNames) {
                emitNoSpace(SCRIPT_ID_MARKER + " " + AstNodeUtil.getBlockId(procedureDefinition));
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
            if (addActorNames) {
                emitNoSpace(SCRIPT_ID_MARKER + " " + AstNodeUtil.getBlockId(script));
                newLine();
            }
            script.accept(this);
            hasContent = true;
        }
    }

    @Override
    public void visit(Program program) {
        this.program = program;
        program.getActorDefinitionList().accept(this);
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never && ignoreLooseBlocks) {
            return;
        }

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
        String procedureName = program.getProcedureMapping().getProcedures()
                .get(actorName).get(node.getIdent()).getName();
        procedureName = escapeBrackets(procedureName);
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
    public void visit(SpriteClicked spriteClicked) {
        emitNoSpace("when this sprite clicked");
        storeNotesForIssue(spriteClicked);
        newLine();
    }

    @Override
    public void visit(StageClicked stageClicked) {
        emitNoSpace("when this stage clicked");
        storeNotesForIssue(stageClicked);
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
        emitNoSpace("when I start as a clone");
        storeNotesForIssue(startedAsClone);
        newLine();
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitNoSpace("when I receive [");
        Message message = receptionOfMessage.getMsg();
        assert (message.getMessage() instanceof StringLiteral);
        StringLiteral literal = (StringLiteral) message.getMessage();
        emitNoSpace(escapeBrackets(literal.getText()));
        storeNotesForIssue(message);
        emitNoSpace(" v]");
        storeNotesForIssue(receptionOfMessage);
        newLine();
    }

    @Override
    public void visit(BackdropSwitchTo backdrop) {
        emitNoSpace("when backdrop switches to [");
        backdrop.getBackdrop().accept(this);
        emitNoSpace(" v]");
        storeNotesForIssue(backdrop);
        newLine();
    }

    @Override
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
        emitNoSpace("stop [other scripts in sprite v]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopThisScript node) {
        emitNoSpace("stop [this script v]");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(CreateCloneOf node) {
        emitNoSpace("create clone of ");
        node.getCloneChoice().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Myself node) {
        emitNoSpace("(myself v)");
    }

    @Override
    public void visit(WithCloneExpr node) {
        if (node.getExpression() instanceof StrId) {
            emitNoSpace("(");
            node.getExpression().accept(this);
            emitNoSpace(" v)");
        } else if (node.getExpression() instanceof Qualified qualified) {
            handlePossibleQualified(qualified);
        } else {
            node.getExpression().accept(this);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        emitNoSpace("delete this clone");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("forever");
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
        ifElseStmt.getThenStmts().accept(this);
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
    public void visit(SwitchBackdropAndWait node) {
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
        emitNoSpace(" and wait");
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
        node.getParam().accept((PenExtensionVisitor) this);
        emitNoSpace(" by ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        emitNoSpace("set pen ");
        node.getParam().accept((PenExtensionVisitor) this);
        emitNoSpace(" to ");
        node.getValue().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(FixedColorParam node) {
        emitNoSpace("(");
        emitNoSpace(node.getType().getType());
        emitNoSpace(" v)");
    }

    @Override
    public void visit(ColorParamFromExpr node) {
        node.getExpr().accept(this);
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
    // TextToSpeech blocks

    @Override
    public void visit(FixedLanguage node) {
        emitNoSpace("( ");
        emitNoSpace(getLanguage(node));
        emitNoSpace(" v)");
    }

    private static String getLanguage(FixedLanguage node) {
        return switch (node.getType()) {
            case ARABIC -> "Arabic";
            case CHINESE -> "Chinese (Mandarin)";
            case DANISH -> "Danish";
            case DUTCH -> "Dutch";
            case ENGLISH -> "English";
            case FRENCH -> "French";
            case GERMAN -> "German";
            case HINDI -> "Hindi";
            case ICELANDIC -> "Icelandic";
            case ITALIAN -> "Italien";
            case JAPANESE -> "Japanese";
            case KOREAN -> "Korean";
            case NORWEGIAN -> "Norwegian";
            case POLISH -> "Polish";
            case PORTUGUESE_BR -> "Portuguese (Brazilian)";
            case PORTUGUESE -> "Portuguese";
            case ROMANIAN -> "Romanian";
            case RUSSIAN -> "Russian";
            case SPANISH -> "Spanish";
            case SPANISH_419 -> "Spanish (Latin America)";
            case SWEDISH -> "Swedish";
            case TURKISH -> "Turkish";
            case WELSH -> "Welsh";
        };
    }

    @Override
    public void visit(ExprLanguage node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ExprVoice node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(FixedVoice node) {
        emitNoSpace("( ");
        emitNoSpace(node.getType().getType());
        emitNoSpace(" v)");
    }

    @Override
    public void visit(SetLanguage node) {
        emitNoSpace("set language to ");
        node.getLanguage().accept((TextToSpeechExtensionVisitor) this);
        storeNotesForIssue(node);
        emitNoSpace(" :: tts");
        newLine();
    }

    @Override
    public void visit(SetVoice node) {
        emitNoSpace("set voice to ");
        node.getVoice().accept((TextToSpeechExtensionVisitor) this);
        storeNotesForIssue(node);
        emitNoSpace(" :: tts");
        newLine();
    }

    @Override
    public void visit(Speak node) {
        emitNoSpace("speak ");
        node.getText().accept(this);
        emitNoSpace(" :: tts");
        storeNotesForIssue(node);
        newLine();
    }

    //---------------------------------------------------------------
    // Variables blocks

    @Override
    public void visit(SetVariableTo node) {
        if (isIgnoredBlock()) {
            return;
        }
        emitNoSpace("set [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] to ");
        handlePossibleQualified(node.getExpr());
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        emitNoSpace("change [");
        node.getIdentifier().accept(this);
        emitNoSpace(" v] by ");
        handlePossibleQualified(node.getExpr());
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
        if (isIgnoredBlock()) {
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
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(WithExpr node) {
        if (node.getExpression() instanceof StrId literal) {
            emitNoSpace("(");
            if (literal.getName().equals("_stage_")) {
                emitNoSpace("Stage");
            } else {
                node.getExpression().accept(this);
            }
            emitNoSpace(" v)");
        } else if (node.getExpression() instanceof Qualified qualified) {
            handlePossibleQualified(qualified);
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
        handlePossibleQualified(node.getExpression());
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ScratchList node) {
        if (isIgnoredBlock()) {
            return;
        }
        node.getName().accept(this);
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Variable node) {
        if (isIgnoredBlock()) {
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
        if (!isIgnoredBlock()) {
            emitNoSpace("[");
            emitNoSpace(escapeBrackets(stringLiteral.getText()));
            emitNoSpace("]");
            storeNotesForIssue(stringLiteral);
        }
    }

    @Override
    public void visit(ColorLiteral colorLiteral) {
        emitNoSpace("[#");
        emitNoSpace(String.format("%02x", colorLiteral.getRed()));
        emitNoSpace(String.format("%02x", colorLiteral.getGreen()));
        emitNoSpace(String.format("%02x", colorLiteral.getBlue()));
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
        if (message instanceof StringLiteral literal) {
            emitNoSpace("(");
            emitNoSpace(escapeBrackets(literal.getText()));
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
        handlePossibleQualified(node.getOperand1());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(AsTouchable node) {
        handlePossibleQualified(node.getOperand1());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(AsBool node) {
        handlePossibleQualified(node.getOperand1());
        storeNotesForIssue(node);
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
        } else if (node.getOperand1() instanceof StrId strId) {
            emitNoSpace("(");
            final String spriteName = strId.getName();
            if (spriteName.equals("_myself_")) {
                emitNoSpace("myself");
            } else {
                emitNoSpace(escapeBrackets(spriteName));
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
        emitSpaced("+");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitSpaced("-");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitSpaced("*");
        node.getOperand2().accept(this);
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitSpaced("/");
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
        handlePossibleQualified(node.getOperand1());
        emitSpaced(">");
        handlePossibleQualified(node.getOperand2());
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("<");
        handlePossibleQualified(node.getOperand1());
        emitSpaced("<");
        handlePossibleQualified(node.getOperand2());
        storeNotesForIssue(node);
        emitNoSpace(">");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("<");
        handlePossibleQualified(node.getOperand1());
        emitSpaced("=");
        handlePossibleQualified(node.getOperand2());
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
        if (node.getStringExpr() instanceof StringLiteral literal) {
            emitNoSpace("(");
            emitNoSpace(escapeBrackets(literal.getText()));
            emitNoSpace(" v)");
        } else {
            node.getStringExpr().accept(this);
        }
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
        if (node.getKey() instanceof NumberLiteral num) {
            emitNoSpace("(");
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
        if (node.getOpcode().equals(ProcedureOpcode.argument_reporter_boolean)) {
            emitNoSpace("<");
        } else {
            emitNoSpace("(");
        }
        visitChildren(node);
        storeNotesForIssue(node);
        if (node.getOpcode().equals(ProcedureOpcode.argument_reporter_boolean)) {
            emitNoSpace(">");
        } else {
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(StrId strId) {
        if (isIgnoredBlock()) {
            return;
        }
        emitNoSpace(escapeBrackets(strId.getName()));
        storeNotesForIssue(strId);
    }

    @Override
    public void visit(CallStmt node) {
        String procedureName = node.getIdent().getName();
        procedureName = escapeBrackets(procedureName);
        List<Expression> parameters = node.getExpressions().getExpressions();
        for (Expression param : parameters) {
            int nextIndex = procedureName.indexOf('%');
            procedureName = procedureName.substring(0, nextIndex)
                    + getParameterName(param)
                    + procedureName.substring(nextIndex + 2);
        }
        if (procedureName.startsWith("+") || procedureName.startsWith("-")) {
            procedureName = "\\" + procedureName;
        }
        emitNoSpace(procedureName);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void begin() {
        emitNoSpace(SCRATCHBLOCKS_START);
        newLine();
        lineWrapped = true;
    }

    @Override
    public void end() {
        if (!lineWrapped) {
            newLine();
        }
        emitNoSpace(SCRATCHBLOCKS_END);
        newLine();
        lineWrapped = true;
    }

    @Override
    protected void emitNoSpace(String string) {
        printStream.append(string);
        lineWrapped = false;
    }

    private void emitSpaced(String string) {
        printStream.append(' ').append(string).append(' ');
        lineWrapped = false;
    }

    // FIXME: Hacky because of PrintVisitor interface, maybe this shouldn't be extended after all?
    public String getScratchBlocks() {
        if (byteStream == null) {
            throw new IllegalArgumentException("To access this function, do not set a PrintStream in the constructor");
        }
        return byteStream.toString();
    }

    @Override
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
            if (issue.isCodeLocation(node)) {
                if (!hasIssue) {
                    if (issue.getIssueType() == IssueType.PERFUME) {
                        emitNoSpace(" :: #167700");
                    } else {
                        emitNoSpace(" :: #ff0000");
                    }
                }
                hasIssue = true;
                if (!issue.hasMultipleBlocks()) {
                    if (issue.getIssueType() == IssueType.PERFUME) {
                        issueNote.add(PERFUME_NOTE);
                    } else if (issue.getIssueType() == IssueType.QUESTION) {
                        issueNote.add(QUESTION_NOTE);
                    } else {
                        issueNote.add(BUG_NOTE);
                    }
                } else {
                    List<ASTNode> nodes = ((MultiBlockIssue) issue).getNodes();
                    if (node == (nodes.get(0))) {
                        if (issue.getIssueType() == IssueType.PERFUME) {
                            issueNote.add(PERFUME_NOTE);
                        } else if (issue.getIssueType() == IssueType.QUESTION) {
                            issueNote.add(QUESTION_NOTE);
                        } else {
                            issueNote.add(BUG_NOTE);
                        }
                    }
                }
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
        handlePossibleQualified(node);
        String name = os.toString();
        printStream = origStream;
        return name;
    }

    private void handlePossibleQualified(ASTNode node) {
        if (node instanceof Qualified qualified) {
            emitNoSpace("(");
            qualified.accept(this);
            if (qualified.getSecond() instanceof ScratchList) {
                emitNoSpace(" :: list");
            }
            emitNoSpace(")");
        } else {
            node.accept(this);
        }
    }

    // mBlock: CodeyRocky and mBot

    @Override
    public void visit(BoardButtonAction node) {
        emitNoSpace("when on-board button [");
        node.getPressed().accept((MBlockVisitor) this);
        emitNoSpace(" v] :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BoardLaunch node) {
        emitNoSpace("when Codey starts up :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BoardShaken node) {
        emitNoSpace("when Codey is shaking :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BoardTilted node) {
        emitNoSpace("when Codey is [");
        node.getDirection().accept((MBlockVisitor) this);
        emitNoSpace(" v] tilted :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BrightnessLess node) {
        emitNoSpace("when light intensity \\< ");
        node.getValue().accept(this);
        emitNoSpace("  :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LaunchButton node) {
        emitNoSpace("when on-board button [");
        node.getButton().accept((MBlockVisitor) this);
        emitNoSpace(" v] :: events hat");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(BoardButtonPressed node) {
        emitNoSpace("<when on-board button [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] ? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ConnectRobot node) {
        emitNoSpace("<@codeyB when Codey connected to Rocky :: sensing> ");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(IRButtonPressed node) {
        emitNoSpace("<@mBot IR remote [");
        IRRemoteButton button = node.getOperand1();
        emitNoSpace(button.getButtonName());
        storeNotesForIssue(button);
        emitNoSpace(" v] pressed :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(LEDMatrixPosition node) {
        emitNoSpace("<@codeyB x: ");
        node.getOperand1().accept(this);
        emitNoSpace(" y: ");
        node.getOperand2().accept(this);
        emitNoSpace(" is it lighted up? :: looks>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ObstaclesAhead node) {
        emitNoSpace("<@mBot obstacles ahead? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(OrientateTo node) {
        emitNoSpace("<@codeyB Codey positioned as [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] ? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(PortOnLine node) {
        emitNoSpace("<@mBot line follower sensor [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] detects [");
        node.getOperand2().accept((MBlockVisitor) this);
        emitNoSpace(" v] being [");
        node.getOperand3().accept((MBlockVisitor) this);
        emitNoSpace(" v] ? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotButtonPressed node) {
        emitNoSpace("<@CodeyB button [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] is pressed? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotShaken node) {
        emitNoSpace("<@codeyB shaken ? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotTilted node) {
        emitNoSpace("<@codeyB Codey [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] tilted? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SeeColor node) {
        emitNoSpace("<@codeyB the color detected is [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] ? :: sensing>");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(AmbientLight node) {
        emitNoSpace("(@codeyB ambient light intensity :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(BatteryEnergy node) {
        emitNoSpace("(@codeyB battery level :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectAmbientLight node) {
        emitNoSpace("(@codeyB color sensor ambient light intensity :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectAmbientLightPort node) {
        emitNoSpace("(@mBot light sensor [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] light intensity :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectDistancePort node) {
        emitNoSpace("(@mBot ultrasonic sensor [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] distance \\(cm\\) :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectGrey node) {
        emitNoSpace("(@codeyB color sensor grey-scale value :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectIRReflection node) {
        emitNoSpace("(@codeyB color sensor reflected infrared light intensity :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectLinePort node) {
        emitNoSpace("(@mBot line follower sensor [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] value :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectReflection node) {
        emitNoSpace("(@codeyB color sensor reflected light intensity :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(DetectRGBValue node) {
        emitNoSpace("(@codeyB [");
        node.getOperand1().accept((MBlockVisitor) this);
        emitNoSpace(" v] color value detected :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(GyroPitchAngle node) {
        emitNoSpace("(@codeyB pitch angle° :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(GyroRollAngle node) {
        emitNoSpace("(@codeyB roll angle° :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Potentiometer node) {
        emitNoSpace("(@codeyB gear potentiometer value :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotTimer node) {
        emitNoSpace("(@codeyB timer :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RotateXAngle node) {
        emitNoSpace("(@codeyB rotation angle around x :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RotateYAngle node) {
        emitNoSpace("(@codeyB rotation angle around y :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RotateZAngle node) {
        emitNoSpace("(@codeyB rotation angle around z :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(ShakingStrength node) {
        emitNoSpace("(@codeyB shaking strength :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SoundVolume node) {
        emitNoSpace("(@codeyB loudness :: sensing)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SpeakerVolume node) {
        emitNoSpace("(@codeyB volume :: infrared)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(IRMessage node) {
        emitNoSpace("(@codeyB IR message received :: infrared)");
        storeNotesForIssue(node);
    }

    @Override
    public void visit(BlackWhite node) {
        switch (node.getBlackWhiteType()) {
            case BLACK -> emitNoSpace("black");
            case WHITE -> emitNoSpace("white");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(IRRemoteButton node) {
        emitNoSpace(node.getButtonName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(LEDColor node) {
        emitNoSpace(node.getColorType().getName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(LEDMatrix node) {
        emitNoSpace("@matrix");
        // TODO individual matrices
    }

    @Override
    public void visit(LEDPosition node) {
        switch (node.getPositionType()) {
            case ALL -> emitNoSpace("all");
            case RIGHT -> emitNoSpace("right");
            case LEFT -> emitNoSpace("left");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(LineFollowState node) {
        switch (node.getLineFollowType()) {
            case NONE -> emitNoSpace("none");
            case RIGHT -> emitNoSpace("rightside");
            case LEFT -> emitNoSpace("leftside");
            case ALL -> emitNoSpace("all");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(MCorePort node) {
        emitNoSpace("port" + node.getPortType().getDefinition());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(PadOrientation node) {
        switch (node.getOrientationType()) {
            case SCREEN_UP -> emitNoSpace("face up");
            case SCREEN_DOWN -> emitNoSpace("face down");
            case UPRIGHT -> emitNoSpace("stand on desk");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(PressedState node) {
        PressedState.EventPressState state = node.getPressed();
        if (state == PressedState.EventPressState.IS_PRESSED_TRUE) {
            emitNoSpace("pressed");
        } else {
            emitNoSpace("released");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RGB node) {
        emitNoSpace(node.getRGBType().getName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotAxis node) {
        switch (node.getAxisType()) {
            case ALL -> emitNoSpace("ALL");
            case X -> emitNoSpace("X");
            case Y -> emitNoSpace("Y");
            case Z -> emitNoSpace("Z");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotButton node) {
        switch (node.getButtonType()) {
            case A -> emitNoSpace("A");
            case B -> emitNoSpace("B");
            case C -> emitNoSpace("C");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(RobotDirection node) {
        switch (node.getDirectionType()) {
            case LEFT, RIGHT -> {
                String directionName = node.getDirectionName();
                emitNoSpace("tilted to the " + directionName);
            }
            case FORWARD -> emitNoSpace("ears up");
            case BACKWARD -> emitNoSpace("ears down");
        }
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SoundList node) {
        emitNoSpace(node.getFileName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(SoundNote node) {
        emitNoSpace(node.getNoteName());
        storeNotesForIssue(node);
    }

    @Override
    public void visit(Aggrieved node) {
        emitNoSpace("@codeyA hurt :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Agree node) {
        emitNoSpace("@codeyA yes :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Angry node) {
        emitNoSpace("@codeyA angry :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Awkward node) {
        emitNoSpace("@codeyA uh_oh :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Coquetry node) {
        emitNoSpace("@codeyA yummy :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Deny node) {
        emitNoSpace("@codeyA no :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Dizzy node) {
        emitNoSpace("@codeyA dizzy :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Exclaim node) {
        emitNoSpace("@codeyA wow :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Greeting node) {
        emitNoSpace("@codeyA hello :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LookAround node) {
        emitNoSpace("@codeyA look around :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LookDown node) {
        emitNoSpace("@codeyA look down :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LookLeft node) {
        emitNoSpace("@codeyA look left :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LookRight node) {
        emitNoSpace("@codeyA look right :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LookUp node) {
        emitNoSpace("@codeyA look up :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Naughty node) {
        emitNoSpace("@codeyA naughty :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Proud node) {
        emitNoSpace("@codeyA proud :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Revive node) {
        emitNoSpace("@codeyA wake :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Sad node) {
        emitNoSpace("@codeyA sad :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Shiver node) {
        emitNoSpace("@codeyA shiver :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Sleeping node) {
        emitNoSpace("@codeyA sleep :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Sleepy node) {
        emitNoSpace("@codeyA yawn :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Smile node) {
        emitNoSpace("@codeyA smile :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Sprint node) {
        emitNoSpace("@codeyA sprint :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Startle node) {
        emitNoSpace("@codeyA scared :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Wink node) {
        emitNoSpace("@codeyA blink :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Yeah node) {
        emitNoSpace("@codeyA yeah :: emotion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LearnWithTime node) {
        emitNoSpace("@codeyB record home appliances remote signal 3 secs :: infrared");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SendIR node) {
        emitNoSpace("@codeyB send IR message ");
        node.getText().accept(this);
        emitNoSpace(" :: infrared");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SendLearnResult node) {
        emitNoSpace("@codeyB send home appliances remote signal :: infrared");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDColorShow node) {
        emitNoSpace("@codeyA RGB LED lights up ");
        node.getColorString().accept(this);
        emitNoSpace(" :: lighting");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDColorShowPosition node) {
        emitNoSpace("@mBot LED [");
        node.getPosition().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows color ");
        node.getColorString().accept(this);
        emitNoSpace(" :: show");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDColorTimed node) {
        emitNoSpace("@codeyA RGB LED lights up ");
        node.getColorString().accept(this);
        emitNoSpace(" for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: lighting");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDColorTimedPosition node) {
        emitNoSpace("@mBot LED [");
        node.getPosition().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows color (#");
        node.getColorString().accept(this);
        emitNoSpace(") for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: show");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDOff node) {
        emitNoSpace("@codeyB RGB LED lights off :: lighting");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RGBValue node) {
        emitNoSpace("@codeyB set the indicator [");
        node.getRgb().accept((MBlockVisitor) this);
        emitNoSpace(" v] with color value ");
        node.getValue().accept(this);
        emitNoSpace(" :: lighting");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RGBValuesPosition node) {
        emitNoSpace("@mBot turn on [");
        node.getPosition().accept((MBlockVisitor) this);
        emitNoSpace(" v] light with color red ");
        node.getRed().accept(this);
        emitNoSpace(" green ");
        node.getGreen().accept(this);
        emitNoSpace(" blue ");
        node.getBlue().accept(this);
        emitNoSpace(" :: show");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RockyLight node) {
        emitNoSpace("@codeyB set Rocky's light with color [");
        node.getColor().accept((MBlockVisitor) this);
        emitNoSpace(" v] :: lighting");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(RockyLightOff node) {
        emitNoSpace("@codeyB Rocky lights off");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(FacePosition node) {
        // TODO show individual matrices
        emitNoSpace("@codeyB show image @matrix at the x:");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(FacePositionPort node) {
        // TODO show individual matrices
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows image @matrix at x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(FaceTimed node) {
        // TODO show individual matrices
        emitNoSpace("@codeyB show image @matrix for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(FaceTimedPort node) {
        // TODO show individual matrices
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows image @matrix for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDNumPort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows number ");
        node.getNumber().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDString node) {
        emitNoSpace("@codeyB show ");
        node.getText().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDStringPort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows text ");
        node.getText().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDStringPosition node) {
        // TODO show individual matrices
        emitNoSpace("@codeyB show image @matrix at the x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDStringPositionPort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows text ");
        node.getText().accept(this);
        emitNoSpace(" at x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDStringScrolling node) {
        emitNoSpace("@codeyB show ");
        node.getText().accept(this);
        emitNoSpace(" until scroll done :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDSwitchOff node) {
        emitNoSpace("@codeyB light off x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDSwitchOn node) {
        emitNoSpace("@codeyB light up x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDTimePort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows time ");
        node.getHour().accept(this);
        emitNoSpace(" : ");
        node.getMinute().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(LEDToggle node) {
        emitNoSpace("@codeyB switch between light-up and light-off x: ");
        node.getxAxis().accept(this);
        emitNoSpace(" y: ");
        node.getyAxis().accept(this);
        emitNoSpace(" :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ShowFace node) {
        // TODO show individual matrices
        emitNoSpace("@codeyB show image @matrix :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ShowFacePort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] shows image @matrix :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnOffFace node) {
        emitNoSpace("@codeyB turn off screen :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnOffFacePort node) {
        emitNoSpace("@mBot LED panel [");
        node.getPort().accept((MBlockVisitor) this);
        emitNoSpace(" v] clears screen :: looks");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(KeepBackwardTimed node) {
        emitNoSpace("@codeyA keep straight backward at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(KeepForwardTimed node) {
        emitNoSpace("@codeyA keep straight forward at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(MoveBackwardTimed node) {
        emitNoSpace("@codeyA move backward at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(MoveDirection node) {
        emitNoSpace("@codeyA [");
        String directionName = node.getDirection().getDirectionName();
        switch (node.getDirection().getDirectionType()) {
            case LEFT, RIGHT -> emitNoSpace("turn " + directionName);
            case FORWARD, BACKWARD -> emitNoSpace("move " + directionName);
        }
        emitNoSpace(" v] at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(MoveForwardTimed node) {
        emitNoSpace("@codeyA move forward at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(MoveSides node) {
        emitNoSpace("@codeyA left wheel turns at power ");
        node.getLeftPower().accept(this);
        emitNoSpace("");
        node.getRightPower().accept(this);
        emitNoSpace(" % :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(MoveStop node) {
        emitNoSpace("@codeyA stop moving :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnLeft2 node) {
        emitNoSpace("@codeyA turn left @turnLeft ");
        node.getDegree().accept(this);
        emitNoSpace(" degrees until done :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnLeftTimed node) {
        emitNoSpace("@codeyA turn left at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnRight2 node) {
        emitNoSpace("@codeyA turn right @turnRight ");
        node.getDegree().accept(this);
        emitNoSpace(" degrees until done :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(TurnRightTimed node) {
        emitNoSpace("@codeyA turn right at power ");
        node.getPercent().accept(this);
        emitNoSpace(" % for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: motion");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ResetAxis node) {
        emitNoSpace("@codeyA reset the [");
        node.getAxis().accept((MBlockVisitor) this);
        emitNoSpace(" v] rotation angle° :: sensing");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ResetTimer2 node) {
        emitNoSpace("@codeyA reset timer :: sensing");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeVolumeBy2 node) {
        emitNoSpace("@codeyB change volume by ");
        node.getVolumeValue().accept(this);
        emitNoSpace(" :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(Pause node) {
        emitNoSpace("@codeyB rest for ");
        node.getBeat().accept(this);
        emitNoSpace(" beats :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlayFrequency node) {
        emitNoSpace("@codeyB play sound at frequency of ");
        node.getFrequency().accept(this);
        emitNoSpace(" HZ for ");
        node.getTime().accept(this);
        emitNoSpace(" secs :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlayNote node) {
        emitNoSpace("@codeyB play note [");
        node.getNote().accept((MBlockVisitor) this);
        emitNoSpace(" v] for ");
        node.getBeat().accept(this);
        emitNoSpace(" beats :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlaySound node) {
        emitNoSpace("@codeyB play sound [");
        node.getSoundList().accept((MBlockVisitor) this);
        emitNoSpace(" v] :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlaySoundWait node) {
        emitNoSpace("@codeyB play sound [");
        node.getSoundList().accept((MBlockVisitor) this);
        emitNoSpace(" v] until done :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetVolumeTo2 node) {
        emitNoSpace("@codeyB set volume to ");
        node.getVolumeValue().accept(this);
        emitNoSpace(" % :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(StopAllSounds2 node) {
        emitNoSpace("@codeyB stop all sounds :: speaker");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visitParentVisitor(MBlockNode node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(MBlockNode node) {
        node.accept((MBlockVisitor) this);
    }

    // music extension

    @Override
    public void visit(Tempo node) {
        emitNoSpace("(Tempo");
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(RestForBeats node) {
        emitNoSpace("rest for ");
        node.getBeats().accept(this);
        emitNoSpace(" beats");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetTempoTo node) {
        emitNoSpace("set tempo to ");
        node.getTempo().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ChangeTempoBy node) {
        emitNoSpace("change tempo by ");
        node.getTempo().accept(this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlayNoteForBeats node) {
        emitNoSpace("play note ");
        node.getNote().accept((MusicExtensionVisitor) this);
        emitNoSpace(" for ");
        node.getBeats().accept(this);
        emitNoSpace("beats");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(PlayDrumForBeats node) {
        emitNoSpace("play drum ");
        node.getDrum().accept((MusicExtensionVisitor) this);
        emitNoSpace(" for ");
        node.getBeats().accept(this);
        emitNoSpace("beats");
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(SetInstrumentTo node) {
        emitNoSpace("set instrument to ");
        node.getInstrument().accept((MusicExtensionVisitor) this);
        storeNotesForIssue(node);
        newLine();
    }

    @Override
    public void visit(ExprInstrument node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(FixedInstrument node) {
        emitNoSpace("(");
        emitNoSpace(node.getType().getName());
        emitNoSpace(" v)");
    }

    @Override
    public void visit(ExprNote node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(FixedNote node) {
        emitNoSpace("(");
        double num = node.getNote();
        if (num % 1 == 0) {
            emitNoSpace(Integer.toString((int) num));
        } else {
            emitNoSpace(String.valueOf(num));
        }
        storeNotesForIssue(node);
        emitNoSpace(")");
    }

    @Override
    public void visit(ExprDrum node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(FixedDrum node) {
        emitNoSpace("(");
        emitNoSpace(node.getType().getName());
        emitNoSpace(" v)");
    }

    private String escapeBrackets(String name) {
        name = name.replace("(", "\\(");
        name = name.replace(")", "\\)");
        name = name.replace("[", "\\[");
        name = name.replace("]", "\\]");
        return name;
    }

    public void setAddActorNames(boolean addActorNames) {
        this.addActorNames = addActorNames;
    }
}
