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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BlockMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testDataBlock() throws ParsingException {
        BlockMetadata blockMetadata = BlockMetadataParser.parse("GKr#[hOQWwm(reaPtK%R",
                prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "GKr#[hOQWwm(reaPtK%R"));
        Assertions.assertTrue(blockMetadata instanceof DataBlockMetadata);
        DataBlockMetadata dataBlock = (DataBlockMetadata) blockMetadata;
        Assertions.assertEquals(471, dataBlock.getX());
        Assertions.assertEquals(383, dataBlock.getY());
    }

    @Test
    public void testNoMetadataTopBlock() throws ParsingException {
        BlockMetadata blockMetadata = BlockMetadataParser.parse("X)N~xB@[E,i0S}Vwwtjm",
                prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "X)N~xB@[E,i0S}Vwwtjm"));
        Assertions.assertTrue(blockMetadata instanceof TopNonDataBlockMetadata);
        TopNonDataBlockMetadata topNonDataBlockMetadata = (TopNonDataBlockMetadata) blockMetadata;
        Assertions.assertEquals("X)N~xB@[E,i0S}Vwwtjm", topNonDataBlockMetadata.getBlockId());
        Assertions.assertNull(topNonDataBlockMetadata.getCommentId());
        Assertions.assertEquals(56, topNonDataBlockMetadata.getXPos());
        Assertions.assertEquals(184, topNonDataBlockMetadata.getYPos());
        Assertions.assertTrue(topNonDataBlockMetadata.getMutation() instanceof NoMutationMetadata);
        Assertions.assertFalse(topNonDataBlockMetadata.isShadow());
        Assertions.assertNull(topNonDataBlockMetadata.getParentNode());
    }

    @Test
    public void testMetadataBlock() throws ParsingException {
        BlockMetadata blockMetadata = BlockMetadataParser.parse("Vr$zTl8mo1W,U?+q6,T{",
                prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "Vr$zTl8mo1W,U?+q6,T{"));
        Assertions.assertTrue(blockMetadata instanceof NonDataBlockMetadata);
        NonDataBlockMetadata nonDataBlockMetadata = (NonDataBlockMetadata) blockMetadata;
        Assertions.assertEquals("Vr$zTl8mo1W,U?+q6,T{", nonDataBlockMetadata.getBlockId());
        Assertions.assertNull(nonDataBlockMetadata.getCommentId());
        Assertions.assertTrue(nonDataBlockMetadata.getMutation() instanceof ProcedureMutationMetadata);
        Assertions.assertTrue(nonDataBlockMetadata.isShadow());
    }
}
