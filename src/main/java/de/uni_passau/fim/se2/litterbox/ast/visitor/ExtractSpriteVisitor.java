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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.*;

public class ExtractSpriteVisitor implements ScratchVisitor {
    private final Map<ActorDefinition, List<ASTNode>> leafsMap = new HashMap<>();
    private final boolean includeStage;

    public ExtractSpriteVisitor(boolean includeStage) {
        this.includeStage = includeStage;
    }

    public Map<ActorDefinition, List<ASTNode>> getLeafsCollector() {
        return leafsMap;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (shouldActorBeIncluded(node)) {
            List<ASTNode> leafsCollector = new LinkedList<>();

            traverseLeafs(node.getScripts(), leafsCollector);
            leafsMap.put(node, leafsCollector);
        }
    }

    private boolean shouldActorBeIncluded(ActorDefinition actor) {
        return actor.isSprite() || (includeStage && actor.isStage());
    }

    private void traverseLeafs(ASTNode node, List<ASTNode> leafsCollector) {
        if (node instanceof ASTLeaf) {
            leafsCollector.add(node);
        }
        for (ASTNode child : node.getChildren()) {
            //Metadata such as code position in the editor are irrelevant for the path contexts
            if (child instanceof Metadata) {
                continue;
            }

            traverseLeafs(child, leafsCollector);
        }
    }
}
