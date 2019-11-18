/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.ast.model.resource;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.ast.model.ASTNode;
import scratch.ast.visitor.ScratchVisitor;

public class ResourceList implements ASTNode {

    List<Resource> resourceList;
    private final ImmutableList<ASTNode> children;

    public ResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
        children = ImmutableList.<ASTNode>builder().addAll(resourceList).build();
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}
