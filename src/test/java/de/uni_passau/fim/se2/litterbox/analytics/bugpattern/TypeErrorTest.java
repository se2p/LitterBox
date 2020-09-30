package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class TypeErrorTest {
    private static Program empty;
    private static Program stringNumber;
    private static Program numberString;
    private static Program loudnessNumber;
    private static Program complex;
    private static Program motivation;
    private static Program booleanEquals;
    private static Program variable;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/stringComparedToNumber.json");
        stringNumber = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/complexeComparison.json");
        complex = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/compareLoudnessToNumber.json");
        loudnessNumber = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/motivation.json");
        motivation = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/numberComparedToString.json");
        numberString = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/redundantBooleanEquals.json");
        booleanEquals = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

    }

    @Test
    public void testEmptyProgram() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testStringComparedToNumber() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(stringNumber);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testNumberComparedToString() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(numberString);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testLoudnessComparedToNumber() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(loudnessNumber);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testComplexComparision() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(complex);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testMotivation() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(motivation);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testRedundantBooleanEquals() {
        TypeError parameterName = new TypeError();
        IssueReport report = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, report.getCount());
    }

}