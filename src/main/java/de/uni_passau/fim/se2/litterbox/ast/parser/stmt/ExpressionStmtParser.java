/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;

import static de.uni_passau.fim.se2.litterbox.ast.parser.DataExprParser.parseDeadParameter;

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
     *       reporter block.
     * @throws ParsingException If the block is not parsable.
     */
    public static Stmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new ExpressionStmt(ExpressionParser.parseExprBlock(blockId, current, allBlocks));
    }

    /**
     * Parses a dead parameter which is not inside of a script and wraps it into an ExpressionStmt.
     *
     * @param blockId The id of the param node.
     * @param paramNode The node holding the name of the parameter.
     * @return The parameter corresponding to the param node wrapped into an ExpressionStmt.
     * @throws ParsingException If parsing the metadata fails.
     */
    public static ExpressionStmt parseParameter(String blockId, JsonNode paramNode) throws ParsingException {
        Parameter parameter = parseDeadParameter(blockId, paramNode);
        return new ExpressionStmt(parameter);
    }
}
