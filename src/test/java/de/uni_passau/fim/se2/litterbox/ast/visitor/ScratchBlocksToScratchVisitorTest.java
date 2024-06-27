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

import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarLexer;
import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarParser;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ScratchBlocksToScratchVisitorTest {

    @Test
    public void test() throws IOException {
        ScratchBlocksGrammarLexer lexer = new ScratchBlocksGrammarLexer(CharStreams.fromString("move (10) steps \n"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScratchBlocksGrammarParser parser = new ScratchBlocksGrammarParser(tokens);
        ParseTree tree = parser.actor();

        ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        ASTNode node = vis.visit(tree);
        System.out.println(node);
    }
}
