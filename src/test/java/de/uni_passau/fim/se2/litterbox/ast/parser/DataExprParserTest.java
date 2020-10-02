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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.*;

class DataExprParserTest implements JsonTest {

    @Test
    public void testParseParam() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/parseParamInExprParser.json");
        List<ActorDefinition> actorDefinitions = program.getActorDefinitionList().getDefinitions();
        for (ActorDefinition actorDefinition : actorDefinitions) {
            if (actorDefinition.isSprite()) {
                List<Script> scriptList = actorDefinition.getScripts().getScriptList();
                Script script = scriptList.get(0);
                Stmt stmt = script.getStmtList().getStmts().get(0);
                assertTrue(stmt instanceof SetVariableTo);
                SetVariableTo setVariableTo = (SetVariableTo) stmt;
                Expression param = setVariableTo.getExpr();
                assertTrue(param instanceof Parameter);
                assertEquals(((Parameter) param).getName().getName(), "input");
                assertEquals(((Parameter) param).getMetadata().getClass(), NonDataBlockMetadata.class);
            }
        }
    }
}
