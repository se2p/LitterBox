package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CollisionTest implements JsonTest {

    @Test
    public void testCollision() throws IOException, ParsingException {
        Program colProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/collistionInForever.json");
        Collision collision = new Collision();
        Set<Issue> reports = collision.check(colProg);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCollisionTwoFaked() throws IOException, ParsingException {
        Program colProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/collisiontwofaked.json");
        Collision collision = new Collision();
        Set<Issue> reports = collision.check(colProg);
        Assertions.assertEquals(1, reports.size());
    }
}
