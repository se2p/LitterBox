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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.*;

class LastLexicalUseVisitor extends GgnnDefineablesVisitor {

    private final Map<String, Expression> variableLastUse = new HashMap<>();
    private final Map<String, Expression> listsLastUse = new HashMap<>();
    private final Map<Class<? extends ASTNode>, ASTNode> attributesLastUse = new HashMap<>();

    private final List<Pair<ASTNode>> lastUses = new ArrayList<>();

    private LastLexicalUseVisitor() {
    }

    static List<Pair<ASTNode>> getEdges(final ASTNode astRoot) {
        final LastLexicalUseVisitor visitor = new LastLexicalUseVisitor();
        astRoot.accept(visitor);
        return Collections.unmodifiableList(visitor.lastUses);
    }

    @Override
    protected void addVariable(final Variable variable) {
        final Expression previousUse = variableLastUse.put(variable.getName().getName(), variable);
        connect(previousUse, variable);
    }

    @Override
    protected void addList(final ScratchList variable) {
        final Expression previousUse = listsLastUse.put(variable.getName().getName(), variable);
        connect(previousUse, variable);
    }

    @Override
    protected void addAttribute(final ASTNode attribute) {
        final ASTNode previousUse = attributesLastUse.put(attribute.getClass(), attribute);
        connect(previousUse, attribute);
    }

    private void connect(final ASTNode previous, final ASTNode current) {
        if (previous == null || current == null) {
            return;
        }

        lastUses.add(Pair.of(current, previous));
    }

    private void resetLastSeen() {
        variableLastUse.clear();
        listsLastUse.clear();
        attributesLastUse.clear();
    }

    @Override
    public void visit(Script node) {
        resetLastSeen();
        super.visit(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        resetLastSeen();
        super.visit(node);
    }
}
