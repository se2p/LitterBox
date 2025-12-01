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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * Checks for a forever loop that checks for inputs and contains blocks that do not take time,
 * acknowledging it as good code (Responsive Loop Sensing).
 */
public class ResponsiveLoopSensing extends AbstractIssueFinder {
    private static final String NAME = "responsive_loop_sensing";
    private boolean inCondition = false;
    private boolean insideEquals = false;
    private boolean sensingInput = false;
    private boolean hasTimeConsumingBlock = false;
    private boolean hasInstantBlock = false;
    private boolean checkingLoopBody = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        // Reset state for each forever loop
        sensingInput = false;
        hasTimeConsumingBlock = false;
        hasInstantBlock = false;
        checkingLoopBody = true;

        // Check the loop body
        visitChildren(node);

        checkingLoopBody = false;

        if (sensingInput && !hasTimeConsumingBlock && hasInstantBlock) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (checkingLoopBody) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            inCondition = false;
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (checkingLoopBody) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            inCondition = false;
            visitChildren(node);
        }
    }

    // Input Sensing Checks (same as InterruptedLoopSensing)

    @Override
    public void visit(IsKeyPressed node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(Touching node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (inCondition) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(Equals node) {
        if (inCondition) {
            insideEquals = true;
            visitChildren(node);
            insideEquals = false;
        }
    }

    @Override
    public void visit(Variable node) {
        if (insideEquals) {
            sensingInput = true;
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (insideEquals) {
            sensingInput = true;
        }
    }

    // Time-Consuming Blocks (Forbidden)

    @Override
    public void visit(GlideSecsTo node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (checkingLoopBody) {
            hasTimeConsumingBlock = true;
        }
    }

    // Common instant blocks

    @Override
    public void visit(Broadcast node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(Say node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(Think node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(GoToPos node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(GoToPosXY node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(PointInDirection node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(PointTowards node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(StartSound node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(AddTo node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (checkingLoopBody) {
            hasInstantBlock = true;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
