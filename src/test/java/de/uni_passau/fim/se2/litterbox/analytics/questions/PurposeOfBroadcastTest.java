package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfBroadcastTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfBroadcast(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneBroadcastStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfBroadcast(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testTwoBroadcastStmtsDifferentMessages() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfBroadcast(), "src/test/fixtures/questions/twoBroadcastDifferentMessage.json");
    }

    @Test
    public void testBroadcastStmtsSameMessage() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfBroadcast(), "src/test/fixtures/questions/twoBroadcastSameMessage.json");
    }

    @Test
    public void testMultipleBroadcastStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfBroadcast(), "src/test/fixtures/questions/eventsTriggeredByStatements.json");
    }
}
