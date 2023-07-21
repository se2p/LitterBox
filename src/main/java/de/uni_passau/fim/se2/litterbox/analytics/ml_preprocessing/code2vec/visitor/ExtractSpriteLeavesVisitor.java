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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.visitor;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.*;

public class ExtractSpriteLeavesVisitor implements ScratchVisitor {
    private final Map<ActorDefinition, List<ASTNode>> leavesMap = new HashMap<>();
    private final boolean includeStage;

    private boolean insideActor = false;
    private List<ASTNode> leaves;

    public ExtractSpriteLeavesVisitor(boolean includeStage) {
        this.includeStage = includeStage;
    }

    public Map<ActorDefinition, List<ASTNode>> getLeavesCollector() {
        return leavesMap;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (!shouldActorBeIncluded(node)) {
            return;
        }

        insideActor = true;

        leaves = new ArrayList<>();
        // ToDo fix in follow-up: enable and handle procedure names
        //  Without further changes the internal Scratch block-ID is used.
        // node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);
        leavesMap.put(node, leaves);

        insideActor = false;
    }

    @Override
    public void visit(ASTNode node) {
        if (insideActor && node instanceof ASTLeaf && !AstNodeUtil.isMetadata(node)) {
            leaves.add(node);
        } else {
            visitChildren(node);
        }
    }

    private boolean shouldActorBeIncluded(ActorDefinition actor) {
        return actor.isSprite() || (includeStage && actor.isStage());
    }
}
