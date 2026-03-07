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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Represents a target for code operations such as export or LLM queries.
 *
 * <p>Can target the entire program, a specific sprite, or a specific script.
 */
public sealed interface CodeTarget {

    /**
     * Returns a human-readable description of the target type.
     *
     * @return Target description (e.g., "program", "sprite", "script")
     */
    String getTargetDescription();

    /**
     * Resolves the target AST node from the given program.
     *
     * @param program The program to search within
     * @return The resolved AST node
     * @throws IllegalArgumentException if the target cannot be found
     */
    ASTNode getTargetNode(Program program);

    /**
     * Targets the entire program.
     */
    record ProgramTarget() implements CodeTarget {
        @Override
        public String getTargetDescription() {
            return "program";
        }

        @Override
        public ASTNode getTargetNode(final Program program) {
            return program;
        }
    }

    /**
     * Targets a specific sprite by name.
     *
     * @param spriteName The name of the sprite to target
     */
    record SpriteTarget(String spriteName) implements CodeTarget {

        public SpriteTarget {
            Preconditions.checkNotNull(spriteName);
        }

        @Override
        public String getTargetDescription() {
            return "sprite";
        }

        @Override
        public ASTNode getTargetNode(Program program) {
            return AstNodeUtil.getActors(program, true)
                    .filter(actor -> actor.getIdent().getName().equals(spriteName))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Could not find sprite '" + spriteName + "' in the program."
                            )
                    );
        }
    }

    /**
     * Targets a specific script by its head block ID.
     *
     * @param headBlockId The ID of the script's head block
     */
    record ScriptTarget(String headBlockId) implements CodeTarget {

        public ScriptTarget {
            Preconditions.checkNotNull(headBlockId);
        }

        @Override
        public String getTargetDescription() {
            return "script";
        }

        @Override
        public ASTNode getTargetNode(Program program) {
            return AstNodeUtil.getScriptEntities(program)
                    .filter(script -> headBlockId.equals(AstNodeUtil.getBlockId(script)))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Could not find script with ID '" + headBlockId + "' in the program."
                            )
                    );
        }
    }
}
