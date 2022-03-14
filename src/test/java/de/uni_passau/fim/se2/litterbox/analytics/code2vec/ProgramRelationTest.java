package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramRelationTest implements JsonTest {

    final static String NO_HASH_OUTPUT = "GreenFlag,(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral),Hello!";

    @Test
    void testSetNoHash() {
        ProgramRelation.setNoHash();
        ProgramRelation programRelation = new ProgramRelation("GreenFlag", "Hello!",
                "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)");
        assertEquals(NO_HASH_OUTPUT, programRelation.toString());
        ProgramRelation.sHasher = (s) -> Integer.toString(s.hashCode());
    }

    @Test
    void testToString() {
        ProgramRelation programRelation = new ProgramRelation("GreenFlag", "Hello!",
                "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)");
        assertEquals("GreenFlag,-2069003229,Hello!", programRelation.toString());
    }
}
