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
package de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow;

import de.uni_passau.fim.se2.litterbox.analytics.bugs.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TypeJoinFunctionTest {

    private final TypeJoinFunction join = new TypeJoinFunction();
    private final Variable var1 = new Variable(new StrId("var1"));

    @Test
    public void testEmptyJoin() {
        Set<VariableTypeFact> result = join.apply(Collections.emptySet());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleBranch() {
        Set<VariableTypeFact> input = Set.of(new VariableTypeFact(var1, Type.NUMBER));
        Set<VariableTypeFact> result = join.apply(Collections.singleton(input));
        
        Assertions.assertEquals(1, result.size());
        VariableTypeFact fact = result.iterator().next();
        Assertions.assertEquals(var1, fact.getVariable());
        Assertions.assertEquals(Type.NUMBER, fact.getType());
    }

    @Test
    public void testConflict() {
        Set<VariableTypeFact> input1 = Set.of(new VariableTypeFact(var1, Type.NUMBER));
        Set<VariableTypeFact> input2 = Set.of(new VariableTypeFact(var1, Type.STRING));
        
        Set<Set<VariableTypeFact>> inputs = new HashSet<>();
        inputs.add(input1);
        inputs.add(input2); 
        
        Set<VariableTypeFact> result = join.apply(inputs);
        Assertions.assertEquals(1, result.size());
        VariableTypeFact fact = result.iterator().next();
        Assertions.assertEquals(Type.TOP, fact.getType());
    }

    @Test
    public void testAnyDominates() {
        Set<VariableTypeFact> input1 = Set.of(new VariableTypeFact(var1, Type.NUMBER));
        Set<VariableTypeFact> input2 = Set.of(new VariableTypeFact(var1, Type.ANY));
        
        Set<Set<VariableTypeFact>> inputs = new HashSet<>();
        inputs.add(input1);
        inputs.add(input2); 
        
        Set<VariableTypeFact> result = join.apply(inputs);
        Assertions.assertEquals(1, result.size());
        VariableTypeFact fact = result.iterator().next();
        Assertions.assertEquals(Type.ANY, fact.getType());
    }
}
