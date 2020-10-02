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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

public class CallMutationMetadata extends AbstractNode implements MutationMetadata, ASTLeaf {
    private String tagName;
    private List<String> children;
    private String procCode;
    private List<String> argumentIds;
    private boolean warp;

    public CallMutationMetadata(String tagName, List<String> children, String procCode, List<String> argumentIds,
                                boolean warp) {
        super();
        this.tagName = tagName;
        this.children = children;
        this.procCode = procCode;
        this.argumentIds = argumentIds;
        this.warp = warp;
    }

    public CallMutationMetadata(CallMutationMetadata orig) {
        this(orig.tagName, new ArrayList<>(orig.children), orig.procCode, new ArrayList<>(orig.argumentIds), orig.warp);
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getChild() {
        return children;
    }

    public String getProcCode() {
        return procCode;
    }

    public List<String> getArgumentIds() {
        return argumentIds;
    }

    public boolean isWarp() {
        return warp;
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
