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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;

/**
 * This parser parses expressions, in this case reporter blocks, which are not
 * embedded in regular statements. In the AST they are represented embedded
 * in an ExpressionStmt.
 */
public class ExpressionStmtParser {

    /**
     * Parses a single reporter block and puts it into a new ExpressionStmt.
     *
     * @param current   The JsonNode of the reporter block.
     * @param allBlocks The JsonNode holding all blocks of the actor definition currently analysed.
     * @return A new ExpressionStmt holding the expression corresponding to the
     * reporter block.
     * @throws ParsingException If the block is not parsable.
     */
    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new ExpressionStmt(ExpressionParser.parseExprBlock(current, allBlocks));
    }
}
