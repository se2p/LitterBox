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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.FixedNodeOption;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedVoice extends AbstractNode implements Voice, FixedNodeOption {
    private final BlockMetadata metadata;
    private final FixedVoiceType type;

    public FixedVoice(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedVoiceType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedVoiceType getType() {
        return type;
    }

    @Override
    public String getTypeName() {
        return type.getType();
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof TextToSpeechExtensionVisitor textToSpeechExtensionVisitor) {
            textToSpeechExtensionVisitor.visit(this);
        } else {
            visitor.visit(this);
        }
    }

    @Override
    public void accept(TextToSpeechExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    public enum FixedVoiceType {

        ALTO("alto"), TENOR("tenor"), SQUEAK("squeak"), GIANT("giant"), KITTEN("kitten");

        private final String type;

        FixedVoiceType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedVoiceType fromString(String type) {
            for (FixedVoiceType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedVoice: " + type);
        }

        public String getType() {
            return type;
        }
    }
}
