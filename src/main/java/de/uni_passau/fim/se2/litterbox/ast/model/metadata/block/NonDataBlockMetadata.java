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
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class NonDataBlockMetadata extends AbstractNode implements BlockMetadata {
    private String commentId;
    private String blockId;
    private String opcode;
    private String next;
    private String parent;
    private InputMetadataList inputMetadata;
    private FieldsMetadataList fields;
    private boolean topLevel;
    private boolean shadow;
    private MutationMetadata mutation;

    public NonDataBlockMetadata(String commentId, String blockId, String opcode, String next, String parent,
                                InputMetadataList inputMetadata, FieldsMetadataList fields, boolean topLevel,
                                boolean shadow, MutationMetadata mutation) {
        super(inputMetadata, fields, mutation);
        this.commentId = commentId;
        this.blockId = blockId;
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.inputMetadata = inputMetadata;
        this.fields = fields;
        this.topLevel = topLevel;
        this.shadow = shadow;
        this.mutation = mutation;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getNext() {
        return next;
    }

    public String getParent() {
        return parent;
    }

    public InputMetadataList getInputMetadata() {
        return inputMetadata;
    }

    public FieldsMetadataList getFields() {
        return fields;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public boolean isShadow() {
        return shadow;
    }

    public MutationMetadata getMutation() {
        return mutation;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
