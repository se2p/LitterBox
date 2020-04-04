/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.utils;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.VariableAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ExpressionContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.AsListIndex;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfVar;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefiniton;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeAttributeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteAllOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BlockCount implements IssueFinder, ScratchVisitor {
    public static final String NAME = "block_count";
    public static final String SHORT_NAME = "blockCnt";
    private int count = 0;
    private boolean insideScript = false;
    private boolean insideProcedure = false;
    private boolean insideParameterList = false;
    private boolean fixedBlock = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        insideScript = false;
        insideProcedure = false;
        insideParameterList = false;
        fixedBlock = false;
        program.accept(this);
        return new IssueReport(NAME, count, new LinkedList<>(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideScript = false;
    }

    @Override
    public void visit(ASTNode node) {
        if ((insideScript || insideProcedure) && !fixedBlock) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Costume node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(Backdrop node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(AttributeOf node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(StringLiteral node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(BoolLiteral node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ColorLiteral node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Identifier node) {
        if ((insideProcedure || insideScript) && !insideParameterList && !fixedBlock) {
            if (node.getName().startsWith(PARAMETER_ABBREVIATION) || node.getName().startsWith(VARIABLE_ABBREVIATION) || node.getName().startsWith(LIST_ABBREVIATION)) {
                count++;
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Never node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(StmtList node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ParameterDefinitionList node) {
        insideParameterList = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideParameterList = false;
    }

    @Override
    public void visit(ExpressionList node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Type node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ParameterDefiniton node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(AsNumber node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(AsString node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(AsBool node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(AsTouchable node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(AsListIndex node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ExpressionStmt node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(NumFunct node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(KeyPressed node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(SetAttributeTo node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(ChangeAttributeBy node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(Current node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(VariableAboveValue node) {
        if (insideScript || insideProcedure) {
            count++;
        }

        if (!node.getChildren().isEmpty()) {
            node.getValue().accept(this);
        }
    }

    @Override
    public void visit(HideVariable node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(ShowVariable node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(LengthOfVar node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        fixedBlock = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        fixedBlock = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(AddTo node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getString().accept(this);
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getNum().accept(this);
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getString().accept(this);
            node.getIndex().accept(this);
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getString().accept(this);
            node.getIndex().accept(this);
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getNum().accept(this);
        }
    }

    @Override
    public void visit(IndexOf node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(ExpressionContains node) {
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            //only expression has to be counted since the attributes are fixed in the blocks
            node.getContained().accept(this);
        }
    }

    @Override
    public void visit(LayerChoice node) {
    }
}

