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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ExtractLeavesVisitor<T extends ASTNode> implements ScratchVisitor {
    private final ProcedureDefinitionNameMapping procedures;

    protected Map<T, List<ASTNode>> leavesMap = new HashMap<>();
    private List<ASTNode> leaves = new ArrayList<>();

    protected ExtractLeavesVisitor(final ProcedureDefinitionNameMapping procedures) {
        this.procedures = procedures;
    }

    /**
     * Marks a leaf for collection.
     *
     * <p>Note: To actually persist the leaf as part of a grouping, call {@link #saveLeaves(ASTNode)}.
     *
     * @param leaf Some leaf of the AST.
     */
    protected void addLeaf(final ASTLeaf leaf) {
        leaves.add(leaf);
    }

    /**
     * Saves all leaves added since the last save to the given group.
     *
     * @param group Some AST node the leaves belong to.
     */
    protected void saveLeaves(final T group) {
        leavesMap.put(group, leaves);
        leaves = new ArrayList<>();
    }

    @Override
    public void visit(final ProcedureDefinition node) {
        addLeaf(getProcedureName(node));

        node.getParameterDefinitionList().accept(this);
        node.getStmtList().accept(this);
    }

    /**
     * Obtains the name for a procedure.
     *
     * <p>The {@link ProcedureDefinition#getIdent()} contains only the Scratch block ID, rather than the proper name.
     * Therefore, we need to map that back to the user-defined actual name and wrap that in an AST node to be able to
     * collect it like other leaves.
     *
     * @param procedure Some custom block.
     * @return The name of the given procedure.
     */
    private StringLiteral getProcedureName(final ProcedureDefinition procedure) {
        final ProcedureInfo info = procedures.getProcedureInfo(procedure);

        final StringLiteral name = new StringLiteral(info.getName());
        name.setParentNode(procedure);

        return name;
    }

    public Map<T, List<ASTNode>> getLeaves() {
        return leavesMap;
    }
}
