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
package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "list_names";
    private static List<String> names;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        Collection<ExpressionListInfo> variables = program.getSymbolTable().getLists().values();
        for (ExpressionListInfo variable : variables) {
            names.add(variable.getVariableName());
        }
        return names;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
