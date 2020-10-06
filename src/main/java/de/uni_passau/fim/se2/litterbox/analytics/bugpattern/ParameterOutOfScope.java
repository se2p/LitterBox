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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

/**
 * The parameters of a custom block can be used anywhere inside the sprite that defines the custom block.
 * However, they will never be initialised outside the custom block, and will always have the default value.
 */
public class ParameterOutOfScope extends AbstractIssueFinder {
    public static final String NAME = "parameter_out_of_scope";
    private boolean insideProcedure;

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (!insideProcedure) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
