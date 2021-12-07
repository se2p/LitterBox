package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ProgramFeaturesTest {
    ProgramFeatures cat;

    @BeforeEach
    void setUp() {
        cat = new ProgramFeatures("cat");
        cat.addFeature("NumberLiteral",
                "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Say)_(StringLiteral)",
                "StringLiteral");
        cat.addFeature("NumberLiteral",
                "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Show)",
                "Show");
        cat.addFeature("StringLiteral",
                "(StringLiteral)^(Say)^(StmtList)_(Show)",
                "Show");
    }

    @Test
    void testToString() {
        assertEquals("cat NumberLiteral,625791294,StringLiteral NumberLiteral," +
                "1493538624,Show StringLiteral,-547448667,Show", cat.toString());
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
        ArrayList<ProgramRelation> features = cat.getFeatures();
        assertEquals(3, features.size());
        assertEquals("NumberLiteral,625791294,StringLiteral", features.get(0).toString());
        assertEquals("NumberLiteral,1493538624,Show", features.get(1).toString());
        assertEquals("StringLiteral,-547448667,Show", features.get(2).toString());
    }
}
