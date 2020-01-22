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
package analytics.utils;

import static scratch.ast.Constants.*;


import java.util.LinkedList;
import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.StmtList;
import scratch.ast.model.event.Never;
import scratch.ast.model.expression.list.ExpressionList;
import scratch.ast.model.expression.list.ExpressionListPlain;
import scratch.ast.model.literals.BoolLiteral;
import scratch.ast.model.literals.ColorLiteral;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.procedure.Parameter;
import scratch.ast.model.procedure.ParameterList;
import scratch.ast.model.procedure.ParameterListPlain;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.statement.CallStmt;
import scratch.ast.model.statement.spritelook.ListOfStmt;
import scratch.ast.model.type.Type;
import scratch.ast.model.variable.Identifier;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class BlockCount implements IssueFinder, ScratchVisitor {
    public static final String NAME = "block_count";
    public static final String SHORT_NAME = "blockCnt";
    private int count = 0;
    private boolean insideScript = false;
    private boolean insideProcedure = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        insideScript = false;
        insideProcedure = false;
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
        if (insideScript || insideProcedure) {
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
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
        if (insideProcedure || insideScript) {
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
    public void visit(ListOfStmt node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ParameterList node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ParameterListPlain node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
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
    public void visit(ExpressionListPlain node) {
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
    public void visit(CallStmt node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Parameter node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
