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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MutationMetadataTest {
    private static Program prog;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        prog = JsonTest.parseProgram("./src/test/fixtures/metadata/blockMeta.json");
    }

    @Test
    public void testProtoMutation() {
        NonDataBlockMetadata meta = NodeFilteringVisitor.getBlocks(prog, NonDataBlockMetadata.class).stream()
                .filter(m -> "Vr$zTl8mo1W,U?+q6,T{".equals(m.getBlockId()))
                .findFirst()
                .orElseThrow();
        MutationMetadata mutationMetadata = meta.getMutation();

        Assertions.assertInstanceOf(ProcedureMutationMetadata.class, mutationMetadata);
        ProcedureMutationMetadata existing = (ProcedureMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
    }

    @Test
    public void testCallMutation() {
        ASTNode block = JsonTest.getBlock(prog, "O3bG_[t(B3p}k0KF:.,|", ASTNode.class);
        NonDataBlockMetadata meta = (NonDataBlockMetadata) block.getMetadata();
        MutationMetadata mutationMetadata = meta.getMutation();

        Assertions.assertInstanceOf(ProcedureMutationMetadata.class, mutationMetadata);
        ProcedureMutationMetadata existing = (ProcedureMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
    }
}
