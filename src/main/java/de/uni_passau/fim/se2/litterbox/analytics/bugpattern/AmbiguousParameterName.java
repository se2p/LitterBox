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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;


/**
 * The parameter names in custom blocks do not have to be unique.
 * Therefore, when two parameters have the same name, no matter the type or which one is used inside the custom
 * block, it will always be evaluated as the last input to the block.
 */
public class AmbiguousParameterName extends AbstractIssueFinder {
    public static final String NAME = "ambiguous_parameter_name";
    public static final String SHORT_NAME = "ambParamName";
    public static final String HINT_TEXT = "ambiguous parameter name";

    private void checkArguments(ArgumentInfo[] arguments, ProcedureDefinition node) {
        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = i + 1; j < arguments.length; j++) {
                if (i != j && current.getName().equals(arguments[j].getName())) {
                    addIssue(node, HINT_TEXT, node.getMetadata().getDefinition());
                }
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        if (node.getStmtList().hasStatements()) {
            checkArguments(procMap.get(node.getIdent()).getArguments(), node);
        }

        visitChildren(node);
        currentProcedure = null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }
}
