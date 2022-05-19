package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GenerateGraphTaskTest implements JsonTest {
     @ParameterizedTest
     @ValueSource(booleans = {true, false})
     void testGraphEmptyProgram(boolean wholeProgram) throws Exception {
         Path inputPath = Path.of("src", "test", "fixtures", "emptyProject.json");
         Program program = getAST(inputPath.toString());
         GenerateGraphTask graphTask = new GenerateGraphTask(program, inputPath, false, wholeProgram, null);

         List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
         assertThat(graphs).hasSize(1);

         int expectedNodeCount;
         if (wholeProgram) {
             expectedNodeCount = 18;
         } else {
             expectedNodeCount = 8;
         }
         assertThat(graphs.get(0).getContextGraph().getNodeLabels()).hasSize(expectedNodeCount);
     }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphWholeProgram(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, inputPath, includeStage, true, null);

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(1);

        String graphJsonl = graphTask.generateJsonGraphData(graphs);
        assertThat(graphJsonl.lines().collect(Collectors.toList())).hasSize(1);
    }
}
