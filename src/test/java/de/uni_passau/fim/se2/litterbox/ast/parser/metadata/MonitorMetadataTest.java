/*
 * Copyright (C) 2020 LitterBox contributors
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.MONITORS_KEY;

public class MonitorMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;
    private static JsonNode monitorMetaAlternative;
    private static JsonNode monitorMetaManipulated;
    private static JsonNode sliderMetadata;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/monitorMeta.json");
        prog = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/monitorMetaAlternative.json");
        monitorMetaAlternative = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/monitorMetaManipulated.json");
        monitorMetaManipulated = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/sliderMetadata.json");
        sliderMetadata = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(empty.get(MONITORS_KEY));
        Assertions.assertEquals(2, monitors.getList().size());
    }

    @Test
    public void testMonitorsProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(prog.get(MONITORS_KEY));
        Assertions.assertEquals(4, monitors.getList().size());
        Assertions.assertTrue(monitors.getList().get(0) instanceof MonitorListMetadata);
        Assertions.assertTrue(monitors.getList().get(1) instanceof MonitorSliderMetadata);
        Assertions.assertTrue(monitors.getList().get(2) instanceof MonitorSliderMetadata);
        Assertions.assertTrue(monitors.getList().get(3) instanceof MonitorSliderMetadata);
    }

    @Test
    public void testMonitorsAlternativeProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(monitorMetaAlternative.get(MONITORS_KEY));
        Assertions.assertEquals(1, monitors.getList().size());
    }

    @Test
    public void testMonitorsManipulatedProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(monitorMetaManipulated.get(MONITORS_KEY));
        Assertions.assertEquals(1, monitors.getList().size());
        Assertions.assertTrue(monitors.getList().get(0) instanceof MonitorSliderMetadata);
        Assertions.assertFalse(monitors.getList().get(0).isVisible());
        Assertions.assertEquals(0, ((MonitorSliderMetadata) monitors.getList().get(0)).getSliderMin());
    }

    @Test
    public void testSlider() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(sliderMetadata.get(MONITORS_KEY));
        MonitorSliderMetadata monitorSliderMetadata = (MonitorSliderMetadata) monitors.getList().get(0);
        Assertions.assertEquals(monitorSliderMetadata.getSliderMin(), 0);
        Assertions.assertEquals(monitorSliderMetadata.getSliderMax(), 100);
        Assertions.assertEquals(monitorSliderMetadata.getValue(), "1000");
        Assertions.assertEquals(monitorSliderMetadata.isDiscrete(), true);
    }
}
