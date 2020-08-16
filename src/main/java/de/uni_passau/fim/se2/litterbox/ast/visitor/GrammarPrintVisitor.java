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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.*;

import java.io.PrintStream;
import java.util.List;

public class GrammarPrintVisitor extends PrintVisitor {

    private boolean emitAttributeType = false;
    private boolean volume = false;

    public GrammarPrintVisitor(PrintStream printStream) {
        super(printStream);
    }

    @Override
    public void visit(ASTNode node) {
        System.err.println(node.getClass().getName());
    }

    @Override
    public void visit(Program program) {
        appendIndentation();
        emitToken("program");
        program.getIdent().accept(this);
        List<ActorDefinition> definitions = program.getActorDefinitionList().getDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            definitions.get(i).accept(this);
            if (i < definitions.size() - 1) {
                newLine();
            }
        }
    }

    @Override
    public void visit(ActorDefinition def) {
        newLine();
        appendIndentation();
        emitToken("actor");
        def.getIdent().accept(this);
        emitToken(" is");
        def.getActorType().accept(this);
        begin();
        beginIndentation();
        DeclarationStmtList declarations = def.getDecls();
        List<DeclarationStmt> declarationStmtList = declarations.getDeclarationStmtList();
        if (declarationStmtList.size() > 0) {
            newLine();
        }
        for (DeclarationStmt declarationStmt : declarationStmtList) {
            newLine();
            appendIndentation();
            declarationStmt.accept(this);
        }

        SetStmtList setStmtList = def.getSetStmtList();
        List<SetStmt> stmts = setStmtList.getStmts();
        if (stmts.size() > 0) {
            newLine();
        }
        for (SetStmt stmt : stmts) {
            newLine();
            appendIndentation();
            stmt.accept(this);
        }

        ProcedureDefinitionList procDefList = def.getProcedureDefinitionList();
        List<ProcedureDefinition> procDefs = procDefList.getList();
        if (procDefs.size() > 0) {
            newLine();
        }
        for (ProcedureDefinition procDef : procDefs) {
            newLine();
            appendIndentation();
            procDef.accept(this);
        }

        ScriptList scripts = def.getScripts();
        List<Script> scriptList = scripts.getScriptList();
        if (scriptList.size() > 0) {
            newLine();
        }
        for (Script script : scriptList) {
            newLine();
            appendIndentation();
            script.accept(this);
        }
        endIndentation();
        end();
    }

    @Override
    public void visit(Script script) {
        emitToken("script");
        emitToken("on");
        script.getEvent().accept(this);
        emitNoSpace(" do");
        script.getStmtList().accept(this);
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
        emitNoSpace("clicked");
    }

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("green flag");
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        keyPressed.getKey().accept(this);
        emitNoSpace("pressed");
    }

    @Override
    public void visit(Key key) {
        emitToken("key");
        key.getKey().accept(this);
    }

    @Override
    public void visit(Never never) {
        emitNoSpace("never");
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitToken("received message");
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
        attributeAboveValue.getValue().accept(this);
    }

    @Override
    public void visit(StmtList stmtList) {
        begin();
        beginIndentation();
        for (Stmt stmt : stmtList.getStmts()) {
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
        emitToken("ask");
        askAndWait.getQuestion().accept(this);
        emitToken(" and wait");
    }

    @Override
    public void visit(SwitchBackdrop switchBackdrop) {
        emitNoSpace("switchBackdropTo(");
        switchBackdrop.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(SwitchBackdropAndWait switchBackdropAndWait) {
        emitNoSpace("switchBackdropToAndWait(");
        switchBackdropAndWait.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ClearGraphicEffects clearGraphicEffects) {
        emitNoSpace("clearGraphicEffects()");
    }

    @Override
    public void visit(ShowVariable showVariable) {
        emitToken("show variable");
        showVariable.getIdentifier().accept(this);
    }

    @Override
    public void visit(HideVariable hideVariable) {
        emitToken("hide variable");
        hideVariable.getIdentifier().accept(this);
    }

    @Override
    public void visit(Show show) {
        emitNoSpace("show");
    }

    @Override
    public void visit(Hide hide) {
        emitNoSpace("hide");
    }

    @Override
    public void visit(SayForSecs sayForSecs) {
        emitNoSpace("sayTextFor(");
        sayForSecs.getString().accept(this);
        comma();
        sayForSecs.getSecs().accept(this);
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
        emitToken("think");
        thinkForSecs.getThought().accept(this);
        emitToken(" for");
        thinkForSecs.getSecs().accept(this);
        emitToken(" secs");
    }

    @Override
    public void visit(Think think) {
        emitToken("think");
        think.getThought().accept(this);
    }

    @Override
    public void visit(SwitchCostumeTo switchCostumeTo) {
        emitNoSpace("changeCostumeTo(");
        switchCostumeTo.getCostumeChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ChangeSizeBy changeSizeBy) {
        emitToken("change size by");
        changeSizeBy.getNum().accept(this);
    }

    @Override
    public void visit(SetSizeTo setSizeTo) {
        emitToken("set size to");
        setSizeTo.getPercent().accept(this);
        emitToken(" percent");
    }

    @Override
    public void visit(ChangeLayerBy changeLayerBy) {
        emitToken("change layer by");
        changeLayerBy.getNum().accept(this);
    }

    @Override
    public void visit(GoToLayer goToLayer) {
        emitToken("go to layer");
        goToLayer.getLayerChoice().accept(this);
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
        emitToken("with_name");
        withExpr.getExpression().accept(this);
    }

    @Override
    public void visit(MoveSteps moveSteps) {
        emitToken("moveSteps(");
        moveSteps.getSteps().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(TurnRight turnRight) {
        emitNoSpace("turnRight(");
        turnRight.getDegrees().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(TurnLeft turnLeft) {
        emitNoSpace("turnLeft(");
        turnLeft.getDegrees().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(GoToPos goToPos) {
        emitToken("go to");
        goToPos.getPosition().accept(this);
    }

    @Override
    public void visit(FromExpression fromExpression) {
        emitToken("pivot of");
        fromExpression.getStringExpr().accept(this);
    }

    @Override
    public void visit(RandomPos randomPos) {
        emitNoSpace("random_pos");
    }

    @Override
    public void visit(MousePos mousePos) {
        emitNoSpace("mouse_pos");
    }

    @Override
    public void visit(GlideSecsTo glideSecsTo) {
        emitToken("glide");
        glideSecsTo.getSecs().accept(this);
        emitToken(" secs to");
        glideSecsTo.getPosition().accept(this);
    }

    @Override
    public void visit(PointInDirection pointInDirection) {
        emitToken("point in direction");
        pointInDirection.getDirection().accept(this);
    }

    @Override
    public void visit(PointTowards pointTowards) {
        emitToken("pointTowards(");
        pointTowards.getPosition().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ChangeXBy changeXBy) {
        emitToken("changeXBy(");
        changeXBy.getNum().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ChangeYBy changeYBy) {
        emitToken("change y by");
        changeYBy.getNum().accept(this);
    }

    @Override
    public void visit(SetXTo setXTo) {
        emitToken("set x to");
        setXTo.getNum().accept(this);
    }

    @Override
    public void visit(SetYTo setYTo) {
        emitToken("set y to");
        setYTo.getNum().accept(this);
    }

    @Override
    public void visit(IfOnEdgeBounce ifOnEdgeBounce) {
        emitToken("if on edge bounce");
    }

    @Override
    public void visit(DeleteAllOf deleteAllOf) {
        emitToken("delete all of");
        deleteAllOf.getIdentifier().accept(this);
    }

    @Override
    public void visit(DeleteOf deleteOf) {
        emitToken("delete");
        deleteOf.getNum().accept(this);
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
        insertAt.getIndex().accept(this);
        emitToken(" of");
        insertAt.getIdentifier().accept(this);
    }

    @Override
    public void visit(ReplaceItem replaceItem) {
        emitToken("replace item");
        replaceItem.getIndex().accept(this);
        emitToken(" of");
        replaceItem.getIdentifier().accept(this);
        emitToken(" by");
        replaceItem.getString().accept(this);
    }

    @Override
    public void visit(WaitSeconds waitSeconds) {
        emitToken("wait");
        waitSeconds.getSeconds().accept(this);
        emitToken(" seconds");
    }

    @Override
    public void visit(WaitUntil waitUntil) {
        emitToken("wait until");
        waitUntil.getUntil().accept(this);
    }

    @Override //FIXME inconsistency between Litterbox and Grammar
    public void visit(StopOtherScriptsInSprite stopOtherScriptsInSprite) {
        emitToken("stop other scripts in sprite");
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
        emitToken("change");
        changeVariableBy.getIdentifier().accept(this);
        emitToken(" by");
        changeVariableBy.getExpr().accept(this);
    }

    @Override
    public void visit(ExpressionStmt expressionStmt) {
        emitToken("evaluate");
        expressionStmt.getExpression().accept(this);
    }

    @Override
    public void visit(CallStmt callStmt) {
        callStmt.getIdent().accept(this);
        callStmt.getExpressions().accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        openParentheses();
        List<Expression> expressions = expressionList.getExpressions();
        if (expressions.size() > 0) {
            for (int i = 0; i < expressions.size() - 1; i++) {
                expressions.get(i).accept(this);
                comma();
            }
            expressions.get(expressions.size() - 1).accept(this);
        }
        closeParentheses();
    }

    @Override
    public void visit(IfThenStmt ifThenStmt) { // FIXME format?
        emitToken("if");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(" then");
        ifThenStmt.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) { //FIXME format?
        emitToken("if");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(" then");
        beginIndentation();
        ifElseStmt.getStmtList().accept(this);
        endIndentation();

        newLine();
        appendIndentation();
        emitNoSpace("else");
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
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
        repeatTimesStmt.getTimes().accept(this);
        emitNoSpace(" times");
        repeatTimesStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("repeat forever");
        repeatForeverStmt.getStmtList().accept(this);
    }

    //@Override
    //public void visit(StmtListPlain) FIXME
    @Override
    public void visit(ProcedureDefinition procedureDefinition) {
        emitToken("procedure");
        procedureDefinition.getIdent().accept(this);
        procedureDefinition.getParameterDefinitionList().accept(this);
        procedureDefinition.getStmtList().accept(this);
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
        declare();
        emitToken("attribute");
        declarationAttributeAsTypeStmt.getStringExpr().accept(this);
        as();
        declarationAttributeAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(BooleanType booleanType) {
        emitNoSpace("boolean");
    }

    @Override
    public void visit(ImageType imageType) {
        emitNoSpace("image");
    }

    @Override
    public void visit(ListType listType) {
        emitNoSpace("list string"); // TODO is this correct
    }

    @Override
    public void visit(NumberType numberType) {
        emitNoSpace("number");
    }

    @Override
    public void visit(SoundType soundType) {
        emitNoSpace("sound");
    }

    @Override
    public void visit(StringType stringType) {
        emitNoSpace("string");
    }

    @Override
    public void visit(AsString asString) {
        emitToken("as string");
        asString.getOperand1().accept(this);
    }

    @Override
    public void visit(AsBool asString) {
        emitToken("as bool");
        asString.getOperand1().accept(this);
    }

    @Override
    public void visit(Join join) {
        emitToken("join");
        join.getOperand1().accept(this);
        join.getOperand2().accept(this);
    }

    @Override
    public void visit(LetterOf letterOf) {
        emitToken("letter");
        letterOf.getNum().accept(this);
        of();
        letterOf.getStringExpr().accept(this);
    }

    @Override
    public void visit(Username username) {
        emitNoSpace("username");
    }

    @Override
    public void visit(ItemOfVariable itemOfVariable) {
        emitToken("item");
        itemOfVariable.getNum().accept(this);
        of();
        itemOfVariable.getIdentifier().accept(this);
    }

    @Override
    public void visit(UnspecifiedStringExpr unspecifiedStringExpr) {
        emitNoSpace("?string");
    }

    @Override
    public void visit(DeclarationIdentAsTypeStmt declarationIdentAsTypeStmt) {
        declare();
        declarationIdentAsTypeStmt.getIdent().accept(this);
        as();
        declarationIdentAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(DeclarationAttributeOfIdentAsTypeStmt declarationAttributeOfIdentAsTypeStmt) {
        declare();
        emitToken("attribute");
        declarationAttributeOfIdentAsTypeStmt.getStringExpr().accept(this);
        of();
        declarationAttributeOfIdentAsTypeStmt.getIdent().accept(this);
        as();
        declarationAttributeOfIdentAsTypeStmt.getType().accept(this);
    }

    @Override
    public void visit(SetAttributeTo setAttributeTo) {
        emitToken("define");
        attribute();
        setAttributeTo.getStringExpr().accept(this);
        as();
        setAttributeTo.getExpr().accept(this);
    }

    private void to() {
        emitToken(" to");
    }

    private void attribute() {
        emitToken("attribute");
    }

    private void set() {
        emitToken("set");
    }

    @Override
    public void visit(SetVariableTo setVariableTo) {
        set();
        setVariableTo.getIdentifier().accept(this);
        to();
        setVariableTo.getExpr().accept(this);
    }

    private void declare() {
        emitToken("declare");
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
            emitNoSpace("\"" + stringLiteral.getText() + "\"");
        } else {
            String text = stringLiteral.getText();
            if (GraphicEffect.contains(text)) {
                emitNoSpace("GraphicEffect");
            } else if (SoundEffect.contains(text)) {
                emitNoSpace("SoundEffect");
            } else if (text.equalsIgnoreCase("VOLUME")) {
                emitNoSpace("Volume");
                volume = true;
            }
        }
    }

    @Override
    public void visit(StrId strId) {
        emitToken("strid");
        emitNoSpace("\"" + strId.getName() + "\"");
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
        biggerThan.getOperand1().accept(this);
        emitToken(" >");
        biggerThan.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(LessThan lessThan) {
        openParentheses();
        lessThan.getOperand1().accept(this);
        emitToken(" <");
        lessThan.getOperand1().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Equals equals) {
        openParentheses();
        equals.getOperand1().accept(this);
        emitToken(" =");
        equals.getOperand2().accept(this);
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
        boolean done = false;
        Touchable touchable = touching.getTouchable();
        if (touchable instanceof Edge) {
            emitNoSpace("touchingEdge(");
        } else if (touchable instanceof MousePointer) {
            emitNoSpace("touchingMousePointer()");
            done = true;
        } else if (touchable instanceof Color) {
            emitNoSpace("touchingColor(");
        } else {
            emitNoSpace("touchingObject(");
        }
        if (!done) {
            touching.getTouchable().accept(this);
        }
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
        emitToken("sprite");
        spriteTouchable.getStringExpr().accept(this);
    }

    @Override //TODO this is not specified in the grammar
    public void visit(ColorLiteral colorLiteral) {
        emitToken("rgb");
        emitToken(String.valueOf(colorLiteral.getRed()));
        emitToken(String.valueOf(colorLiteral.getGreen()));
        emitNoSpace(String.valueOf(colorLiteral.getBlue()));
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
        emitToken("key");
        isKeyPressed.getKey().accept(this);
        emitNoSpace(" pressed");
    }

    @Override
    public void visit(FromNumber fromNumber) {
        emitToken("from number");
        fromNumber.getValue().accept(this);
    }

    @Override
    public void visit(IsMouseDown isMouseDown) {
        emitNoSpace("mouse down");
    }

    @Override
    public void visit(UnspecifiedBoolExpr unspecifiedBoolExpr) {
        emitNoSpace("?bool");
    }

    @Override
    public void visit(AsNumber asNumber) {
        emitToken("as number");
        asNumber.getOperand1().accept(this);
    }

    @Override
    public void visit(Timer timer) {
        emitNoSpace("timer");
    }

    @Override
    public void visit(DaysSince2000 daysSince2000) {
        emitNoSpace("days since millennium");
    }

    @Override
    public void visit(Current current) {
        emitToken("current");
        current.getTimeComp().accept(this);
    }

    @Override
    public void visit(TimeComp timeComp) {
        emitNoSpace(timeComp.getLabel());
    }

    @Override
    public void visit(DistanceTo distanceTo) {
        if (distanceTo.getPosition() instanceof MousePos) {
            emitNoSpace("distanceToMousePointer()");
        } else {
            emitToken("distanceto");
            distanceTo.getPosition().accept(this);
        }
    }

    @Override
    public void visit(MouseX mouseX) {
        emitNoSpace("mousex");
    }

    @Override
    public void visit(MouseY mouseY) {
        emitNoSpace("mousey");
    }

    @Override
    public void visit(Loudness loudness) {
        emitNoSpace("loudness");
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
        indexOf.getExpr().accept(this);
        emitToken(" in");
        indexOf.getIdentifier().accept(this);
    }

    @Override
    public void visit(PickRandom pickRandom) {
        emitToken("pick random");
        pickRandom.getOperand1().accept(this);
        emitToken(" of");
        pickRandom.getOperand2().accept(this);
    }

    @Override
    public void visit(Round round) {
        emitToken("round");
        round.getOperand1().accept(this);
    }

    @Override
    public void visit(NumberLiteral number) {
        emitNoSpace(String.valueOf(number.getValue()));
    }

    @Override
    public void visit(NumFunctOf numFunctOf) {
        numFunctOf.getOperand1().accept(this);
        numFunctOf.getOperand2().accept(this);
    }

    @Override
    public void visit(Mult mult) {
        openParentheses();
        mult.getOperand1().accept(this);
        emitToken(" *");
        mult.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Div div) {
        openParentheses();
        div.getOperand1().accept(this);
        emitToken(" /");
        div.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Mod mod) {
        openParentheses();
        mod.getOperand1().accept(this);
        emitToken(" mod");
        mod.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Add add) {
        openParentheses();
        add.getOperand1().accept(this);
        emitToken(" +");
        add.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Minus minus) {
        openParentheses();
        minus.getOperand1().accept(this);
        emitToken(" -");
        minus.getOperand2().accept(this);
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
    public void visit(NumFunct numFunct) {
        emitNoSpace(numFunct.getFunction());
    }

    @Override
    public void visit(UnspecifiedExpression unspecifiedExpression) {
        emitNoSpace("?expr");
    }

    @Override
    public void visit(Qualified qualified) {
        qualified.getFirst().accept(this);
        emitNoSpace(".");
        qualified.getSecond().accept(this);
    }
}
