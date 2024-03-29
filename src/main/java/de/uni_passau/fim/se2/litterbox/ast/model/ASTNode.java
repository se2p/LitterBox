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
package de.uni_passau.fim.se2.litterbox.ast.model;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NOPCode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.Visitable;

import java.util.List;

public interface ASTNode extends Visitable<ASTNode> {

    List<? extends ASTNode> getChildren();

    boolean hasChildren();

    ASTNode getParentNode();

    void setParentNode(ASTNode node);

    String getUniqueName();

    BlockMetadata getMetadata();

    default String getScratchBlocks() {
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        this.accept(visitor);
        return visitor.getScratchBlocks();
    }

    default Opcode getOpcode() {
        return new NOPCode();
    }
}
