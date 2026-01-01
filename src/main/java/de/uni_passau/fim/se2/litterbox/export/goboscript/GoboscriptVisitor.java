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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.CloneChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.export.AbstractExportVisitor;

import java.io.PrintStream;
import java.util.List;

/**
 * Exports LitterBox AST to Goboscript format.
 *
 * <p>Goboscript is a text-based programming language that compiles to Scratch.
 * Syntax uses C/Rust-style braces and semicolons.
 *
 * <p>Example output:
 * <pre>{@code
 * sprite Stage
 *
 * onflag {
 *     say "Hello!";
 *     forever {
 *         turn_right 15;
 *     }
 * }
 * }</pre>
 *
 * @see <a href="https://aspizu.github.io/goboscript/">Goboscript Documentation</a>
 */
public class GoboscriptVisitor extends AbstractExportVisitor {

    private boolean inScript = false;

    /**
     * Creates a new Goboscript visitor with internal buffering.
     */
    public GoboscriptVisitor() {
        super();
    }

    /**
     * Creates a new Goboscript visitor writing to the given stream.
     *
     * @param printStream The stream to write output to
     */
    public GoboscriptVisitor(PrintStream printStream) {
        super(printStream);
    }

    /**
     * Converts the given AST node to Goboscript format.
     *
     * @param node Some node of the AST
     * @return The Goboscript representation
     */
    public static String of(ASTNode node) {
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(AstNodeUtil.findParent(node, Program.class));
        visitor.setCurrentActor(AstNodeUtil.findParent(node, ActorDefinition.class));
        node.accept(visitor);
        return visitor.getOutput();
    }

    // ---------------------------------------------------------------
    // Program Structure
    // ---------------------------------------------------------------

    @Override
    public void visit(Program program) {
        this.program = program;
        program.getActorDefinitionList().accept(this);
    }

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        String actorType = node.isStage() ? "stage" : "sprite";
        emitNoSpace(actorType + " " + escapeIdentifier(node.getIdent().getName()));
        newLine();
        newLine();

