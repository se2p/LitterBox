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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramFeaturesTest {
    ProgramFeatures cat;

    @BeforeEach
    void setUp() {
        ProgramRelation.setDefaultHasher();

        cat = new ProgramFeatures("cat");
        cat.addFeature("39.0",
                "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Say)_(StringLiteral)",
                "Hi!");
        cat.addFeature("39.0",
                "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Show)",
                "Show");
        cat.addFeature("Hi!",
                "(StringLiteral)^(Say)^(StmtList)_(Show)",
                "Show");
    }

    @Test
    void testToString() {
        assertEquals("cat 39.0,625791294,Hi! 39.0," +
                "1493538624,Show Hi!,-547448667,Show", cat.toString());
    }

    @Test
    void testAddFeature() {
        assertEquals(3, cat.getFeatures().size());
    }

    @Test
    void testIsEmpty() {
        ProgramFeatures programFeatures = new ProgramFeatures("abby");
        assertTrue(programFeatures.isEmpty());
    }

    @Test
    void testGetName() {
        assertEquals("cat", cat.getName());
    }

    @Test
    void testGetFeatures() {
        List<ProgramRelation> features = cat.getFeatures();
        assertEquals(3, features.size());
        assertEquals("39.0,625791294,Hi!", features.get(0).toString());
        assertEquals("39.0,1493538624,Show", features.get(1).toString());
        assertEquals("Hi!,-547448667,Show", features.get(2).toString());
    }
}
