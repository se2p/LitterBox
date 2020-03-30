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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeAttributeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.ChangePenColorParamBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStampStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.SetPenColorParamTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.PEN_SIZE_KEY;

public class ProgramUsingPen implements IssueFinder, ScratchVisitor {
    public static final String NAME = "using_pen";
    public static final String SHORT_NAME = "usingPen";
    private boolean found = false;
    private List<String> actorNames = new LinkedList<>();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        actorNames = new LinkedList<>();
        program.accept(this);
        int count = 0;
        if (found) {
            count = 1;
        }
        return new IssueReport(NAME, count, actorNames, "");
    }

    @Override
    public void visit(PenDownStmt node) {
        found = true;
    }

    @Override
    public void visit(PenUpStmt node) {
        found = true;
    }

    @Override
    public void visit(PenClearStmt node) {
        found = true;
    }

    @Override
    public void visit(SetAttributeTo node) {
        if (node.getStringExpr().equals(new StringLiteral(PEN_SIZE_KEY))) {
            found = true;
        } else {
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        found = true;
    }

    @Override
    public void visit(PenStampStmt node) {
        found = true;
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        found = true;
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        found = true;
    }

    @Override
    public void visit(ChangeAttributeBy node) {
        if (node.getAttribute().equals(new StringLiteral(PEN_SIZE_KEY))) {
            found = true;
        } else {
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }
    }
}
