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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * Visitor to collect all variable and list names referenced in scripts.
 *
 * <p>This visitor traverses the AST and collects the names of all variables
 * and lists that are referenced, including definitions, reads, writes, and
 * visibility changes.
 */
public class VariableReferenceCollector implements ScratchVisitor {
    private final Set<String> referencedVariables = new HashSet<>();

    /**
     * Returns the set of variable names found during traversal.
     *
     * @return Set of variable names
     */
    public Set<String> getReferencedVariables() {
        return referencedVariables;
    }

    @Override
    public void visit(SetVariableTo node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(ShowVariable node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(HideVariable node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(ShowList node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(HideList node) {
        addVariable(node.getIdentifier());
        visitChildren(node);
    }

    @Override
    public void visit(Variable node) {
        referencedVariables.add(node.getName().getName());
        visitChildren(node);
    }

    @Override
    public void visit(ScratchList node) {
        referencedVariables.add(node.getName().getName());
        visitChildren(node);
    }

    /**
     * Extracts and adds the variable name from an identifier.
     * Handles Variable, ScratchList, StrId, and Qualified identifiers.
     *
     * @param id The identifier to extract the name from
     */
    private void addVariable(Identifier id) {
        String name = extractVariableName(id);
        if (name != null) {
            referencedVariables.add(name);
        }
    }

    /**
     * Extracts the variable name from an identifier.
     *
     * @param id The identifier
     * @return The variable name, or null if not extractable
     */
    private String extractVariableName(Identifier id) {
        if (id instanceof Variable v) {
            return v.getName().getName();
        } else if (id instanceof ScratchList l) {
            return l.getName().getName();
        } else if (id instanceof StrId s) {
            return s.getName();
        } else if (id instanceof Qualified q) {
            return extractDataExprName(q.getSecond());
        }
        return null;
    }

    /**
     * Extracts the variable name from a data expression.
     *
     * @param expr The data expression
     * @return The variable name, or null if not extractable
     */
    private String extractDataExprName(DataExpr expr) {
        if (expr instanceof Variable v) {
            return v.getName().getName();
        } else if (expr instanceof ScratchList l) {
            return l.getName().getName();
        }
        return null;
    }
}
