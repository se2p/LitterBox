package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.analytics.smells.DeadCode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.ALL;
import static org.junit.jupiter.api.Assertions.fail;

public class GeneralTestMBlock implements JsonTest {

    @Test
    public void testSmallAST() throws IOException {
        Program program = null;
        try {
            program = getAST("./src/test/fixtures/mblock/small_ast.json");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        Set<Issue> issues = runFinder(program, new DeadCode(), false);
        Assertions.assertTrue(issues.isEmpty());
    }

    @Test
    public void testAllIssueFinders() throws IOException {
        List<IssueFinder> issueFinders = IssueTool.getFinders(ALL);
        Program program = null;
        try {
            program = getAST("./src/test/fixtures/mblock/mBlock_all_variants.json");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        if (LOAD_GENERAL || LOAD_MBLOCK) {
            Set<Issue> issues = runFinders(program, issueFinders, false);
            Assertions.assertNotNull(program);
            Assertions.assertNotNull(issues);
            if (LOAD_GENERAL) {
                Assertions.assertTrue(issues.stream().anyMatch(c -> c.getFinder() instanceof DeadCode));
            }
        }
    }
}
