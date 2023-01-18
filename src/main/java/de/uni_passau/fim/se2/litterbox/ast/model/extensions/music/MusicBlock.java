package de.uni_passau.fim.se2.litterbox.ast.model.extensions.music;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;

public interface MusicBlock extends ExtensionBlock {

    void accept(MusicExtensionVisitor visitor);
}
