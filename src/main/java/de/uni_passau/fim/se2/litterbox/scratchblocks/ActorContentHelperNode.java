/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

/**
 * Virtual helper AST node for the ScratchBlocks parser.
 *
 * <p>Implementation note: intentionally package-private since this type of node does not fully implement everything
 * required to get a regular {@link ASTNode}. See method-level documentation for more information.
 *
 * @param scripts The scripts in an actor.
 * @param procedures The procedure definitions in an actor.
 */
record ActorContentHelperNode(ScriptList scripts, ProcedureDefinitionList procedures) implements ASTNode {

    /**
     * Since the visitor class does not know about this helper type, uses {@link ScratchVisitor#visit(ASTNode)}.
     * @param visitor A Scratch visitor.
     */
    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((ASTNode) visitor);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        ScriptList s = (ScriptList) visitor.visit(scripts);
        ProcedureDefinitionList p = (ProcedureDefinitionList) visitor.visit(procedures);
        return new ActorContentHelperNode(s, p);
    }

    @Override
    public List<? extends ASTNode> getChildren() {
        return List.of(scripts, procedures);
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public ASTNode getParentNode() {
        return null;
    }

    /**
     * No-op.
     * @param node Some AST node.
     */
    @Override
    public void setParentNode(ASTNode node) {
        // do nothing
    }

    @Override
    public String getUniqueName() {
        return "ActorContent";
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }
}
