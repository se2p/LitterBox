package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;

public class AstNodeUtil {
    private AstNodeUtil() {
    }

    public static boolean isMetadata(final ASTNode node) {
        return node instanceof Metadata
                || node instanceof CommentMetadataList
                || node instanceof ImageMetadataList
                || node instanceof MonitorMetadataList
                || node instanceof SoundMetadataList;
    }
}
