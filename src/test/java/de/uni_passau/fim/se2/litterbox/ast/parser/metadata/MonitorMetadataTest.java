/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MonitorMetadataTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        MonitorMetadataList monitors = program.getProgramMetadata().getMonitor();

        Assertions.assertEquals(2, monitors.getList().size());
    }

    @Test
    public void testMonitorsProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/metadata/monitorMeta.json");
        MonitorMetadataList monitors = program.getProgramMetadata().getMonitor();

        Assertions.assertEquals(4, monitors.getList().size());
        Assertions.assertInstanceOf(MonitorListMetadata.class, monitors.getList().get(0));
        Assertions.assertInstanceOf(MonitorSliderMetadata.class, monitors.getList().get(1));
        Assertions.assertInstanceOf(MonitorSliderMetadata.class, monitors.getList().get(2));
        Assertions.assertInstanceOf(MonitorSliderMetadata.class, monitors.getList().get(3));
    }

    @Test
    public void testMonitorsAlternativeProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/metadata/monitorMetaAlternative.json");
        MonitorMetadataList monitors = program.getProgramMetadata().getMonitor();

        Assertions.assertEquals(1, monitors.getList().size());
    }

    @Test
    public void testMonitorsManipulatedProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/metadata/monitorMetaManipulated.json");
        MonitorMetadataList monitors = program.getProgramMetadata().getMonitor();

        Assertions.assertEquals(1, monitors.getList().size());
        Assertions.assertInstanceOf(MonitorSliderMetadata.class, monitors.getList().get(0));
        Assertions.assertFalse(monitors.getList().get(0).isVisible());
        Assertions.assertEquals(0, ((MonitorSliderMetadata) monitors.getList().get(0)).getSliderMin());
    }

    @Test
    public void testSlider() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/metadata/sliderMetadata.json");
        MonitorMetadataList monitors = program.getProgramMetadata().getMonitor();
        MonitorSliderMetadata monitorSliderMetadata = (MonitorSliderMetadata) monitors.getList().get(0);

        Assertions.assertEquals(monitorSliderMetadata.getSliderMin(), 0);
        Assertions.assertEquals(monitorSliderMetadata.getSliderMax(), 100);
        Assertions.assertEquals(monitorSliderMetadata.getValue(), "1000");
        Assertions.assertTrue(monitorSliderMetadata.isDiscrete());
    }
}
