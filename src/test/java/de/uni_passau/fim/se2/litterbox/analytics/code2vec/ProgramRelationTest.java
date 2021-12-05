package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ProgramRelationTest implements JsonTest {

    final String NO_HASH_OUTPUT = "GreenFlag,(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral),StringLiteral";

    @Test
    void testSetNoHash() {
        ProgramRelation.setNoHash();
        ProgramRelation programRelation = new ProgramRelation("GreenFlag", "StringLiteral",
                "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)");
        assertEquals(NO_HASH_OUTPUT, programRelation.toString());
    }

    @Test
    void testToString() {
        ProgramRelation programRelation = new ProgramRelation("GreenFlag", "StringLiteral",
                "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)");
        assertEquals("GreenFlag,-2069003229,StringLiteral", programRelation.toString());
    }
}
