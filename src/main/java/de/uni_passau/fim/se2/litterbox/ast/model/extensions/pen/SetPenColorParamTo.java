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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.PenOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetPenColorParamTo extends AbstractNode implements PenStmt {
    private final NumExpr value;
    private final ColorParam param;
    private final BlockMetadata metadata;

    public SetPenColorParamTo(NumExpr value, ColorParam param, BlockMetadata metadata) {
        super(value, param, metadata);
        this.value = value;
        this.param = param;
        this.metadata = metadata;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    public NumExpr getValue() {
        return value;
    }

    public ColorParam getParam() {
        return param;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof PenExtensionVisitor penExtensionVisitor) {
            penExtensionVisitor.visit(this);
        } else {
            visitor.visit((PenBlock) this);
        }
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(PenExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Opcode getOpcode() {
        return PenOpcode.pen_setPenColorParamTo;
    }

    public Opcode getMenuColorParamOpcode() {
        return DependentBlockOpcode.pen_menu_colorParam;
    }
}
