/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedLanguage extends AbstractNode implements Language, ASTLeaf {
    private final BlockMetadata metadata;
    private FixedLanguageType type;

    public FixedLanguage(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedLanguageType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedLanguageType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(TextToSpeechExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    public enum FixedLanguageType {

        ARABIC("ar"), CHINESE("zh-cn"), DANISH("da"), DUTCH("nl"), ENGLISH("en"), FRENCH("fr"), GERMAN("de"), HINDI("hi"),
        ICELANDIC("is"), ITALIAN("it"), JAPANESE("ja"), KOREAN("ko"), NORWEGIAN("nb"), POLISH("pl"), PORTUGUESE_BR("pt-br"),
        PORTUGUESE("pt"), ROMANIAN("ro"), RUSSIAN("ru"), SPANISH("es"), SPANISH_419("es-419"), SWEDISH("sv"), TURKISH("tr"),
        WELSH("cy");

        private final String type;

        FixedLanguageType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedLanguageType fromString(String type) {
            for (FixedLanguageType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedLanguage: " + type);
        }

        public String getType() {
            return type;
        }
    }
}
