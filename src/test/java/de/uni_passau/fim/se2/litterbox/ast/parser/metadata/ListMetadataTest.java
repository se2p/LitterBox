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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ListMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.LISTS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class ListMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        JsonNode empty = mapper.readTree(f);
        ListMetadataList monitors = ListMetadataListParser.parse(empty.get(TARGETS_KEY).get(0)
                .get(LISTS_KEY));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testListsProgram() throws IOException {
        File f = new File("./src/test/fixtures/metadata/monitorMeta.json");
        JsonNode prog = mapper.readTree(f);
        ListMetadataList monitors = ListMetadataListParser.parse(prog.get(TARGETS_KEY).get(0)
                .get(LISTS_KEY));
        Assertions.assertEquals(1, monitors.getList().size());
        Assertions.assertEquals("h|7mn11vxAw-d7dTt,_/", monitors.getList().get(0).getListId());
        Assertions.assertEquals("test", monitors.getList().get(0).getListName());
        List<String> list = new ArrayList<>();
        list.add("bla");
        list.add("test");
        Assertions.assertEquals(list, monitors.getList().get(0).getValues());
    }
}
