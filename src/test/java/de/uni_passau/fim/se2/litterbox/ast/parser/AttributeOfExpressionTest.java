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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import static junit.framework.TestCase.fail;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AttributeOfExpressionTest {
    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/attributeOfExpression.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testContains() throws ParsingException {
        Program program = ProgramParser.parseProgram("AttributeOf", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(0);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(0);
        Truth.assertThat(stmt).isInstanceOf(ExpressionStmt.class);

        ExpressionStmt expressionStmt = (ExpressionStmt) stmt;
        Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(AttributeOf.class);
        AttributeOf attributeOf = (AttributeOf) expressionStmt.getExpression();
        Truth.assertThat(attributeOf.getElementChoice()).isInstanceOf(WithExpr.class);
    }
}