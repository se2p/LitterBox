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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AttributeAboveValueTest implements JsonTest {
    @Test
    public void testEvent() throws IOException, ParsingException {
        Program project = getAST("src/test/fixtures/valueAbove.json");
        Assertions.assertTrue(project.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getEvent() instanceof AttributeAboveValue);
        Assertions.assertEquals("loudness",
                ((AttributeAboveValue) project.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getEvent()).getAttribute().getTypeName());
        Assertions.assertTrue(project.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1).getEvent() instanceof AttributeAboveValue);
        Assertions.assertEquals("timer",
                ((AttributeAboveValue) project.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1).getEvent()).getAttribute().getTypeName());
    }
}
