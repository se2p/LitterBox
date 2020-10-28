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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.META_KEY;

public class MetaMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMeta() throws IOException {
        File f = new File("./src/test/fixtures/metadata/metaExtensionMonitorData.json");
        JsonNode prog = mapper.readTree(f);
        MetaMetadata meta = MetaMetadataParser.parse(prog.get(META_KEY));
        Assertions.assertEquals("0.2.0-prerelease.20200402182733", meta.getVm());
        Assertions.assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0",
                meta.getAgent());
        Assertions.assertEquals("3.0.0", meta.getSemver());
    }
}
