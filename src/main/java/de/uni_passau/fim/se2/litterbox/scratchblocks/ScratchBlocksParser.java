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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksGrammarLexer;
import de.uni_passau.fim.se2.litterbox.generated.ScratchBlocksGrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ScratchBlocksParser {

    // todo: probably similar methods for whole actors and programs?

    public ScriptEntity parseScript(final String scratchBlocksCode) {
        final ScratchBlocksGrammarParser parser = buildParser(scratchBlocksCode);
        final ParseTree tree = parser.script();

        final ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        final ASTNode node = vis.visit(tree);

        if (node instanceof ScriptEntity script) {
            return script;
        } else {
            throw new IllegalArgumentException(
                    "Could not parse ScratchBlocks code as script. Got: " + node.getClass().getSimpleName()
            );
        }
    }

    private ScratchBlocksGrammarParser buildParser(final String scratchBlocks) {
        final ScratchBlocksGrammarLexer lexer = new ScratchBlocksGrammarLexer(CharStreams.fromString(scratchBlocks));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ScratchBlocksGrammarParser(tokens);
    }
}