        node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);

        currentActor = null;
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        for (ProcedureDefinition proc : node.getList()) {
            proc.accept(this);
            newLine();
        }
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            script.accept(this);
            newLine();
        }
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return; // Skip loose blocks
        }
        inScript = true;
        script.getEvent().accept(this);
        inScript = false;
    }

    @Override
    public void visit(StmtList node) {
        for (var stmt : node.getStmts()) {
            appendIndentation();
            stmt.accept(this);
        }
    }

    // ---------------------------------------------------------------
    // Event Handlers (Hat Blocks)
    // ---------------------------------------------------------------

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("onflag ");
        emitBlock(greenFlag);
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        emitNoSpace("onkey ");
        Key key = keyPressed.getKey();
        if (key.getKey() instanceof NumberLiteral num) {
            emitNoSpace("\"" + getKeyName((int) num.getValue()) + "\" ");
        } else {
            key.getKey().accept(this);
        }
        emitBlock(keyPressed);
    }

    @Override
    public void visit(SpriteClicked spriteClicked) {
        emitNoSpace("onclick ");
        emitBlock(spriteClicked);
    }

    @Override
    public void visit(StageClicked stageClicked) {
        emitNoSpace("onclick ");
        emitBlock(stageClicked);
    }

    @Override
    public void visit(BackdropSwitchTo backdrop) {
        emitNoSpace("onbackdrop ");
        backdrop.getBackdrop().accept(this);
        emitNoSpace(" ");
        emitBlock(backdrop);
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitNoSpace("on ");
        Message message = receptionOfMessage.getMsg();
        if (message.getMessage() instanceof StringLiteral literal) {
            emitNoSpace("\"" + escapeString(literal.getText()) + "\" ");
        } else {
            message.getMessage().accept(this);
        }
        emitBlock(receptionOfMessage);
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitNoSpace("onclone ");
        emitBlock(startedAsClone);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        String attr = getAttributeName(node.getAttribute());
        emitNoSpace("on" + attr + " > ");
        node.getValue().accept(this);
        emitNoSpace(" ");
        emitBlock(node);
    }

    @Override
    public void visit(Never never) {
        // Loose blocks - skip
    }

    // ---------------------------------------------------------------
    // Control Structures
    // ---------------------------------------------------------------

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        emitNoSpace("if ");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        ifThenStmt.getThenStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        emitNoSpace("if ");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        ifElseStmt.getThenStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("} else {");
        newLine();
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("forever {");
        newLine();
        beginIndentation();
        repeatForeverStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitNoSpace("repeat ");
        repeatTimesStmt.getTimes().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        repeatTimesStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitNoSpace("until ");
        untilStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        untilStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(WaitSeconds node) {
        emitNoSpace("wait ");
        node.getSeconds().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(WaitUntil node) {
        emitNoSpace("wait_until ");
        node.getUntil().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Motion Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(MoveSteps node) {
        emitNoSpace("move ");
        node.getSteps().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(TurnLeft node) {
        emitNoSpace("turn_left ");
        node.getDegrees().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(TurnRight node) {
        emitNoSpace("turn_right ");
        node.getDegrees().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GoToPos node) {
        emitNoSpace("goto ");
        visitPosition(node.getPosition());
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GoToPosXY node) {
        emitNoSpace("goto ");
        node.getX().accept(this);
        emitNoSpace(", ");
        node.getY().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GlideSecsTo node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(" to ");
        visitPosition(node.getPosition());
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(" to ");
        node.getX().accept(this);
        emitNoSpace(", ");
        node.getY().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(PointInDirection node) {
        emitNoSpace("point_in_direction ");
        node.getDirection().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(PointTowards node) {
        emitNoSpace("point_towards ");
        visitPosition(node.getPosition());
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeXBy node) {
        emitNoSpace("change_x ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeYBy node) {
        emitNoSpace("change_y ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetXTo node) {
        emitNoSpace("set_x ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetYTo node) {
        emitNoSpace("set_y ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        emitNoSpace("if_on_edge_bounce;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Looks Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(Say node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SayForSecs node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        emitNoSpace(" for ");
        node.getSecs().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Think node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ThinkForSecs node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(" for ");
        node.getSecs().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        emitNoSpace("switch_costume ");
        node.getCostumeChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(NextCostume node) {
        emitNoSpace("next_costume;");
        newLine();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        emitNoSpace("switch_backdrop ");
        switch (node.getElementChoice()) {
            case Next ignored -> emitNoSpace("\"next backdrop\"");
            case Prev ignored -> emitNoSpace("\"previous backdrop\"");
            case Random ignored -> emitNoSpace("\"random backdrop\"");
            default -> node.getElementChoice().accept(this);
        }
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        emitNoSpace("change_size ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetSizeTo node) {
        emitNoSpace("set_size ");
        node.getPercent().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Show node) {
        emitNoSpace("show;");
        newLine();
    }

    @Override
    public void visit(Hide node) {
        emitNoSpace("hide;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Sound Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(PlaySoundUntilDone node) {
        emitNoSpace("play_sound_until_done ");
        node.getElementChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(StartSound node) {
        emitNoSpace("start_sound ");
        node.getElementChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(StopAllSounds node) {
        emitNoSpace("stop_all_sounds;");
        newLine();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        emitNoSpace("change_volume ");
        node.getVolumeValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetVolumeTo node) {
        emitNoSpace("set_volume ");
        node.getVolumeValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(SetVariableTo node) {
        node.getIdentifier().accept(this);
        emitNoSpace(" = ");
        node.getExpr().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        node.getIdentifier().accept(this);
        emitNoSpace(" += ");
        node.getExpr().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Lists
    // ---------------------------------------------------------------

    @Override
    public void visit(AddTo node) {
        emitNoSpace("add ");
        node.getString().accept(this);
        emitNoSpace(" to ");
        node.getIdentifier().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(DeleteOf node) {
        emitNoSpace("delete ");
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getNum().accept(this);
        emitNoSpace("];");
        newLine();
    }

    @Override
    public void visit(DeleteAllOf node) {
        emitNoSpace("delete_all ");
        node.getIdentifier().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(InsertAt node) {
        emitNoSpace("insert ");
        node.getString().accept(this);
        emitNoSpace(" at ");
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getIndex().accept(this);
        emitNoSpace("];");
        newLine();
    }

    @Override
    public void visit(ReplaceItem node) {
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getIndex().accept(this);
        emitNoSpace("] = ");
        node.getString().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Events / Messaging
    // ---------------------------------------------------------------

    @Override
    public void visit(Broadcast node) {
        emitNoSpace("broadcast ");
        node.getMessage().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        emitNoSpace("broadcast ");
        node.getMessage().accept(this);
        emitNoSpace(" and_wait;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Clone Control
    // ---------------------------------------------------------------

    @Override
    public void visit(CreateCloneOf node) {
        emitNoSpace("create_clone ");
        node.getCloneChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Myself node) {
        emitNoSpace("myself");
    }

    @Override
    public void visit(WithCloneExpr node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(DeleteClone node) {
        emitNoSpace("delete_this_clone;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Termination
    // ---------------------------------------------------------------

    @Override
    public void visit(StopAll node) {
        emitNoSpace("stop_all;");
        newLine();
    }

    @Override
    public void visit(StopThisScript node) {
        emitNoSpace("stop_this_script;");
        newLine();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        emitNoSpace("stop_other_scripts;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Custom Blocks (Procedures)
    // ---------------------------------------------------------------

    @Override
    public void visit(ProcedureDefinition node) {
        String procName = getProcedureName(node);
        emitNoSpace("proc " + escapeIdentifier(procName));

        List<ParameterDefinition> params = node.getParameterDefinitionList().getParameterDefinitions();
        if (!params.isEmpty()) {
            emitNoSpace(" ");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    emitNoSpace(", ");
                }
                ParameterDefinition param = params.get(i);
                emitNoSpace(escapeIdentifier(param.getIdent().getName()));
            }
        }
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        inScript = true;
        node.getStmtList().accept(this);
        inScript = false;
        endIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(CallStmt node) {
        String procName = getProcedureCallName(node);
        emitNoSpace(escapeIdentifier(procName));
        if (!node.getExpressions().getExpressions().isEmpty()) {
            emitNoSpace(" ");
            List<Expression> args = node.getExpressions().getExpressions();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    emitNoSpace(", ");
                }
                args.get(i).accept(this);
            }
        }
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Expressions - Numbers
    // ---------------------------------------------------------------

    @Override
    public void visit(NumberLiteral node) {
        double value = node.getValue();
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            emitNoSpace(String.valueOf((long) value));
        } else {
            emitNoSpace(String.valueOf(value));
        }
    }

    @Override
    public void visit(Add node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" + ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" - ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" * ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" / ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mod node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" % ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Round node) {
        emitNoSpace("round ");
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(PickRandom node) {
        emitNoSpace("pick_random ");
        node.getOperand1().accept(this);
        emitNoSpace(" to ");
        node.getOperand2().accept(this);
    }

    @Override
    public void visit(Timer node) {
        emitNoSpace("timer");
    }

    @Override
    public void visit(MouseX node) {
        emitNoSpace("mouse_x");
    }

    @Override
    public void visit(MouseY node) {
        emitNoSpace("mouse_y");
    }

    @Override
    public void visit(PositionX node) {
        emitNoSpace("x_position");
    }

    @Override
    public void visit(PositionY node) {
        emitNoSpace("y_position");
    }

    @Override
    public void visit(Direction node) {
        emitNoSpace("direction");
    }

    @Override
    public void visit(Size node) {
        emitNoSpace("size");
    }

    @Override
    public void visit(Volume node) {
        emitNoSpace("volume");
    }

    @Override
    public void visit(Loudness node) {
        emitNoSpace("loudness");
    }

    // ---------------------------------------------------------------
    // Expressions - Strings
    // ---------------------------------------------------------------

    @Override
    public void visit(StringLiteral node) {
        emitNoSpace("\"" + escapeString(node.getText()) + "\"");
    }

    @Override
    public void visit(Join node) {
        emitNoSpace("join ");
        node.getOperand1().accept(this);
        emitNoSpace(", ");
        node.getOperand2().accept(this);
    }

    @Override
    public void visit(Answer node) {
        emitNoSpace("answer");
    }

    @Override
    public void visit(Username node) {
        emitNoSpace("username");
    }

    // ---------------------------------------------------------------
    // Expressions - Boolean
    // ---------------------------------------------------------------

    @Override
    public void visit(And node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" and ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Or node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" or ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Not node) {
        emitNoSpace("not ");
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(BiggerThan node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" > ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" < ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" == ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Touching node) {
        emitNoSpace("touching ");
        node.getTouchable().accept(this);
    }

    @Override
    public void visit(IsMouseDown node) {
        emitNoSpace("mouse_down");
    }

    @Override
    public void visit(IsKeyPressed node) {
        emitNoSpace("key_pressed ");
        if (node.getKey().getKey() instanceof NumberLiteral num) {
            emitNoSpace("\"" + getKeyName((int) num.getValue()) + "\"");
        } else {
            node.getKey().getKey().accept(this);
        }
    }

    // ---------------------------------------------------------------
    // Identifiers and Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(Variable node) {
        emitNoSpace(escapeIdentifier(node.getName().getName()));
    }

    @Override
    public void visit(ScratchList node) {
        emitNoSpace(escapeIdentifier(node.getName().getName()));
    }

    @Override
    public void visit(Parameter node) {
        emitNoSpace("$" + escapeIdentifier(node.getName().getName()));
    }

    @Override
    public void visit(StrId node) {
        emitNoSpace("\"" + escapeString(node.getName()) + "\"");
    }

    @Override
    public void visit(Qualified node) {
        emitNoSpace("[");
        node.getFirst().accept(this);
        emitNoSpace(" v] of ");
        node.getSecond().accept(this);
    }

    // ---------------------------------------------------------------
    // Touchables
    // ---------------------------------------------------------------

    @Override
    public void visit(SpriteTouchable node) {
        StringExprNameVisitor nameVisitor = new StringExprNameVisitor();
        node.getStringExpr().accept(nameVisitor);
        emitNoSpace("\"" + escapeString(nameVisitor.getResult()) + "\"");
    }

    @Override
    public void visit(Edge node) {
        emitNoSpace("\"edge\"");
    }

    @Override
    public void visit(MousePointer node) {
        emitNoSpace("\"mouse-pointer\"");
    }

    // ---------------------------------------------------------------
    // Element Choices
    // ---------------------------------------------------------------

    @Override
    public void visit(WithExpr node) {
        node.getExpression().accept(this);
    }

    // ---------------------------------------------------------------
    // Colors
    // ---------------------------------------------------------------

    @Override
    public void visit(ColorLiteral node) {
        emitNoSpace(String.format("#%02x%02x%02x",
                (int) node.getRed(), (int) node.getGreen(), (int) node.getBlue()));
    }

    // ---------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------

    private void emitBlock(Event event) {
        Script parentScript = AstNodeUtil.findParent(event, Script.class);
        if (parentScript != null) {
            emitNoSpace("{");
            newLine();
            beginIndentation();
            parentScript.getStmtList().accept(this);
            endIndentation();
            emitNoSpace("}");
            newLine();
        }
    }

    private void visitPosition(Position position) {
        if (position instanceof MousePos) {
            emitNoSpace("\"mouse-pointer\"");
        } else if (position instanceof RandomPos) {
            emitNoSpace("\"random position\"");
        } else {
            position.accept(this);
        }
    }

    private String escapeIdentifier(String name) {
        // Replace spaces with underscores for Goboscript identifiers
        return name.replace(" ", "_").replace("-", "_");
    }

    private String escapeString(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String getKeyName(int keyCode) {
        return switch (keyCode) {
            case 32 -> "space";
            case 37 -> "left arrow";
            case 38 -> "up arrow";
            case 39 -> "right arrow";
            case 40 -> "down arrow";
            default -> {
                if (keyCode >= 48 && keyCode <= 57) {
                    yield String.valueOf((char) keyCode);
                } else if (keyCode >= 65 && keyCode <= 90) {
                    yield String.valueOf((char) (keyCode + 32));
                }
                yield "any";
            }
        };
    }

    private String getAttributeName(EventAttribute attribute) {
        return attribute.getTypeName();
    }

    private String getProcedureName(ProcedureDefinition node) {
        if (program != null && currentActor != null) {
            String actorName = currentActor.getIdent().getName();
            var procMap = program.getProcedureMapping().getProcedures().get(actorName);
            if (procMap != null) {
                var procInfo = procMap.get(node.getIdent());
                if (procInfo != null) {
                    // Strip parameter placeholders from name
                    return procInfo.getName().replaceAll("%[sbn]", "").trim();
                }
            }
        }
        return node.getIdent().getName();
    }

    private String getProcedureCallName(CallStmt node) {
        if (program != null && currentActor != null) {
            String actorName = currentActor.getIdent().getName();
            var procMap = program.getProcedureMapping().getProcedures().get(actorName);
            if (procMap != null) {
                var procInfo = procMap.get(node.getIdent());
                if (procInfo != null) {
                    return procInfo.getName().replaceAll("%[sbn]", "").trim();
                }
            }
        }
        return node.getIdent().getName();
    }

    /**
     * Simple visitor to extract sprite/touchable names.
     */
    private static class StringExprNameVisitor implements de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor {
        private String result = "";

        public String getResult() {
            return result;
        }

        @Override
        public void visit(StringLiteral node) {
            result = node.getText();
        }

        @Override
        public void visit(Variable node) {
            result = node.getName().getName();
        }
    }
}
