/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MutationMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testProtoMutation() {
        MutationMetadata mutationMetadata = MutationMetadataParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                "Vr$zTl8mo1W,U?+q6,T{").get(MUTATION_KEY));
        Assertions.assertTrue(mutationMetadata instanceof ProcedureMutationMetadata);
        ProcedureMutationMetadata existing = (ProcedureMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
    }

    @Test
    public void testCallMutation() {
        MutationMetadata mutationMetadata = MutationMetadataParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                "O3bG_[t(B3p}k0KF:.,|").get(MUTATION_KEY));
        Assertions.assertTrue(mutationMetadata instanceof ProcedureMutationMetadata);
        ProcedureMutationMetadata existing = (ProcedureMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
    }
}
