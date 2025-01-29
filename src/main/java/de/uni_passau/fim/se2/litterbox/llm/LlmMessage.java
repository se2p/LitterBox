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
package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.Collections;
import java.util.Map;

public interface LlmMessage {

    String text();

    LlmMessageSender sender();

    default Map<String, ASTNode> relevantCode() {
        return Collections.emptyMap();
    }

    record GenericLlmMessage(
            String text,
            LlmMessageSender sender,
            Map<String, ASTNode> relevantCode
    ) implements LlmMessage {
        public GenericLlmMessage(String text, LlmMessageSender sender) {
            this(text, sender, Collections.emptyMap());
        }
    }

    // todo: other specific types for specific messages that have proper attributes for specific code snippets,
    //  eg the fixed program rather than only a generic map with arbitrary identifiers as keys?
}
