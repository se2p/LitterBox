package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopBlockMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BlockMetadataParser {
    public static BlockMetadata parse(String blockId, JsonNode blockNode) {
        if (blockNode.has(OPCODE_KEY)) {
            String commentId = null;
            if (blockNode.has(COMMENT_KEY)) {
                commentId = blockNode.get(COMMENT_KEY).asText();
            }
            String opcode = blockNode.get(OPCODE_KEY).asText();
            String next = null;
            if (!(blockNode.get(NEXT_KEY) instanceof NullNode)) {
                next = blockNode.get(NEXT_KEY).asText();
            }
            String parent = null;
            if (!(blockNode.get(PARENT_KEY) instanceof NullNode)) {
                parent = blockNode.get(PARENT_KEY).asText();
            }
            InputMetadataList inputMetadata = InputMetadataListParser.parse(blockNode.get(INPUTS_KEY));
            InputMetadataList fields = InputMetadataListParser.parse(blockNode.get(FIELDS_KEY));
            boolean topLevel = blockNode.get(TOPLEVEL_KEY).asBoolean();
            boolean shadow = blockNode.get(SHADOW_KEY).asBoolean();
            MutationMetadata mutation;
            if (blockNode.has(MUTATION_KEY)) {
                mutation = MutationMetadataParser.parse(blockNode.get(MUTATION_KEY));
            } else {
                mutation = new NoMutationMetadata();
            }
            if (!topLevel) {
                return new BlockMetadata(commentId, blockId, opcode, next, parent, inputMetadata, fields, topLevel,
                        shadow,
                        mutation);
            }
            double x = blockNode.get(X_KEY).asDouble();
            double y = blockNode.get(Y_KEY).asDouble();
            return new TopBlockMetadata(commentId, blockId, opcode, next, parent, inputMetadata, fields, topLevel,
                    shadow,
                    mutation, x, y);
        } else {
            return null;
        }
    }
}
