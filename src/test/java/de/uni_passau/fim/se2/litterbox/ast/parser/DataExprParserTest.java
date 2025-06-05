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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DataExprParserTest implements JsonTest {

    @Test
    void testParseParam() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/parseParamInExprParser.json");
        List<ActorDefinition> actorDefinitions = program.getActorDefinitionList().getDefinitions();
        for (ActorDefinition actorDefinition : actorDefinitions) {
            if (actorDefinition.isSprite()) {
                List<Script> scriptList = actorDefinition.getScripts().getScriptList();
                Script script = scriptList.get(0);
                Stmt stmt = script.getStmtList().getStmts().get(0);
                assertInstanceOf(SetVariableTo.class, stmt);
                SetVariableTo setVariableTo = (SetVariableTo) stmt;
                Expression param = setVariableTo.getExpr();
                assertInstanceOf(Parameter.class, param);
                assertEquals("input", ((Parameter) param).getName().getName());
                assertInstanceOf(NonDataBlockMetadata.class, param.getMetadata());
            }
        }
    }

    @Test
    void testParseNonTopLevelVariable() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/nonTopLevelVariable.json");
        final List<Variable> variables = NodeFilteringVisitor.getBlocks(program, Variable.class);
        assertThat(variables).isNotEmpty();

        final Variable onlineModeVar = variables.stream()
                .filter(variable -> "Online Mode?".equals(variable.getName().getName()))
                .filter(variable -> variable.getMetadata() instanceof DataBlockMetadata)
                .findFirst()
                .orElseThrow();
        final DataBlockMetadata varMetadata = (DataBlockMetadata) onlineModeVar.getMetadata();
        assertThat(varMetadata.getX()).isEqualTo(0);
        assertThat(varMetadata.getY()).isEqualTo(0);
    }

    @Test
    void testParseVariableNullID() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/variablesWithNullId.json");
        final List<Variable> variables = NodeFilteringVisitor.getBlocks(program.getActorDefinitionList().getDefinitions().get(1).getScripts(), Variable.class);
        assertThat(variables).isNotEmpty();
        Assertions.assertEquals(2, variables.size());
    }
}
