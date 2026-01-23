/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds all {@link Variable Variables} and attributes that are used in the AST.
 */
class DefineableUsesVisitor extends GgnnDefineablesVisitor {

    private final Map<String, List<Variable>> variables = new HashMap<>();
    private final Map<String, List<ScratchList>> lists = new HashMap<>();
    private final List<ASTNode> attributes = new ArrayList<>();

    private DefineableUsesVisitor() {
    }

    public static DefineableUsesVisitor visitNode(final ASTNode node) {
        DefineableUsesVisitor v = new DefineableUsesVisitor();
        node.accept(v);
        return v;
    }

    public Map<String, List<Variable>> getVariables() {
        return variables;
    }

    public Map<String, List<ScratchList>> getLists() {
        return lists;
    }

    public List<ASTNode> getAttributes() {
        return attributes;
    }

    @Override
    protected void addVariable(final Variable variable) {
        variables.compute(variable.getName().getName(), (name, vars) -> {
            if (vars == null) {
                vars = new ArrayList<>();
            }
            vars.add(variable);
            return vars;
        });
    }

    @Override
    protected void addList(final ScratchList variable) {
        lists.compute(variable.getName().getName(), (name, vars) -> {
            if (vars == null) {
                vars = new ArrayList<>();
            }
            vars.add(variable);
            return vars;
        });
    }

    @Override
    protected void addAttribute(ASTNode node) {
        attributes.add(node);
    }
}
