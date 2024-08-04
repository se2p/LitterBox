//package de.uni_passau.fim.se2.litterbox.analytics.questions;
//
//import de.uni_passau.fim.se2.litterbox.JsonTest;
//import de.uni_passau.fim.se2.litterbox.analytics.Issue;
//import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
//import de.uni_passau.fim.se2.litterbox.ast.model.Program;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.Set;
//import java.util.regex.Pattern;
//
//import static com.google.common.truth.Truth.assertThat;
//
//class ScriptToSetVariableTest implements JsonTest {
//
//    @Test
//    public void testEmptyProgram() throws IOException, ParsingException {
//        assertThatFinderReports(0, new ScriptToSetVariable(), "./src/test/fixtures/emptyProject.json");
//    }
//
//    @Test
//    public void testNoSetVariable() throws IOException, ParsingException {
//        assertThatFinderReports(0, new ScriptToSetVariable(), "./src/test/fixtures/questions/oneScriptWithForeverLoopOneWithOtherElement.json");
//    }
//
//    @Test
//    public void testOneSetVariableInOneScript() throws IOException, ParsingException {
//        assertThatFinderReports(1, new ScriptToSetVariable(), "./src/test/fixtures/questions/oneVariableAppearsInTwoSeparateScripts.json");
//    }
//
//    @Test
//    public void testThreeSetVariableInOneScript() throws IOException, ParsingException {
//        assertThatFinderReports(1, new ScriptToSetVariable(), "./src/test/fixtures/questions/fourScriptsThreeWithVariables.json");
//    }
//
//    @Test
//    public void testSetVariableInThreeScripts() throws IOException, ParsingException {
//        assertThatFinderReports(3, new ScriptToSetVariable(), "./src/test/fixtures/questions/threeScriptsWithSetVariable.json");
//    }
//
//    @Test
//    public void testLimitChoices() throws IOException, ParsingException {
//        Program prog = getAST("src/test/fixtures/questions/eventsTriggeredByStatements.json");
//        Set<Issue> issues = runFinder(prog, new ScriptToSetVariable(), false);
//
//        for (Issue issue : issues) {
//            String hint = issue.getHint();
//            String choices = "\\[choices](\\[scratchblocks](?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\|){3}\\[scratchblocks](?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\[/choices]";
//            String answer = "\\[solutions]\\[scratchblocks](?:(?!\\[scratchblocks]).)*set \\[my variable v] to \\(0\\)(?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\[/solutions]";
//            assertThat(hint).containsMatch(Pattern.compile(choices, Pattern.DOTALL));
//            assertThat(hint).containsMatch(Pattern.compile(answer, Pattern.DOTALL));
//        }
//    }
//}
