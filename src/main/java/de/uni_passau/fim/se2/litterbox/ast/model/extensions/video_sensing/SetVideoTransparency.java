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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.VideoSensingOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;

public class SetVideoTransparency extends AbstractNode implements VideoSensingStmt {

    private final NumExpr transparency;
    private final BlockMetadata metadata;

    public SetVideoTransparency(NumExpr transparency, BlockMetadata metadata) {
        super(transparency, metadata);
        this.transparency = transparency;
        this.metadata = metadata;
    }

    public NumExpr getTransparency() {
        return transparency;
    }

    @Override
    public Opcode getOpcode() {
        return VideoSensingOpcode.videoSensing_setVideoTransparency;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof VideoSensingExtensionVisitor videoSensingExtensionVisitor) {
            videoSensingExtensionVisitor.visit(this);
        } else {
            visitor.visit((VideoSensingBlock) this);
        }
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(VideoSensingExtensionVisitor visitor) {
        visitor.visit(this);
    }
}
