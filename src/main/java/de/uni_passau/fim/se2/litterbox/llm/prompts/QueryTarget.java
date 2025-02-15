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

package de.uni_passau.fim.se2.litterbox.llm.prompts;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public record QueryTarget(String sprite, String script) {


    public ASTNode getTargetNode(Program program) {
        if (script != null) {
            return getScript(program);
        } else if (sprite != null) {
            return getSprite(program);
        } else {
            return program;
        }
    }

    public String getTargetDescription () {
        if (script != null) {
            return "script";
        } else if (sprite != null) {
            return "sprite";
        } else {
            return "program";
        }
    }

    public ActorDefinition getSprite(Program program) {
        Preconditions.checkNotNull(sprite);
        return AstNodeUtil.getActors(program, false)
                .filter(actor -> actor.getIdent().getName().equals(sprite))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Could not find sprite '" + sprite + "' in the program.")
                );
    }

    public Script getScript(Program program) {
        Preconditions.checkNotNull(script);
        return AstNodeUtil.getScripts(program)
                .filter(script -> AstNodeUtil.getBlockId(script.getEvent()).equals(script))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Could not find script with ID '" + script + "' in the program.")
                );
    }

}
