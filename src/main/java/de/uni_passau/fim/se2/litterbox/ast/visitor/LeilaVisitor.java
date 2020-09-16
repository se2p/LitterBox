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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ResourceMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

import static de.uni_passau.fim.se2.litterbox.ast.visitor.LeilaVisitor.TYPE.*;

public class LeilaVisitor extends PrintVisitor {

    private final boolean nonDet; // indicates whether attributes should be initialized or not
    private final boolean onNever;
    private boolean emitAttributeType = false;
    private int skippedDeclarations = 0;
    private final boolean noCast = false;
    private boolean showHideVar = false;
    private boolean methodCall = false;
    private String currentActor = null;
    private Program program = null;
    private Stack<TYPE> expectedTypes = new Stack<>();

    enum TYPE {
        INTEGER, FLOAT, ORIGINAL
    }

    private enum STDVAR {
        X, Y, VOLUME, TEMPO, VISIBLE, DRAGGABLE, SIZE, DIRECTION, ROTATIONSTYLE, LAYERORDER, VIDEOTRANSPARENCY,
        VIDEOSTATE;

        public static boolean contains(String varname) {
            for (STDVAR value : STDVAR.values()) {
                if (value.name().toLowerCase().equals(varname.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Creates a new LeILa visitor that prints the LeILa equivalent of the programs to the {@code printStream}.
     *
     * @param printStream The stream to which the LeILa output will be printed.
     * @param nonDet Indicates whether attributes should be initialized or not.
     * @param onNever Indicates whether "Never" events are printed or not.
     */
    public LeilaVisitor(PrintStream printStream, boolean nonDet, boolean onNever) {
        super(printStream);
        this.nonDet = nonDet;
        this.onNever = onNever;
    }

    private void endExpectation() {
        expectedTypes.pop();
    }

    private void expectInteger() {
        expectedTypes.push(INTEGER);
    }

    private void expectOriginal() {
        expectedTypes.push(ORIGINAL);
    }

    private void expectFloat() {
        expectedTypes.push(FLOAT);
    }

    @Override
    public void visit(ASTNode node) {
        throw new RuntimeException("Visit method not implemented for class: " + node.getClass());
    }

    @Override
    public void visit(PenStmt stmt) {
        throw new RuntimeException("Pen statements are not supported.");
    }

    @Override
    public void visit(UnspecifiedStmt stmt) {
        throw new RuntimeException("Unspecified statements are not supported.");
    }

    @Override
    public void visit(Program program) {
        this.program = program;
        emitToken("program");
        program.getIdent().accept(this);
        newLine();

        List<ActorDefinition> definitions = program.getActorDefinitionList().getDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            definitions.get(i).accept(this);
            if (i < definitions.size() - 1) {
                newLine();
            }
        }
        this.program = null;
    }

    @Override
    public void visit(ActorDefinition def) {
        currentActor = def.getIdent().getName();
        skippedDeclarations = 0;
        newLine();
        emitToken("actor");
        def.getIdent().accept(this);
        emitToken(" is");
        def.getActorType().accept(this);
        begin();
        beginIndentation();

        emitResourceListsOf(def);

        DeclarationStmtList declarations = def.getDecls();
        List<DeclarationStmt> declarationStmtList = declarations.getDeclarationStmtList();
        int numDeclarations = declarationStmtList.size();
        if (numDeclarations > 0) {
            newLine();
        }
        for (DeclarationStmt declarationStmt : declarationStmtList) {
            declarationStmt.accept(this);
        }

        if (!nonDet) {
            SetStmtList setStmtList = def.getSetStmtList();
            List<SetStmt> stmts = setStmtList.getStmts();
            if (stmts.size() > 0 && !(skippedDeclarations == numDeclarations)) {
                newLine();
            }
            for (SetStmt stmt : stmts) {
                newLine();
                appendIndentation();
                stmt.accept(this);
            }
        }
        initialiseCostume(def);

        ProcedureDefinitionList procDefList = def.getProcedureDefinitionList();
        List<ProcedureDefinition> procDefs = procDefList.getList();
        for (ProcedureDefinition procDef : procDefs) {
            newLine();
            newLine();
            appendIndentation();
            procDef.accept(this);
        }

        ScriptList scripts = def.getScripts();
        List<Script> scriptList = scripts.getScriptList();
        for (Script script : scriptList) {
            newLine();
            newLine();
            appendIndentation();
            script.accept(this);
        }
        endIndentation();
        newLine();
        end();
        currentActor = null;
    }

    private void emitResourceListsOf(ActorDefinition def) {
        List<ImageMetadata> images = def.getActorMetadata().getCostumes().getList();
        emitResourceList(images);

        List<SoundMetadata> sounds = def.getActorMetadata().getSounds().getList();
        emitResourceList(sounds);
    }

    private void emitResourceList(List<? extends ResourceMetadata> images) {
        int i = 0;
        int size = images.size();
        if (size > 0) {
            newLine();
        }
        for (ResourceMetadata res : images) {
            appendIndentation();
            if (res instanceof SoundMetadata) {
                emitToken("sound");
            } else if (res instanceof ImageMetadata) {
                emitToken("image");
            } else {
                throw new RuntimeException("Unknown resource type: " + res.getClass());
            }
            emitStrId(res.getName());
            emitNoSpace(" ");
            emitString(res.getMd5ext());
            i++;
            if (i < size) {
                newLine();
            }
        }
    }

    private void emitString(String str) {
        emitNoSpace("\"" + str + "\"");
    }

    private void initialiseCostume(ActorDefinition def) {
        newLine();
        newLine();
        appendIndentation();
        emitToken("script on bootstrap do begin");
        beginIndentation();
        newLine();
        appendIndentation();
        // using 'changeActiveGraphicTo' instead of 'changeCostumeTo' since also available for the stage
        emitNoSpace("changeActiveGraphicTo(");
        ActorMetadata metadata = def.getActorMetadata();
        int currentCostume = metadata.getCurrentCostume();
        String currentCostumeName = metadata.getCostumes().getList().get(currentCostume).getName();
        emitString(currentCostumeName);
        closeParentheses();
        newLine();
        endIndentation();
        appendIndentation();
        emitToken("end");
    }

    @Override
    public void visit(Script script) {
        if (!(script.getEvent() instanceof Never) || onNever) {
            emitToken("script");
            emitToken("on");
            script.getEvent().accept(this);
            emitNoSpace(" do");
            script.getStmtList().accept(this);
        }
    }

    @Override
    public void visit(BackdropSwitchTo backdropSwitchTo) {
        emitToken("backdrop");
        emitToken("switched");
        emitToken("to");
        backdropSwitchTo.getBackdrop().accept(this);
    }

    @Override
    public void visit(Clicked clicked) {
        emitNoSpace("message \"SPRITE_CLICK\" ()");
    }

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("startup");
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        emitNoSpace("message \"KEY_");
        expectInteger();
        keyPressed.getKey().getKey().accept(this);
        endExpectation();
        emitNoSpace("_PRESSED\" ()");
    }

    @Override
    public void visit(Key key) {
        expectInteger();
        key.getKey().accept(this);
        endExpectation();
    }

    @Override
    public void visit(Never never) {
        emitNoSpace("never");
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitToken("message");
        receptionOfMessage.getMsg().accept(this);
    }

    @Override
    public void visit(Message message) {
        message.getMessage().accept(this);
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitNoSpace("started as clone");
    }

    @Override
    public void visit(AttributeAboveValue attributeAboveValue) {
        emitToken("value of");
        attributeAboveValue.getAttribute().accept(this);
        emitToken(" above");
        expectOriginal();
        attributeAboveValue.getValue().accept(this);
        endExpectation();
    }

    @Override
    public void visit(StmtList stmtList) {
        begin();
        beginIndentation();
        for (Stmt stmt : stmtList.getStmts()) {
            newLine();
            appendIndentation();
            stmt.accept(this);
        }
        endIndentation();
        end();
    }

    @Override
    public void visit(PlaySoundUntilDone playSoundUntilDone) {
        emitNoSpace("playUntilDone(");
        playSoundUntilDone.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(StartSound startSound) {
        emitNoSpace("startSound(");
        startSound.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ClearSoundEffects clearSoundEffects) {
        emitNoSpace("clearSoundEffects()");
    }

    @Override
    public void visit(StopAllSounds stopAllSounds) {
        emitNoSpace("stopAllSounds()");
    }

    @Override
    public void visit(AskAndWait askAndWait) {
        emitNoSpace("askAndWait(");
        askAndWait.getQuestion().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(SwitchBackdrop switchBackdrop) {
        if (switchBackdrop.getElementChoice() instanceof Next) {
            emitNoSpace("switchBackdropToNext()");
        } else if (switchBackdrop.getElementChoice() instanceof Prev) {
            emitNoSpace("switchBackdropToPrev()");
        } else if (switchBackdrop.getElementChoice() instanceof Random) {
            emitNoSpace("switchBackdropToRandom()");
        } else {
            emitNoSpace("switchBackdropToId(\"");
            switchBackdrop.getElementChoice().accept(this);
            emitNoSpace("\"");
            closeParentheses();
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait switchBackdropAndWait) {
        emitNoSpace("switchBackdropToAndWait(\"");
        switchBackdropAndWait.getElementChoice().accept(this);
        emitNoSpace("\"");
        closeParentheses();
    }

    @Override
    public void visit(ClearGraphicEffects clearGraphicEffects) {
        emitNoSpace("clearGraphicEffects()");
    }

    @Override
    public void visit(ShowVariable showVariable) {
        emitNoSpace("showVariable(\"");
        showHideVar = true;
        showVariable.getIdentifier().accept(this);
        showHideVar = false;
        emitNoSpace("\"");
        closeParentheses();
    }

    @Override
    public void visit(HideVariable hideVariable) {
        emitNoSpace("hideVariable(\"");
        showHideVar = true;
        hideVariable.getIdentifier().accept(this);
        showHideVar = false;
        emitNoSpace("\"");
        closeParentheses();
    }

    @Override
    public void visit(Show show) {
        emitNoSpace("show()");
    }

    @Override
    public void visit(Hide hide) {
        emitNoSpace("hide()");
    }

    @Override
    public void visit(SayForSecs sayForSecs) {
        emitNoSpace("sayTextFor(");
        sayForSecs.getString().accept(this);
        comma();
        expectInteger();
        sayForSecs.getSecs().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Say say) {
        emitNoSpace("sayText(");
        say.getString().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ThinkForSecs thinkForSecs) {
        emitNoSpace("thinkTextFor(");
        thinkForSecs.getThought().accept(this);
        emitToken(",");
        expectInteger();
        thinkForSecs.getSecs().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Think think) {
        emitNoSpace("thinkText(");
        think.getThought().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(SwitchCostumeTo switchCostumeTo) {
        emitNoSpace("changeCostumeTo(");
        if (switchCostumeTo.getCostumeChoice() instanceof WithExpr) {
            final Expression expr = ((WithExpr) switchCostumeTo.getCostumeChoice()).getExpression();
            if (expr instanceof LocalIdentifier) {
                emitNoSpace("\"");
                emitNoSpace(((LocalIdentifier) expr).getName());
                emitNoSpace("\"");
            }
        } else {
            switchCostumeTo.getCostumeChoice().accept(this);
        }
        closeParentheses();
    }

    @Override
    public void visit(ChangeSizeBy changeSizeBy) {
        emitToken("define size as size +");
        expectInteger();
        changeSizeBy.getNum().accept(this);
        endExpectation();
    }

    @Override
    public void visit(SetSizeTo setSizeTo) {
        emitToken("define size as");
        expectInteger();
        setSizeTo.getPercent().accept(this);
        endExpectation();
    }

    @Override
    public void visit(ChangeLayerBy changeLayerBy) {
        emitToken("changeLayerBy(");
        expectInteger();
        changeLayerBy.getNum().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(GoToLayer goToLayer) {
        LayerChoice layerChoice = goToLayer.getLayerChoice();
        if (LayerChoice.BACK.equals(layerChoice)) {
            emitToken("goToBackLayer()");
        } else {
            emitToken("goToFrontLayer()");
        }
    }

    @Override
    public void visit(Next next) {
        emitNoSpace("next");
    }

    @Override
    public void visit(Prev prev) {
        emitNoSpace("prev");
    }

    @Override
    public void visit(Random random) {
        emitNoSpace("random");
    }

    @Override
    public void visit(WithExpr withExpr) {
        expectOriginal();
        withExpr.getExpression().accept(this);
        endExpectation();
    }

    @Override
    public void visit(MoveSteps moveSteps) {
        emitNoSpace("moveSteps(");
        expectInteger();
        moveSteps.getSteps().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(TurnRight turnRight) {
        emitNoSpace("turnRight(");
        expectInteger();
        turnRight.getDegrees().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(TurnLeft turnLeft) {
        emitNoSpace("turnLeft(");
        expectInteger();
        turnLeft.getDegrees().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(GoToPos goToPos) {
        Position position = goToPos.getPosition();
        if (position instanceof RandomPos) {
            emitToken("goToRandomPosition()");
        } else if (position instanceof FromExpression) {
            emitToken("declare o as actor");
            newLine();
            appendIndentation();
            emitToken("define o as");
            position.accept(this);
            newLine();
            appendIndentation();
            emitToken("goToSprite(o)");
        } else if (position instanceof MousePos) {
            emitToken("goTo(");
            position.accept(this);
            closeParentheses();
        }
    }

    @Override
    public void visit(FromExpression fromExpression) {
        emitActorExpression(fromExpression.getStringExpr());
    }

    @Override
    public void visit(RandomPos randomPos) {
        emitNoSpace("randomIntegerBetween(0-240, 240), randomIntegerBetween(0-180, 180)");
    }

    @Override
    public void visit(MousePos mousePos) {
        emitNoSpace("getMouseX(), getMouseY()");
    }

    @Override
    public void visit(GlideSecsTo glideSecsTo) {
        Position position = glideSecsTo.getPosition();
        if (position instanceof RandomPos) {
            emitNoSpace("glideSecondsToRandomPos(");
            expectInteger();
            glideSecsTo.getSecs().accept(this);
            endExpectation();
            closeParentheses();
        } else if (position instanceof FromExpression) {
            emitToken("declare o as actor");
            newLine();
            appendIndentation();
            emitToken("define o as");
            position.accept(this);
            newLine();
            appendIndentation();
            emitNoSpace("glideSecsToSprite(");
            expectInteger();
            glideSecsTo.getSecs().accept(this);
            endExpectation();
            comma();
            emitNoSpace("o)");
        } else if (position instanceof MousePos) {
            emitToken("glideSecondsTo(");
            expectInteger();
            glideSecsTo.getSecs().accept(this);
            endExpectation();
            comma();
            position.accept(this);
            closeParentheses();
        }
    }

    @Override
    public void visit(PointInDirection pointInDirection) {
        emitNoSpace("pointInDirection(");
        expectInteger();
        pointInDirection.getDirection().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(PointTowards pointTowards) {
        if (pointTowards.getPosition() instanceof MousePos) {
            emitNoSpace("pointTowardsPos(");
            pointTowards.getPosition().accept(this);
            closeParentheses();
        } else if (pointTowards.getPosition() instanceof FromExpression) {
            // A sprite
            emitNoSpace("pointTowards(");
            StringExpr strExpr = ((FromExpression)pointTowards.getPosition()).getStringExpr();
            emitActorExpression(strExpr);
            closeParentheses();
        } else if (pointTowards.getPosition() instanceof RandomPos) {
            emitNoSpace("pointTowardsPos(");
            pointTowards.getPosition().accept(this);
            closeParentheses();
        }
    }

    private void emitActorExpression(Expression expr) {
        emitNoSpace("locate actor ");
        expectOriginal();
        if (expr instanceof LocalIdentifier) {
            emitNoSpace("\"" + ((LocalIdentifier) expr).getName() + "\"");
        } else if (expr instanceof AsString) {
            Expression op1 = ((AsString) expr).getOperand1();
            if (op1 instanceof LocalIdentifier) {
                emitNoSpace("\"" + ((LocalIdentifier) op1).getName() + "\"");
            } else {
                op1.accept(this);
            }
        } else {
            emitToken(expr.getClass().getSimpleName());
        }
        endExpectation();
    }

    @Override
    public void visit(ChangeXBy changeXBy) {
        emitNoSpace("changeXBy(");
        expectInteger();
        changeXBy.getNum().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(ChangeYBy changeYBy) {
        emitToken("define y as y +");
        expectInteger();
        changeYBy.getNum().accept(this);
        endExpectation();
    }

    @Override
    public void visit(SetXTo setXTo) {
        emitToken("define x as x +");
        expectInteger();
        setXTo.getNum().accept(this);
        endExpectation();
    }

    @Override
    public void visit(SetYTo setYTo) {
        emitToken("define y as");
        expectInteger();
        setYTo.getNum().accept(this);
        endExpectation();
    }

    @Override
    public void visit(IfOnEdgeBounce ifOnEdgeBounce) {
        emitToken("ifOnEdgeBounce()");
    }

    @Override
    public void visit(DeleteAllOf deleteAllOf) {
        emitToken("delete all of");
        deleteAllOf.getIdentifier().accept(this);
    }

    @Override
    public void visit(DeleteOf deleteOf) {
        emitToken("delete");
        expectInteger();
        deleteOf.getNum().accept(this);
        endExpectation();
        emitToken(" of");
        deleteOf.getIdentifier().accept(this);
    }

    @Override
    public void visit(AddTo addTo) {
        emitToken("add");
        addTo.getString().accept(this);
        emitToken(" to");
        addTo.getIdentifier().accept(this);
    }

    @Override
    public void visit(InsertAt insertAt) {
        emitToken("insert");
        insertAt.getString().accept(this);
        emitToken(" at");
        expectInteger();
        insertAt.getIndex().accept(this);
        endExpectation();
        emitToken(" of");
        insertAt.getIdentifier().accept(this);
    }

    @Override
    public void visit(ReplaceItem replaceItem) {
        emitToken("replace item");
        expectInteger();
        replaceItem.getIndex().accept(this);
        endExpectation();
        emitToken(" of");
        replaceItem.getIdentifier().accept(this);
        emitToken(" by");
        replaceItem.getString().accept(this);
    }

    @Override
    public void visit(WaitSeconds waitSeconds) {
        emitToken("wait");
        expectInteger();
        waitSeconds.getSeconds().accept(this);
        endExpectation();
        emitToken(" seconds");
    }

    @Override
    public void visit(WaitUntil waitUntil) {
        emitToken("wait until");
        waitUntil.getUntil().accept(this);
    }

    @Override
    public void visit(StopOtherScriptsInSprite stopOtherScriptsInSprite) {
        emitToken("stop other scripts in actor");
    }

    @Override
    public void visit(CreateCloneOf createCloneOf) {
        emitToken("create clone of");
        createCloneOf.getStringExpr().accept(this);
    }

    @Override
    public void visit(Broadcast broadcast) {
        emitToken("broadcast");
        broadcast.getMessage().accept(this);
    }

    @Override
    public void visit(BroadcastAndWait broadcastAndWait) {
        emitToken("broadcast");
        broadcastAndWait.getMessage().accept(this);
        emitToken(" and wait");
    }

    @Override
    public void visit(ResetTimer resetTimer) {
        emitToken("reset timer");
    }

    @Override
    public void visit(ChangeVariableBy changeVariableBy) {
        define();
        Identifier identifier = changeVariableBy.getIdentifier();
        identifier.accept(this);
        as();
        identifier.accept(this);
        emitToken(" +");
        expectOriginal();
        changeVariableBy.getExpr().accept(this);
        endExpectation();
    }

    @Override
    public void visit(ExpressionStmt expressionStmt) {
        emitToken("evaluate");
        expectOriginal();
        expressionStmt.getExpression().accept(this);
        endExpectation();
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (!methodCall) {
            emitNoSpace("[");
        }
        List<Expression> expressions = expressionList.getExpressions();
        if (expressions.size() > 0) {
            expectOriginal();
            for (int i = 0; i < expressions.size() - 1; i++) {
                expressions.get(i).accept(this);
                comma();
            }
            expressions.get(expressions.size() - 1).accept(this);
            endExpectation();
        }
        if (!methodCall) {
            emitNoSpace("]");
        }
    }

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        emitNoSpace("if (");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(") then");
        ifThenStmt.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        emitNoSpace("if (");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(") then");
        ifElseStmt.getStmtList().accept(this);

        emitNoSpace("else");
        ifElseStmt.getElseStmts().accept(this);
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitToken("until");
        untilStmt.getBoolExpr().accept(this);
        emitNoSpace(" repeat");
        untilStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitToken("repeat");
        expectInteger();
        repeatTimesStmt.getTimes().accept(this);
        endExpectation();
        emitNoSpace(" times");
        repeatTimesStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("repeat forever");
        repeatForeverStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(ProcedureDefinition procedureDefinition) {
        emitToken("define");
        Map<LocalIdentifier, ProcedureInfo> proceduresOfSprite = getProceduresOfCurrentSprite();
        String procedureName = proceduresOfSprite.get(procedureDefinition.getIdent()).getName();

        if (isUniqueProcedureName(procedureName, proceduresOfSprite)) {
            emitToken(prepareName(procedureName));
        } else {
            String identifier = procedureDefinition.getIdent().getName();
            emitToken(generateProcedureName(procedureName, identifier));
        }
        procedureDefinition.getParameterDefinitionList().accept(this);
        procedureDefinition.getStmtList().accept(this);
    }

    @Override
    public void visit(CallStmt callStmt) {
        String procedureName = callStmt.getIdent().getName();
        Map<LocalIdentifier, ProcedureInfo> procedures = getProceduresOfCurrentSprite();
        if (isUniqueProcedureName(procedureName, procedures)) {
            emitNoSpace(prepareName(procedureName));
        } else {
            Set<Map.Entry<LocalIdentifier, ProcedureInfo>> entries = procedures.entrySet();
            Map.Entry<LocalIdentifier, ProcedureInfo> procedureInfo = entries
                    .stream()
                    .filter(entry -> entry.getValue().getName().equals(procedureName))
                    .findFirst()
                    .get();
            emitNoSpace(generateProcedureName(procedureName, procedureInfo.getKey().getName()));
        }
        openParentheses();
        methodCall = true;
        callStmt.getExpressions().accept(this);
        methodCall = false;
        closeParentheses();
    }

    private String prepareName(String procedureName) {
        return procedureName.trim().replace(" ", "").replace("\"", "").replace("%s", "").replace("%b", "");
    }

    private String generateProcedureName(String procedureName, String identifier) {
        procedureName = prepareName(procedureName);
        procedureName = procedureName + "_" + identifier; // FIXME handle unsupported chars in identifiers
        return procedureName;
    }

    private Map<LocalIdentifier, ProcedureInfo> getProceduresOfCurrentSprite() {
        ProcedureDefinitionNameMapping procedureMapping = program.getProcedureMapping();
        Map<String, Map<LocalIdentifier, ProcedureInfo>> procedures = procedureMapping.getProcedures();
        return procedures.get(currentActor);
    }

    private boolean isUniqueProcedureName(String procedureName, Map<LocalIdentifier, ProcedureInfo> procedures) {
        Collection<ProcedureInfo> procedureInfos = procedures.values();
        int proceduresWithGivenName = 0;
        for (ProcedureInfo procedureInfo : procedureInfos) {
            String name = procedureInfo.getName();
            if (name.equals(procedureName)) {
                proceduresWithGivenName++;
            }
        }
        return proceduresWithGivenName == 1;
    }

    @Override
    public void visit(Parameter param) {
        param.getName().accept(this);
    }

    @Override
    public void visit(StopAll stopAll) {
        emitToken("stop all");
    }

    @Override
    public void visit(StopThisScript stopThisScript) {
        emitToken("stop this script");
    }

    @Override
    public void visit(DeleteClone deleteClone) {
        emitNoSpace("delete this clone");
    }

    @Override
    public void visit(ParameterDefinitionList parameterDefinitionList) {
        openParentheses();
        List<ParameterDefinition> parameterDefinitions = parameterDefinitionList.getParameterDefinitions();
        if (parameterDefinitions.size() > 0) {
            for (int i = 0; i < parameterDefinitions.size() - 1; i++) {
                parameterDefinitions.get(i).accept(this);
                comma();
            }
            parameterDefinitions.get(parameterDefinitions.size() - 1).accept(this);
        }
        closeParentheses();
    }

    private void comma() {
        emitToken(",");
    }

    @Override
    public void visit(ParameterDefinition parameterDefinition) {
        parameterDefinition.getIdent().accept(this);
        colon();
        parameterDefinition.getType().accept(this);
    }

    private void colon() {
        emitToken(":");
    }

    @Override
    public void visit(DeclarationAttributeAsTypeStmt declarationAttributeAsTypeStmt) {

        StringExpr stringExpr = declarationAttributeAsTypeStmt.getStringExpr();
        if (stringExpr instanceof StringLiteral) {
            String text = ((StringLiteral) stringExpr).getText();
            if (STDVAR.contains(text)) {
                skippedDeclarations++;
                return;
            }
        }
        // TODO these lines seem to be unreachable
        newLine();
        appendIndentation();
        declare();
        emitAttributeType = true;
        declarationAttributeAsTypeStmt.getStringExpr().accept(this);
        emitAttributeType = false;
        as();
        declarationAttributeAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(BooleanType booleanType) {
        emitNoSpace("boolean");
    }

    @Override
    public void visit(ListType listType) {
        emitNoSpace("list of string");
    }

    @Override
    public void visit(NumberType numberType) {
        emitNoSpace("integer");
    }

    @Override
    public void visit(StringType stringType) {
        emitNoSpace("string");
    }

    @Override
    public void visit(AsString asString) {
        if (noCast) {
            asString.getOperand1().accept(this);
        } else {
            expectOriginal();
            emitToken("cast");
            asString.getOperand1().accept(this);
            emitNoSpace(" to string");
            endExpectation();
        }
    }

    @Override
    public void visit(AsBool asBool) {
        expectOriginal();
        emitToken("cast");
        asBool.getOperand1().accept(this);
        emitNoSpace(" to boolean");
        endExpectation();
    }

    @Override
    public void visit(Join join) {
        emitToken("join");
        join.getOperand1().accept(this);
        emitNoSpace(" ");
        join.getOperand2().accept(this);
    }

    @Override
    public void visit(LetterOf letterOf) {
        emitToken("letter");
        expectInteger();
        letterOf.getNum().accept(this);
        endExpectation();
        of();
        letterOf.getStringExpr().accept(this);
    }

    @Override
    public void visit(Username username) {
        emitNoSpace("username()");
    }

    @Override
    public void visit(ItemOfVariable itemOfVariable) {
        emitToken("item");
        expectInteger();
        itemOfVariable.getNum().accept(this);
        endExpectation();
        of();
        itemOfVariable.getIdentifier().accept(this);
    }

    @Override
    public void visit(UnspecifiedStringExpr unspecifiedStringExpr) {
        emitNoSpace("?string");
    }

    @Override
    public void visit(DeclarationIdentAsTypeStmt declarationIdentAsTypeStmt) {
        newLine();
        appendIndentation();
        declare();
        emitAttributeType = true;
        declarationIdentAsTypeStmt.getIdent().accept(this);
        emitAttributeType = false;
        as();
        declarationIdentAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(DeclarationAttributeOfIdentAsTypeStmt declarationAttributeOfIdentAsTypeStmt) {
        newLine();
        appendIndentation();
        declare();
        emitToken("attribute");
        emitAttributeType = true;
        declarationAttributeOfIdentAsTypeStmt.getStringExpr().accept(this);
        emitAttributeType = false;
        of();
        declarationAttributeOfIdentAsTypeStmt.getIdent().accept(this);
        as();
        declarationAttributeOfIdentAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(SetAttributeTo setAttributeTo) {
        define();
        emitAttributeType = true;
        setAttributeTo.getStringExpr().accept(this);
        emitAttributeType = false;
        as();
        expectOriginal();
        setAttributeTo.getExpr().accept(this);
        endExpectation();
    }

    @Override
    public void visit(SetVariableTo setVariableTo) {
        define();
        setVariableTo.getIdentifier().accept(this);
        as();
        expectOriginal();
        setVariableTo.getExpr().accept(this);
        endExpectation();
    }

    private void declare() {
        emitToken("declare");
    }

    private void define() {
        emitToken("define");
    }


    private void of() {
        emitToken(" of");
    }

    private void as() {
        emitToken(" as");
    }

    @Override
    public void visit(ActorType actorType) {
        if (actorType.equals(ActorType.STAGE)) {
            emitNoSpace("ScratchStage");
        } else if (actorType.equals(ActorType.SPRITE)) {
            emitNoSpace("ScratchSprite");
        } else {
            emitNoSpace("ScratchActor");
        }
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (!emitAttributeType) {
            emitString(stringLiteral.getText());
        } else if (stringLiteral.getText().equalsIgnoreCase(String.valueOf(STDVAR.LAYERORDER))) {
            emitNoSpace("layer");
        } else {
            emitNoSpace(stringLiteral.getText());
        }
    }

    @Override
    public void visit(GraphicEffect graphicEffect) {
        emitNoSpace(graphicEffect.getToken());
    }

    @Override
    public void visit(SoundEffect soundEffect) {
        String effect = soundEffect.getToken();
        if (effect.equals(SoundEffect.PITCH.getToken())) {
            emitNoSpace("pitch");
        } else {
            emitNoSpace("pan_left_right");
        }
    }

    @Override
    public void visit(AttributeFromFixed attributeFromFixed) {
        attributeFromFixed.getAttribute().accept(this);
    }

    @Override
    public void visit(FixedAttribute fixedAttribute) {
        emitToken(fixedAttribute.getType());
    }

    @Override
    public void visit(StrId strId) {
        String name = strId.getName();
        if (!showHideVar) {
            emitStrId(name);
        } else {
            emitNoSpace(name);
        }
    }

    private void emitStrId(String name) {
        if (name.contains("\"")) {
            throw new RuntimeException("Ids containing \" are not allowed here.");
        } else {
            if (Pattern.matches("[a-zA-Z][a-zA-Z0-9]*", name)) {
                emitNoSpace(name);
            } else {
                emitToken("strid");
                emitString(name);
            }
        }
    }

    @Override
    public void visit(BoolLiteral boolLiteral) {
        emitToken(String.valueOf(boolLiteral.getValue()));
    }

    @Override
    public void visit(Not not) {
        emitToken("not");
        openParentheses();
        not.getOperand1().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(And and) {
        openParentheses();
        and.getOperand1().accept(this);
        emitToken(" and");
        and.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Or or) {
        openParentheses();
        or.getOperand1().accept(this);
        emitToken(" or");
        or.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(BiggerThan biggerThan) {
        openParentheses();
        expectOriginal();
        biggerThan.getOperand1().accept(this);
        emitToken(" >");
        biggerThan.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(LessThan lessThan) {
        openParentheses();
        expectOriginal();
        lessThan.getOperand1().accept(this);
        emitToken(" <");
        lessThan.getOperand1().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Equals equals) {
        openParentheses();
        expectOriginal();
        equals.getOperand1().accept(this);
        emitToken(" =");
        equals.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(StringContains stringContains) {
        stringContains.getContaining().accept(this);
        emitToken(" contains");
        stringContains.getContained().accept(this);
    }

    @Override
    public void visit(Touching touching) {
        Touchable touchable = touching.getTouchable();
        if (touchable instanceof Edge) {
            emitNoSpace("touchingEdge()");
            return;
        } else if (touchable instanceof MousePointer) {
            emitNoSpace("touchingMousePointer()");
            return;
        } else if (touchable instanceof Color) {
            emitNoSpace("touchingColor(");
        } else {
            emitNoSpace("touchingObject(");
        }
        touching.getTouchable().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(MousePointer mousePointer) {
        emitNoSpace("mousepointer");
    }

    @Override
    public void visit(Edge edge) {
        emitNoSpace("edge");
    }

    @Override
    public void visit(SpriteTouchable spriteTouchable) {
        emitToken("locate actor");
        spriteTouchable.getStringExpr().accept(this);
    }

    @Override
    public void visit(ColorLiteral colorLiteral) {
        emitNoSpace("rgb(");
        emitNoSpace(String.valueOf(colorLiteral.getRed()));
        comma();
        emitNoSpace(String.valueOf(colorLiteral.getGreen()));
        comma();
        emitNoSpace(String.valueOf(colorLiteral.getBlue()));
        closeParentheses();
    }

    @Override
    public void visit(ColorTouchingColor colorTouchingColor) {
        emitNoSpace("colorIsTouchingColor(");
        colorTouchingColor.getOperand1().accept(this);
        comma();
        colorTouchingColor.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(IsKeyPressed isKeyPressed) {
        emitNoSpace("keyPressedByCode(");
        isKeyPressed.getKey().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(FromNumber fromNumber) { // TODO look up how Scratch handles this internally
        expectOriginal();
        fromNumber.getValue().accept(this);
        endExpectation();
    }

    @Override
    public void visit(IsMouseDown isMouseDown) {
        emitNoSpace("mouseDown()");
    }

    @Override
    public void visit(UnspecifiedBoolExpr unspecifiedBoolExpr) {
        emitNoSpace("?bool");
    }

    @Override
    public void visit(AsNumber asNumber) {
        emitToken("cast");
        expectOriginal(); // This should in theory not be necessary, but does not hurt and guarantees correctness here
        asNumber.getOperand1().accept(this);
        endExpectation();
        emitNoSpace(" to int"); //TODO distinguish between int and float?
        // TODO update this to integer as soon as the grammar has been updated
    }

    @Override
    public void visit(Timer timer) {
        emitNoSpace("timer");
    }

    @Override
    public void visit(DaysSince2000 daysSince2000) {
        emitNoSpace("daysSinceMillennium()");
    }

    @Override
    public void visit(Current current) {
        current.getTimeComp().accept(this);
    }

    @Override
    public void visit(TimeComp timeComp) {
        emitNoSpace("current");
        String label = timeComp.getLabel();
        if (label.equalsIgnoreCase(TimeComp.DAY_OF_WEEK.getLabel())) {
            emitNoSpace("DayOfWeek");
        } else {
            emitNoSpace(label.substring(0, 1).toUpperCase() + label.substring(1));
        }
        emitNoSpace("()");
    }

    @Override
    public void visit(DistanceTo distanceTo) {
        if (distanceTo.getPosition() instanceof MousePos) {
            emitNoSpace("distanceToMousePointer()");
        } else {
            emitToken("distanceTo("); // FIXME there is no method in BASTET that handles distances to sprites
            distanceTo.getPosition().accept(this);
            closeParentheses();
        }
    }

    @Override
    public void visit(MouseX mouseX) {
        emitNoSpace("mouseX()");
    }

    @Override
    public void visit(MouseY mouseY) {
        emitNoSpace("mouseY()");
    }

    @Override
    public void visit(Loudness loudness) {
        emitNoSpace("loudness()");
    }

    @Override
    public void visit(LengthOfString lengthOfString) {
        emitToken("length of");
        lengthOfString.getStringExpr().accept(this);
    }

    @Override
    public void visit(LengthOfVar lengthOfVar) {
        emitToken("length of list");
        lengthOfVar.getIdentifier().accept(this);
    }

    @Override
    public void visit(IndexOf indexOf) {
        emitToken("index of");
        expectOriginal();
        indexOf.getExpr().accept(this);
        endExpectation();
        emitToken(" in");
        indexOf.getIdentifier().accept(this);
    }

    @Override
    public void visit(PickRandom pickRandom) { // FIXME distinguish between random float and random integer
        emitNoSpace("randomBetween(");
        pickRandom.getOperand1().accept(this);
        comma();
        pickRandom.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Round round) {
        emitToken("round");
        expectOriginal();
        round.getOperand1().accept(this);
        endExpectation();
    }

    @Override
    public void visit(NumberLiteral number) {
        TYPE expectedType = expectedTypes.peek();
        double value = number.getValue();
        switch (expectedType) {
            case ORIGINAL:
                if (isInteger(value)) {
                    emitAsLong(value);
                } else {
                    emitAsDouble(value);
                }
                break;
            case INTEGER:
                if (isInteger(value)) {
                    emitAsLong(value);
                } else {
                    throw new RuntimeException("Expected type integer but got " + value);
                }
                break;
            case FLOAT:
                emitAsDouble(value);
                break;
            default:
                throw new RuntimeException("Unknown expected type: " + expectedType);
        }
    }

    private boolean isInteger(double value) {
        return (value == Math.floor(value)) && !Double.isInfinite(value);
    }

    private void emitAsLong(double value) {
        long intValue = (long) value;
        if (value >= 0) {
            emitNoSpace(String.valueOf(intValue));
        } else {
            emitNoSpace("(0" + intValue + ")");
        }
    }

    private void emitAsDouble(double value) {
        String val = String.valueOf(value);
        if (value >= 0) {
            emitNoSpace(val);
        } else {
            emitNoSpace("(0" + val + ")");
        }
    }

    @Override
    public void visit(NumFunctOf numFunctOf) {
        NumExpr operand2 = numFunctOf.getOperand2();
        boolean expectationSet = false;
        switch (numFunctOf.getOperand1()) {
            case ABS:
                emitNoSpace("mathAbsF(");
                expectFloat(); // TODO there is also mathAbs for integers but finding out whether a NumExpr evaluates to
                // an int or float is nearly impossible
                expectationSet = true;
                break;
            case LN:
                emitNoSpace("mathLn(");
                break;
            case ACOS:
                emitNoSpace("mathAcos(");
                break;
            case ASIN:
                emitNoSpace("mathAsin(");
                break;
            case ATAN:
                emitNoSpace("mathAtan(");
                expectFloat();
                expectationSet = true;
                break;
            case CEILING:
                emitNoSpace("mathCeiling("); // FIXME expecting the default integer here is what makes BASTET parse the
                // program, but this does not make sense for the ceiling function?
                break;
            case COS:
                emitNoSpace("mathCos(");
                expectFloat();
                expectationSet = true;
                break;
            case FLOOR:
                emitNoSpace("mathFloor(");
                expectFloat();
                expectationSet = true;
                break;
            case LOG:
                emitNoSpace("mathLog(");
                break;
            case POW10:
                emitNoSpace("mathPowten(");
                break;
            case POWE:
                emitNoSpace("mathPowe(");
                break;
            case SIN:
                emitNoSpace("mathSin(");
                expectFloat();
                expectationSet = true;
                break;
            case SQRT:
                emitNoSpace("mathSqrt(");
                expectFloat();
                expectationSet = true;
                break;
            case TAN:
                emitNoSpace("mathTan(");
                break;
            case UNKNOWN:
            default:
                throw new RuntimeException("Unknown numerical function");
        }
        if (!expectationSet) {
            expectInteger();
        }
        operand2.accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Mult mult) {
        openParentheses();
        expectOriginal();
        mult.getOperand1().accept(this);
        emitToken(" *");
        mult.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Div div) {
        openParentheses();
        expectOriginal();
        div.getOperand1().accept(this);
        emitToken(" /");
        div.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Mod mod) {
        openParentheses();
        expectOriginal();
        mod.getOperand1().accept(this);
        emitToken(" mod");
        mod.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Add add) {
        openParentheses();
        expectOriginal();
        add.getOperand1().accept(this);
        emitToken(" +");
        add.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Minus minus) {
        openParentheses();
        expectOriginal();
        minus.getOperand1().accept(this);
        emitToken(" -");
        minus.getOperand2().accept(this);
        endExpectation();
        closeParentheses();
    }

    private void openParentheses() {
        emitNoSpace("(");
    }

    private void closeParentheses() {
        emitNoSpace(")");
    }

    @Override
    public void visit(UnspecifiedNumExpr unspecifiedNumExpr) {
        emitNoSpace("?number");
    }

    @Override
    public void visit(NumFunct numFunct) { // TODO use library functions
        emitNoSpace(numFunct.getFunction());
    }

    @Override
    public void visit(UnspecifiedExpression unspecifiedExpression) {
        emitNoSpace("?expr");
    }

    @Override
    public void visit(Qualified qualified) {
        if (!qualified.getFirst().getName().equals(currentActor)) {
            qualified.getFirst().accept(this);
            emitNoSpace(".");
        }
        qualified.getSecond().accept(this);
    }

    @Override
    public void visit(GoToPosXY goToPosXY) {
        emitNoSpace("goTo(");
        expectInteger();
        goToPosXY.getX().accept(this);
        comma();
        goToPosXY.getY().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(Variable variable) {
        variable.getName().accept(this);
    }

    @Override
    public void visit(ScratchList scratchList) {
        scratchList.getName().accept(this);
    }

    @Override
    public void visit(AttributeOf node) { // TODO check -- the old version had different ways of handling
        // TODO backdrop number etc.
        emitToken("attribute");
        node.getAttribute().accept(this);
        emitToken("of");
        node.getElementChoice().accept(this);
    }

    @Override
    public void visit(GlideSecsToXY glideSecsToXY) {
        emitNoSpace("glideSecondsTo(");
        expectInteger();
        glideSecsToXY.getSecs().accept(this);
        comma();
        glideSecsToXY.getX().accept(this);
        comma();
        glideSecsToXY.getY().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(NextCostume nextCostume) {
        emitNoSpace("nextCostume()");
    }

    @Override
    public void visit(ChangeGraphicEffectBy changeGraphicEffectBy) {
        emitNoSpace("changeGraphicEffectBy(\"");
        changeGraphicEffectBy.getEffect().accept(this);
        emitNoSpace("\"");
        comma();
        expectInteger();
        changeGraphicEffectBy.getValue().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(SetGraphicEffectTo setGraphicEffectTo) {
        define();
        setGraphicEffectTo.getEffect().accept(this);
        emitToken("_effect_value as");
        expectFloat();
        setGraphicEffectTo.getValue().accept(this);
        endExpectation();
    }

    @Override
    public void visit(Costume costume) {
        if (costume.getType().equals(NameNum.NAME)) {
            emitNoSpace("costumeName()");
        } else {
            emitNoSpace("costumeNumber()");
        }
    }

    @Override
    public void visit(LayerChoice layerChoice) {
        emitToken(layerChoice.getType());
    }

    @Override
    public void visit(NextBackdrop nextBackdrop) {
        emitToken("nextBackdrop()");
    }

    @Override
    public void visit(Backdrop backdrop) {
        NameNum type = backdrop.getType();
        if (type.equals(NameNum.NAME)) {
            emitNoSpace("backdropName()");
        } else {
            emitNoSpace("backdropNumber()");
        }
    }

    @Override
    public void visit(Size size) {
        emitNoSpace("size");
    }

    @Override
    public void visit(EventAttribute eventAttribute) {
        emitToken(eventAttribute.getType());
    }

    @Override
    public void visit(PositionX positionX) {
        emitNoSpace("x");
    }

    @Override
    public void visit(Direction direction) {
        emitNoSpace("direction");
    }

    @Override
    public void visit(SetSoundEffectTo setSoundEffectTo) {
        define();
        setSoundEffectTo.getEffect().accept(this);
        emitToken("_effect_value as");
        expectFloat();
        setSoundEffectTo.getValue().accept(this);
        endExpectation();
    }

    @Override
    public void visit(ChangeVolumeBy changeVolumeBy) {
        emitNoSpace("changeVolumeBy(");
        expectInteger();
        changeVolumeBy.getVolumeValue().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(SpriteTouchingColor spriteTouchingColor) {
        emitNoSpace("touchingColor(");
        spriteTouchingColor.getColor().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(PositionY positionY) {
        emitNoSpace("y");
    }

    @Override
    public void visit(SetRotationStyle setRotationStyle) {
        emitToken("define rotationStyle as");
        setRotationStyle.getRotation().accept(this);
    }

    @Override
    public void visit(RotationStyle rotationStyle) {
        emitString(rotationStyle.getToken());
    }

    @Override
    public void visit(ChangeSoundEffectBy changeSoundEffectBy) {
        emitNoSpace("changeSoundEffectBy(\"");
        changeSoundEffectBy.getEffect().accept(this);
        emitNoSpace("\"");
        comma();
        expectInteger();
        changeSoundEffectBy.getValue().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(SetVolumeTo setVolumeTo) {
        emitNoSpace("setVolumeTo(");
        expectInteger();
        setVolumeTo.getVolumeValue().accept(this);
        endExpectation();
        closeParentheses();
    }

    @Override
    public void visit(SetDragMode setDragMode) {
        emitToken("define draggable as");
        setDragMode.getDrag().accept(this);
    }

    @Override
    public void visit(DragMode dragMode) {
        emitString(dragMode.getToken());
    }

    @Override
    public void visit(Answer answer) {
        emitNoSpace("answer()");
    }

    @Override
    public void visit(HideList hideList) {
        emitNoSpace("hideVariable(\"");
        showHideVar = true;
        hideList.getIdentifier().accept(this);
        showHideVar = false;
        emitNoSpace("\"");
        closeParentheses();
    }

    @Override
    public void visit(ShowList showList) {
        emitNoSpace("showVariable(\"");
        showHideVar = true;
        showList.getIdentifier().accept(this);
        showHideVar = false;
        emitNoSpace("\"");
        closeParentheses();
    }

    @Override
    public void visit(ListContains listContains) {
        emitNoSpace("TODO"); // TODO -- grammar?
    }

    @Override
    public void visit(DeclarationBroadcastStmt declarationBroadcastStmt) {
        declare();
        declarationBroadcastStmt.getIdent().accept(this);
        as();
        declarationBroadcastStmt.getType().accept(this);
    }

    @Override public void visit(Volume volume) {
        emitNoSpace("volume()");
    }
}
