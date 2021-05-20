package de.uni_passau.fim.se2.litterbox.dependency;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class ProgramDependencyGraphTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);

        assertThat(tdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(tdg.getNumEdges()).isEqualTo(0);
    }
}
