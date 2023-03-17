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
package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

public class CostumeNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "costume_names";
    private static List<String> names;
    private boolean inActor;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        program.accept(this);
        return names;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (node.isSprite()) {
            inActor = true;
        }
        visitChildren(node);
        inActor = false;
    }

    @Override
    public void visit(ImageMetadata node) {
        if (inActor) {
            names.add(node.getName());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
