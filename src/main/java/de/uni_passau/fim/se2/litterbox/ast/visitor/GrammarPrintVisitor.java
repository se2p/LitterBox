package de.uni_passau.fim.se2.litterbox.ast.visitor;


import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.URI;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithId;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Clicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.event.VariableAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouches;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ExpressionContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionListPlain;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ListExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DaysSince2000;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfVar;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Loudness;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Mod;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Mult;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunctOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.CoordinatePosition;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.PivotOf;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterListPlain;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.ImageResource;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.Resource;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.ResourceList;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.SoundResource;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ListOfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeAttributeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationAttributeAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationAttributeOfIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteAllOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeLayerBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToBackLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToFrontLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.IfOnEdgeBounce;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Rgba;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ImageType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.MapType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.SoundType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Id;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.SoundEffect;

import java.io.PrintStream;
import java.util.List;

public class GrammarPrintVisitor implements ScratchVisitor {

    private static final String INDENT = "    ";
    private PrintStream printStream;
    private int level;
    private boolean emitAttributeType = false;
    private boolean volume = false;
    private boolean emitSpace = true;

    public GrammarPrintVisitor(PrintStream printStream) {
        this.printStream = printStream;
        level = 0;
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
        for (ActorDefinition child : program.getActorDefinitionList().getDefintions()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ActorDefinition def) {
        newLine();
        appendIndentation();
        emitToken("actor");
        def.getIdent().accept(this);
        emitToken("is");
        def.getActorType().accept(this);
        begin();
        beginIndentation();
        ResourceList resources = def.getResources();
        List<Resource> resourceList = resources.getResourceList();
        for (Resource resource : resourceList) {
            newLine();
            appendIndentation();
            resource.accept(this);
        }

        DeclarationStmtList declarations = def.getDecls();
        List<DeclarationStmt> declarationStmtList1 = declarations.getDeclarationStmtList();
        for (DeclarationStmt declarationStmt : declarationStmtList1) {
            newLine();
            appendIndentation();
            declarationStmt.accept(this);
        }

        SetStmtList setStmtList = def.getSetStmtList();
        List<SetStmt> stmts = setStmtList.getStmts();
        for (SetStmt stmt : stmts) {
            newLine();
            appendIndentation();
            stmt.accept(this);
        }

        ProcedureDefinitionList procDefList = def.getProcedureDefinitionList();
        List<ProcedureDefinition> procDefs = procDefList.getList();
        for (ProcedureDefinition procDef : procDefs) {
            newLine();
            appendIndentation();
            procDef.accept(this);
        }

        ScriptList scripts = def.getScripts();
        List<Script> scriptList = scripts.getScriptList();
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
        emitToken("do");
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
        emitToken("clicked");
    }

    @Override
    public void visit(GreenFlag greenFlag) {
        emitToken("green");
        emitToken("flag");
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        keyPressed.getKey().accept(this);
        emitToken("pressed");
    }

    @Override
    public void visit(Key key) {
        emitToken("key");
        key.getKey().accept(this);
    }

    @Override
    public void visit(Never never) {
        emitToken("never");
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitToken("received");
        emitToken("message");
        receptionOfMessage.getMsg().accept(this);
    }

    @Override
    public void visit(Message message) {
        message.getMessage().accept(this);
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitToken("started");
        emitToken("as");
        emitToken("clone");
    }

    @Override
    public void visit(VariableAboveValue variableAboveValue) {
        emitToken("value");
        emitToken("of");
        variableAboveValue.getVariable().accept(this);
        emitToken("above");
        variableAboveValue.getValue().accept(this);
    }


    @Override
    public void visit(StmtList stmtList) {
        begin();
        beginIndentation();
        stmtList.getStmts().accept(this);
        endIndentation();
        end();
    }

    @Override
    public void visit(ListOfStmt listOfStmt) {
        for (Stmt stmt : listOfStmt.getListOfStmt()) {
            newLine();
            appendIndentation();
            stmt.accept(this);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone playSoundUntilDone) {
        emitNoSpace("playUntilDone(");
        playSoundUntilDone.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(StartSound startSound) {
        emitToken("startSound(");
        startSound.getElementChoice().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(ClearSoundEffects clearSoundEffects) {
        emitToken("clearSoundEffects()");
    }

    @Override
    public void visit(StopAllSounds stopAllSounds) {
        emitToken("stopAllSounds()");
    }

    @Override
    public void visit(AskAndWait askAndWait) {
        emitToken("ask");
        askAndWait.getQuestion().accept(this);
        emitToken("and wait");
    }

    @Override
    public void visit(SwitchBackdrop switchBackdrop) {
        emitToken("switch backdrop to");
        switchBackdrop.getElementChoice().accept(this);
    }

    @Override
    public void visit(SwitchBackdropAndWait switchBackdropAndWait) {
        emitToken("switch backdrop to");
        switchBackdropAndWait.getElementChoice().accept(this);
        emitToken("and wait");
    }

    @Override
    public void visit(ClearGraphicEffects clearGraphicEffects) {
        emitToken("clearGraphicEffects()");
    }

    @Override
    public void visit(ShowVariable showVariable) {
        emitToken("show variable");
        showVariable.getVariable().accept(this);
    }

    @Override
    public void visit(HideVariable hideVariable) {
        emitToken("hide variable");
        hideVariable.getVariable().accept(this);
    }

    @Override
    public void visit(Show show) {
        emitToken("show");
    }

    @Override
    public void visit(Hide hide) {
        emitToken("hide");
    }

    @Override
    public void visit(SayForSecs sayForSecs) {
        emitToken("say");
        sayForSecs.getString().accept(this);
        emitToken("for");
        sayForSecs.getSecs().accept(this);
        emitToken("secs");
    }

    @Override
    public void visit(Say say) {
        emitToken("say");
        say.getString().accept(this);
    }

    @Override
    public void visit(ThinkForSecs thinkForSecs) {
        emitToken("think");
        thinkForSecs.getThought().accept(this);
        emitToken("for");
        thinkForSecs.getSecs().accept(this);
        emitToken("secs");
    }

    @Override
    public void visit(Think think) {
        emitToken("think");
        think.getThought().accept(this);
    }

    @Override
    public void visit(SwitchCostumeTo switchCostumeTo) {
        emitToken("switch costume to");
        switchCostumeTo.getElementChoice().accept(this);
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
        emitToken("percent");
    }

    @Override
    public void visit(ChangeLayerBy changeLayerBy) {
        emitToken("change layer by");
        changeLayerBy.getNum().accept(this);
    }

    @Override
    public void visit(GoToLayer goToLayer) {
        emitToken("go to layer");
        goToLayer.getLayer().accept(this);
    }

    @Override
    public void visit(GoToFrontLayer goToFrontLayer) {
        emitToken("go to front layer");
    }

    @Override
    public void visit(GoToBackLayer goToBackLayer) {
        emitToken("go to back layer");
    }

    @Override
    public void visit(Next next) {
        emitToken("next");
    }

    @Override
    public void visit(Prev prev) {
        emitToken("prev");
    }

    @Override
    public void visit(Random random) {
        emitToken("random");
    }

    @Override
    public void visit(WithNumber withNumber) {
        emitToken("with_number");
        withNumber.getNumExpr().accept(this);
    }

    @Override
    public void visit(WithId withId) {
        emitToken("with_name");
        withId.getStringExpr().accept(this);
    }

    @Override
    public void visit(MoveSteps moveSteps) {
        emitToken("move");
        moveSteps.getSteps().accept(this);
        emitToken("steps");
    }

    @Override
    public void visit(TurnRight turnRight) {
        emitToken("turn right");
        turnRight.getDegrees().accept(this);
        emitToken("degrees");
    }

    @Override
    public void visit(TurnLeft turnLeft) {
        emitToken("turn left");
        turnLeft.getDegrees().accept(this);
        emitToken("degrees");
    }

    @Override
    public void visit(GoToPos goToPos) {
        emitToken("go to");
        goToPos.getPosition().accept(this);
    }

    @Override
    public void visit(PivotOf pivotOf) {
        emitToken("pivot of");
        pivotOf.getStringExpr().accept(this);
    }

    @Override
    public void visit(RandomPos randomPos) {
        emitToken("random_pos");
    }

    @Override
    public void visit(MousePos mousePos) {
        emitToken("mouse_pos");
    }

    @Override
    public void visit(CoordinatePosition coordinatePosition) {
        emitToken("(");
        coordinatePosition.getXCoord().accept(this);
        emitToken(",");
        coordinatePosition.getYCoord().accept(this);
        emitToken(")");
    }

    @Override
    public void visit(GlideSecsTo glideSecsTo) {
        emitToken("glide");
        glideSecsTo.getSecs().accept(this);
        emitToken("secs to");
        glideSecsTo.getPosition().accept(this);
    }

    @Override
    public void visit(PointInDirection pointInDirection) {
        emitToken("point in direction");
        pointInDirection.getDirection().accept(this);
    }

    @Override
    public void visit(PointTowards pointTowards) {
        emitToken("point towards");
        pointTowards.getPosition().accept(this);
    }

    @Override
    public void visit(ChangeXBy changeXBy) {
        emitToken("change x by");
        changeXBy.getNum().accept(this);
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
        deleteAllOf.getVariable().accept(this);
    }

    @Override
    public void visit(DeleteOf deleteOf) {
        emitToken("delete");
        deleteOf.getNum().accept(this);
        emitToken("of");
        deleteOf.getVariable().accept(this);
    }

    @Override
    public void visit(AddTo addTo) {
        emitToken("add");
        addTo.getString().accept(this);
        emitToken("to");
        addTo.getVariable().accept(this);
    }

    @Override
    public void visit(InsertAt insertAt) {
        emitToken("insert");
        insertAt.getString().accept(this);
        emitToken("at");
        insertAt.getIndex().accept(this);
        emitToken("of");
        insertAt.getVariable().accept(this);
    }

    @Override
    public void visit(ReplaceItem replaceItem) {
        emitToken("replace item");
        replaceItem.getIndex().accept(this);
        emitToken("of");
        replaceItem.getVariable().accept(this);
        emitToken("by");
        replaceItem.getString().accept(this);
    }

    @Override
    public void visit(WaitSeconds waitSeconds) {
        emitToken("wait");
        waitSeconds.getSeconds().accept(this);
        emitToken("seconds");
    }

    @Override
    public void visit(WaitUntil waitUntil) {
        emitToken("wait");
        emitToken("until");
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
        emitToken("and wait");
    }

    @Override
    public void visit(ResetTimer resetTimer) {
        emitToken("reset timer");
    }

    @Override
    public void visit(ChangeVariableBy changeVariableBy) {
        emitToken("change");
        changeVariableBy.getVariable().accept(this);
        emitToken("by");
        changeVariableBy.getExpr().accept(this);
    }

    @Override
    public void visit(ChangeAttributeBy changeAttributeBy) { //FIXME this won't work that way
        volume = false;
        emitNoSpace("change");
        emitAttributeType = true;
        changeAttributeBy.getAttribute().accept(this);
        emitNoSpace("By(");
        emitAttributeType = false;
        if (!volume) {
            emitSpace = false;
            changeAttributeBy.getAttribute().accept(this);
            comma();
        }
        //emitSpace = false; FIXME
        changeAttributeBy.getExpr().accept(this);
        closeParentheses();
        volume = false;
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
        emitToken("(");
        expressionList.getExpressionListPlain().accept(this);
        emitToken(")");
    }

    @Override
    public void visit(ExpressionListPlain expressionListPlain) {
        List<Expression> expressions = expressionListPlain.getExpressions();
        if (expressions.size() > 0) {
            for (int i = 0; i < expressions.size() - 1; i++) {
                expressions.get(i).accept(this);
                comma();
            }
            expressions.get(expressions.size() - 1).accept(this);
        }
    }

    @Override
    public void visit(IfThenStmt ifThenStmt) { // FIXME format?
        emitToken("if");
        ifThenStmt.getBoolExpr().accept(this);
        emitToken("then");
        ifThenStmt.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) { //FIXME format?
        emitToken("if");
        ifElseStmt.getBoolExpr().accept(this);

        emitToken("then");
        beginIndentation();
        ifElseStmt.getStmtList().accept(this);
        endIndentation();

        newLine();
        appendIndentation();
        emitToken("else");
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitToken("until");
        untilStmt.getBoolExpr().accept(this);
        emitToken("repeat");
        untilStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitToken("repeat");
        repeatTimesStmt.getTimes().accept(this);
        emitToken("times");
        repeatTimesStmt.getStmtList().accept(this);
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitToken("repeat");
        emitToken("forever");
        repeatForeverStmt.getStmtList().accept(this);
    }

    //@Override
    //public void visit(StmtListPlain) FIXME
    @Override
    public void visit(ProcedureDefinition procedureDefinition) {
        emitToken("procedure");
        procedureDefinition.getIdent().accept(this);
        procedureDefinition.getParameterList().accept(this);
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
        emitToken("delete this clone");
    }

    @Override
    public void visit(ParameterList parameterList) {
        emitToken("(");
        parameterList.getParameterListPlain().accept(this);
        emitToken(")");
    }

    @Override
    public void visit(ParameterListPlain parameterListPlain) {
        List<Parameter> parameters = parameterListPlain.getParameters();
        if (parameters.size() > 0) {
            for (int i = 0; i < parameters.size() - 1; i++) {
                parameters.get(i).accept(this);
                comma();
            }
            parameters.get(parameters.size() - 1).accept(this);
        }
    }

    private void comma() {
        emitToken(",");
    }

    @Override
    public void visit(Parameter parameter) {
        parameter.getIdent().accept(this);
        colon();
        parameter.getType().accept(this);
    }

    private void colon() {
        emitToken(":");
    }

    @Override
    public void visit(ImageResource imageResource) {
        emitToken("image");
        imageResource.getIdent().accept(this);
        imageResource.getUri().accept(this);
    }

    @Override
    public void visit(SoundResource soundResource) {
        emitToken("sound");
        soundResource.getIdent().accept(this);
        soundResource.getUri().accept(this);
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
        emitToken("boolean");
    }

    @Override
    public void visit(ImageType imageType) {
        emitToken("image");
    }

    @Override
    public void visit(ListType listType) {
        emitToken("list string"); // TODO is this correct
    }

    @Override
    public void visit(MapType mapType) {
        emitToken("map string");
    }

    @Override
    public void visit(NumberType numberType) {
        emitToken("number");
    }

    @Override
    public void visit(SoundType soundType) {
        emitToken("sound");
    }

    @Override
    public void visit(StringType stringType) {
        emitToken("string");
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
    public void visit(AttributeOf attributeOf) {
        StringExpr attribute = attributeOf.getAttribute();
        boolean done = false;
        if (attribute instanceof StringLiteral) {
            String attributeText = ((StringLiteral) attribute).getText();
            if (attributeText.equalsIgnoreCase("backdrop_number")) {
                emitToken("backdropNumber()");
                done = true;
            } else if (attributeText.equalsIgnoreCase("backdrop_name")) {
                emitToken("backdropName()");
                done = true;
            } else if (attributeText.equalsIgnoreCase("sound_volume")) {
                emitToken("volume()");
                done = true;
            }
        }
        if (!done) {
            emitToken("attribute");
            attributeOf.getAttribute().accept(this);
            of();
            attributeOf.getIdentifier().accept(this);
        }
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
        emitToken("username");
    }

    @Override
    public void visit(ItemOfVariable itemOfVariable) {
        emitToken("item");
        itemOfVariable.getNum().accept(this);
        of();
        itemOfVariable.getVariable().accept(this);
    }

    @Override
    public void visit(UnspecifiedStringExpr unspecifiedStringExpr) {
        emitToken("?string");
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
        boolean done = false;
        StringExpr attribute = setAttributeTo.getStringExpr();
        if (attribute instanceof StringLiteral) {
            if (((StringLiteral) attribute).getText().equalsIgnoreCase("volume")) {
                emitNoSpace("setVolumeTo(");
                setAttributeTo.getExpr().accept(this);
                closeParentheses();
                done = true;
            }
        }
        if (!done) {
            emitToken("define");
            attribute();
            setAttributeTo.getStringExpr().accept(this);
            as();
            setAttributeTo.getExpr().accept(this);
        }
    }

    private void to() {
        emitToken("to");
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
        setVariableTo.getVariable().accept(this);
        to();
        setVariableTo.getExpr().accept(this);
    }

    private void declare() {
        emitToken("declare");
    }

    private void of() {
        emitToken("of");
    }

    private void as() {
        emitToken("as");
    }

    @Override
    public void visit(URI uri) {
        emitToken(uri.getUri().getText());
    }

    @Override
    public void visit(ActorType actorType) {
        if (actorType.equals(ActorType.STAGE)) {
            emitToken("ScratchStage");
        } else if (actorType.equals(ActorType.SPRITE)) {
            emitToken("ScratchSprite");
        } else {
            emitToken("ScratchActor");
        }
    }

    @Override
    public void visit(Id id) {
        emitToken("\"" + id.getName() + "\"");
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (!emitAttributeType) {
            emitToken("\"" + stringLiteral.getText() + "\"");
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
        emitToken("\"" + strId.getName() + "\"");
    }

    @Override
    public void visit(BoolLiteral boolLiteral) {
        emitToken(String.valueOf(boolLiteral.getValue()));
    }

    @Override
    public void visit(Not not) {
        emitToken("not");
        not.getOperand1().accept(this);
    }

    @Override
    public void visit(And and) {
        and.getOperand1().accept(this);
        emitToken("and");
        and.getOperand2().accept(this);
    }

    @Override
    public void visit(Or or) {
        or.getOperand1().accept(this);
        emitToken("or");
        or.getOperand2().accept(this);
    }

    @Override
    public void visit(BiggerThan biggerThan) {
        biggerThan.getOperand1().accept(this);
        emitToken(">");
        biggerThan.getOperand2().accept(this);
    }

    @Override
    public void visit(LessThan lessThan) {
        lessThan.getOperand1().accept(this);
        emitToken("<");
        lessThan.getOperand1().accept(this);
    }

    @Override
    public void visit(Equals equals) {
        equals.getOperand1().accept(this);
        emitToken("=");
        equals.getOperand2().accept(this);
    }

    @Override
    public void visit(ExpressionContains expressionContains) {
        expressionContains.getContaining().accept(this);
        emitToken("contains");
        expressionContains.getContained().accept(this);
    }

    @Override
    public void visit(Touching touching) {
        emitToken("touching");
        touching.getTouchable().accept(this);
    }

    @Override
    public void visit(MousePointer mousePointer) {
        emitToken("mousepointer");
    }

    @Override
    public void visit(Edge edge) {
        emitToken("edge");
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
        emitToken(String.valueOf(colorLiteral.getBlue()));
    }

    @Override
    public void visit(ColorTouches colorTouches) {
        colorTouches.getOperand1().accept(this);
        emitToken("touches");
        colorTouches.getOperand2().accept(this);
    }

    @Override
    public void visit(IsKeyPressed isKeyPressed) {
        emitToken("key");
        isKeyPressed.getKey().accept(this);
        emitToken("pressed");
    }

    @Override
    public void visit(Rgba rgba) {
        emitToken("rgba");
        rgba.getrValue().accept(this);
        rgba.getgValue().accept(this);
        rgba.getbValue().accept(this);
        rgba.getaValue().accept(this);
    }

    @Override
    public void visit(FromNumber fromNumber) {
        emitToken("from number");
        fromNumber.getValue().accept(this);
    }

    @Override
    public void visit(IsMouseDown isMouseDown) {
        emitToken("mouse down");
    }

    @Override
    public void visit(UnspecifiedBoolExpr unspecifiedBoolExpr) {
        emitToken("?bool");
    }

    @Override
    public void visit(AsNumber asNumber) {
        emitToken("as number");
        asNumber.getOperand1().accept(this);
    }

    @Override
    public void visit(Timer timer) {
        emitToken("timer");
    }

    @Override
    public void visit(DaysSince2000 daysSince2000) {
        emitToken("days since millennium");
    }

    @Override
    public void visit(Current current) {
        emitToken("current");
        current.getTimeComp().accept(this);
    }

    @Override
    public void visit(TimeComp timeComp) {
        emitToken(timeComp.getLabel());
    }

    @Override
    public void visit(DistanceTo distanceTo) {
        emitToken("distanceto");
        distanceTo.getPosition().accept(this);
    }

    @Override
    public void visit(MouseX mouseX) {
        emitToken("mousex");
    }

    @Override
    public void visit(MouseY mouseY) {
        emitToken("mousey");
    }

    @Override
    public void visit(Loudness loudness) {
        emitToken("loudness");
    }

    @Override
    public void visit(LengthOfString lengthOfString) {
        emitToken("length of");
        lengthOfString.getStringExpr().accept(this);
    }

    @Override
    public void visit(LengthOfVar lengthOfVar) {
        emitToken("length of list");
        lengthOfVar.getVariable().accept(this);
    }

    @Override
    public void visit(IndexOf indexOf) {
        emitToken("index of");
        indexOf.getExpr().accept(this);
        emitToken("in");
        indexOf.getVariable().accept(this);
    }

    @Override
    public void visit(PickRandom pickRandom) {
        emitToken("pick random");
        pickRandom.getFrom().accept(this);
        emitToken("of");
        pickRandom.getTo().accept(this);
    }

    @Override
    public void visit(Round round) {
        emitToken("round");
        round.getNum().accept(this);
    }

    @Override
    public void visit(NumberLiteral number) {
        emitToken(String.valueOf(number.getValue()));
    }

    @Override
    public void visit(ListExpr listExpr) {
        emitToken("ListExprTODO"); //FIXME but how
    }

    @Override
    public void visit(NumFunctOf numFunctOf) {
        numFunctOf.getFunct().accept(this);
        openParentheses();
        numFunctOf.getNum().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Mult mult) {
        openParentheses();
        mult.getOperand1().accept(this);
        emitToken("*");
        mult.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Div div) {
        openParentheses();
        div.getOperand1().accept(this);
        emitToken("/");
        div.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Mod mod) {
        openParentheses();
        mod.getOperand1().accept(this);
        emitToken("mod");
        mod.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Add add) {
        openParentheses();
        add.getOperand1().accept(this);
        emitToken("+");
        add.getOperand2().accept(this);
        closeParentheses();
    }

    @Override
    public void visit(Minus minus) {
        openParentheses();
        minus.getOperand1().accept(this);
        emitToken("-");
        minus.getOperand2().accept(this);
        closeParentheses();
    }

    private void openParentheses() {
        emitNoSpace("(");
    }

    private void closeParentheses() {
        emitToken(")");
    }

    @Override
    public void visit(UnspecifiedNumExpr unspecifiedNumExpr) {
        emitToken("?number");
    }

    @Override
    public void visit(NumFunct numFunct) {
        emitNoSpace(numFunct.getFunction());
    }

    @Override
    public void visit(UnspecifiedExpression unspecifiedExpression) {
        emitToken("?expr");
    }

    @Override
    public void visit(Qualified qualified) {
        qualified.getFirst().accept(this);
        emitToken(".");
        qualified.getSecond().accept(this);
    }

    private void emitToken(String string) {
        emitNoSpace(string);
        if (emitSpace) {
            emitNoSpace(" ");
        }
        emitSpace = true;
    }

    private void emitNoSpace(String string) {
        printStream.append(string);
    }

    private void endIndentation() {
        level--;
    }

    private void beginIndentation() {
        level++;
    }

    private void appendIndentation() {
        String currentIndent = new String(new char[level]).replace("\0", INDENT);
        emitToken(currentIndent);
    }

    private void begin() {
        emitToken("begin");
    }

    private void end() {
        newLine();
        appendIndentation();
        emitToken("end");
    }

    private void newLine() {
        emitToken("\n");
    }
}
