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

import java.util.ArrayList;
import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class ProcedureCount implements IssueFinder, ScratchVisitor {
    public static final String NAME = "procedure_count";
    public static final String SHORT_NAME = "procCnt";

    private int count = 0;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);

        return new IssueReport(NAME, count, new ArrayList<>(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public void visit(ProcedureDefinition node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
