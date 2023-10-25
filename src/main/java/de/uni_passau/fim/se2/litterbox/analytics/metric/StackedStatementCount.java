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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class StackedStatementCount<T extends ASTNode> implements ScratchVisitor, MetricExtractor<T>, PenExtensionVisitor, TextToSpeechExtensionVisitor {

    public static final String NAME = "stacked_statement_count";
    private int maxStackedDepth = 0;
    private int currentStackedDepth = 0;
    private boolean insideProcedure = false;
    private boolean insideScript = false;

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        visitChildren(node);
        insideProcedure = false;
    }

    //Motion
    @Override
    public void visit(MoveSteps node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(TurnLeft node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(TurnRight node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(GoToPos node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(GoToPosXY node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(PointInDirection node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(PointTowards node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeXBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeYBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetXTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetYTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //Looks
    @Override
    public void visit(SayForSecs node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(Say node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(Think node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSizeBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetSizeTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeLayerBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //sound
    @Override
    public void visit(PlaySoundUntilDone node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(StartSound node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetVolumeTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //Events
    @Override
    public void visit(Event node) {
        if (node instanceof AttributeAboveValue attributeAboveValue) {
            EventAttribute.EventAttributeType eventAttributeType = attributeAboveValue.getAttribute().getType();
            if (eventAttributeType == EventAttribute.EventAttributeType.LOUDNESS
                    || eventAttributeType == EventAttribute.EventAttributeType.TIMER) {
                currentStackedDepth = 0;
                visitChildren(node);
            }
        }
    }

    @Override
    public void visit(Broadcast node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //control
    @Override
    public void visit(WaitSeconds node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(CreateCloneOf node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //sensing
    @Override
    public void visit(AskAndWait node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }
//Variables

    @Override
    public void visit(SetVariableTo node) {
        if (insideScript || insideProcedure) {
            currentStackedDepth = 0;
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(AddTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(DeleteOf node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(InsertAt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ReplaceItem node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    //blocks
    @Override
    public void visit(CallStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        maxStackedDepth = 0;
        node.accept(this);
        return maxStackedDepth;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        visitChildren(node);
        insideScript = false;
    }

    //sound
    @Override
    public void visit(Volume node) {
        incrementAndVisit(node);
    }

    //looks
    @Override
    public void visit(Size node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        incrementAndVisit(node);
    }

    //sensing
    @Override
    public void visit(Touching node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(DistanceTo node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(IsKeyPressed node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(AttributeOf node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Answer node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(MouseX node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(MouseY node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Loudness node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Timer node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(DaysSince2000 node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Username node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Current node) {
        incrementAndVisit(node);
    }

    //Operators
    @Override
    public void visit(BiggerThan node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(LessThan node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Equals node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(And node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Or node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Not node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Add node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Mult node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Div node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Minus node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(PickRandom node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Join node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(LetterOf node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Round node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Mod node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(StringContains node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(LengthOfString node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(NumFunctOf node) {
        incrementAndVisit(node);
    }

    //variables
    @Override
    public void visit(ScratchList node) {
        if (insideScript || insideProcedure) {
            incrementAndVisit(node);
        }
    }

    @Override
    public void visit(Variable node) {
        if (insideScript || insideProcedure) {
            incrementAndVisit(node);
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(ListContains node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(IndexOf node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(LengthOfVar node) {
        incrementAndVisit(node);
    }

    //Motion
    @Override
    public void visit(PositionX node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(PositionY node) {
        incrementAndVisit(node);
    }

    @Override
    public void visit(Direction node) {
        incrementAndVisit(node);
    }

    private void incrementAndVisit(ASTNode node) {
        currentStackedDepth++;
        if (currentStackedDepth > maxStackedDepth) {
            maxStackedDepth = currentStackedDepth;
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetLanguage node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(SetVoice node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }

    @Override
    public void visit(Speak node) {
        currentStackedDepth = 0;
        visitChildren(node);
    }
}
