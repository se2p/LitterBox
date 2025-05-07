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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public sealed interface QueryTarget {

    String getTargetDescription();

    ASTNode getTargetNode(Program program);

    record ProgramTarget() implements QueryTarget {
        @Override
        public String getTargetDescription() {
            return "program";
        }

        @Override
        public ASTNode getTargetNode(final Program program) {
            return program;
        }
    }

    record SpriteTarget(String spriteName) implements QueryTarget {

        public SpriteTarget {
            Preconditions.checkNotNull(spriteName);
        }

        @Override
        public String getTargetDescription() {
            return "sprite";
        }

        @Override
        public ASTNode getTargetNode(Program program) {
            return AstNodeUtil.getActors(program, false)
                    .filter(actor -> actor.getIdent().getName().equals(spriteName))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Could not find sprite '" + spriteName + "' in the program."
                            )
                    );
        }
    }

    record ScriptTarget(String headBlockId) implements QueryTarget {

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
