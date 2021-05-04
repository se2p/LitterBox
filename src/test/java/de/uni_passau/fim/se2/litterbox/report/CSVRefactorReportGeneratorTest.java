package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.AddHelloBlock;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DeleteControlBlock;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVRefactorReportGeneratorTest implements JsonTest {
    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");
    }

    @Test
    public void testSingleRefactoringSingleProjectNewCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        Refactoring r1 = new DeleteControlBlock(mock(ControlStmt.class));
        Refactoring r2 = new DeleteControlBlock(mock(ControlStmt.class));
        Refactoring r3 = new AddHelloBlock(mock(Script.class));

        when(refactorSequence.getExecutedRefactorings()).thenReturn(List.of(r1, r2, r3));

        Path tmpFile = Files.createTempFile("foo", "bar");
        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(tmpFile.toString(), refactorSequence.getExecutedRefactorings());
        reportGenerator.generateReport(program, refactorSequence);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("project,delete_control_block,add_hello_block");
        assertThat(lines.get(1)).isEqualTo("helloBlockHelloBlockWithinControl,2,1");
    }

    @Test
    public void testSingleIssueTwoProjectsAppendCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        Refactoring r1 = new DeleteControlBlock(mock(ControlStmt.class));
        Refactoring r2 = new DeleteControlBlock(mock(ControlStmt.class));
        Refactoring r3 = new AddHelloBlock(mock(Script.class));

        when(refactorSequence.getExecutedRefactorings()).thenReturn(List.of(r1, r2, r3));

        Path tmpFile = Files.createTempFile("foo", "bar");
        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(tmpFile.toString(), refactorSequence.getExecutedRefactorings());
        reportGenerator.generateReport(program, refactorSequence);
        reportGenerator.close();

        reportGenerator = new CSVRefactorReportGenerator(tmpFile.toString(), refactorSequence.getExecutedRefactorings());
        reportGenerator.generateReport(program, refactorSequence);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).isEqualTo("project,delete_control_block,add_hello_block");
        assertThat(lines.get(1)).isEqualTo("helloBlockHelloBlockWithinControl,2,1");
        assertThat(lines.get(1)).isEqualTo("helloBlockHelloBlockWithinControl,2,1");
    }
}
