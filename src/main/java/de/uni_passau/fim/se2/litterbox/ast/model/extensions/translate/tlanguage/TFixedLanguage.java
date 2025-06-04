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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.FixedNodeOption;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TFixedLanguage extends AbstractNode implements TLanguage, FixedNodeOption {
    private final BlockMetadata metadata;
    private final TFixedLanguage.TFixedLanguageType type;

    public TFixedLanguage(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = TFixedLanguage.TFixedLanguageType.fromString(typeName);
        this.metadata = metadata;
    }

    public TFixedLanguage.TFixedLanguageType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof TranslateExtensionVisitor translateExtensionVisitor) {
            translateExtensionVisitor.visit(this);
        } else {
            visitor.visit(this);
        }
    }

    @Override
    public void accept(TranslateExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getTypeName() {
        return type.getName();
    }

    public enum TFixedLanguageType {

        ALBANIAN("sq"), AMHARIC("am"), ARABIC("ar"), ARMENIAN("hy"), AZERBAIJANI("az"), BASQUE("eu"),
        BELARUSIAN("be"), BULGARIAN("bg"), CATALAN("ca"), CHINESE_TRADITIONAL("zh-tw"), CROATIAN("hr"), CZECH("cs"),
        DANISH("da"), DUTCH("nl"), ENGLISH("en"), ESPERANTO("eo"), ESTONIAN("et"), FINNISH("fi"),
        FRENCH("fr"), GALICIAN("gl"), GERMAN("de"), GREEK("el"), HAITIAN_CREOLE("ht"), HINDI("hi"), HUNGARIAN("hu"),
        ICELANDIC("is"), INDONESIAN("id"), IRISH("ga"), ITALIAN("it"), JAPANESE("ja"), KANNADA("kn"), KOREAN("ko"),
        KURDISH_KURMANJI("ku"), LATIN("la"), LATVIAN("lv"), LITHUANIAN("lt"), MACEDONIAN("mk"), MALAY("ms"),
        MALAYALAM("ml"), MALTESE("mt"), MAORI("mi"), MARATHI("mr"), MONGOLIAN("mn"), MYANMAR_BURMESE("my"),
        NORWEGIAN("nb"), PERSIAN("fa"), POLISH("pl"), PORTUGUESE("pt"), ROMANIAN("ro"), RUSSIAN("ru"),
        SCOTS_GAELIC("gd"), SERBIAN("sr"), SLOVAK("sk"), SLOVENIAN("sl"), SPANISH("es"), SWEDISH("sv"), TELUGU("te"),
        THAI("th"), TURKISH("tr"), UKRAINIAN("uk"), UZBEK("uz"), VIETNAMESE("vi"), WELSH("cy"), ZULU("zu"),
        HEBREW("he"), CHINESE_SIMPLIFIED("zh-cn");

        private final String type;

        TFixedLanguageType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static TFixedLanguage.TFixedLanguageType fromString(String type) {
            for (TFixedLanguage.TFixedLanguageType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown TFixedLanguage: " + type);
        }

        public String getName() {
            return switch (this) {
                case ALBANIAN -> "Albanian";
                case AMHARIC -> "Amharic";
                case ARABIC -> "Arabic";
                case ARMENIAN -> "Armenian";
                case AZERBAIJANI -> "Azerbaijani";
                case BASQUE -> "Basque";
                case BELARUSIAN -> "Belarusian";
                case BULGARIAN -> "Bulgarian";
                case CATALAN -> "Catalan";
                case CHINESE_TRADITIONAL -> "Chinese (Traditional)";
                case CROATIAN -> "Croatian";
                case CZECH -> "Czech";
                case DANISH -> "Danish";
                case DUTCH -> "Dutch";
                case ENGLISH -> "English";
                case ESPERANTO -> "Esperanto";
                case ESTONIAN -> "Estonian";
                case FINNISH -> "Finnish";
                case FRENCH -> "French";
                case GALICIAN -> "Galician";
                case GERMAN -> "German";
                case GREEK -> "Greek";
                case HAITIAN_CREOLE -> "Haitian Creole";
                case HINDI -> "Hindi";
                case HUNGARIAN -> "Hungarian";
                case ICELANDIC -> "Icelandic";
                case INDONESIAN -> "Indonesian";
                case IRISH -> "Irish";
                case ITALIAN -> "Italian";
                case JAPANESE -> "Japanese";
                case KANNADA -> "Kannada";
                case KOREAN -> "Korean";
                case KURDISH_KURMANJI -> "Kurdish (Kurmanji)";
                case LATIN -> "Latin";
                case LATVIAN -> "Latvian";
                case LITHUANIAN -> "Lithuanian";
                case MACEDONIAN -> "Macedonian";
                case MALAY -> "Malay";
                case MALAYALAM -> "Malayalam";
                case MALTESE -> "Maltese";
                case MAORI -> "Maori";
                case MARATHI -> "Marathi";
                case MONGOLIAN -> "Mongolian";
                case MYANMAR_BURMESE -> "Myanmar (Burmese)";
                case NORWEGIAN -> "Norwegian";
                case PERSIAN -> "Persian";
                case POLISH -> "Polish";
                case PORTUGUESE -> "Portuguese";
                case ROMANIAN -> "Romanian";
                case RUSSIAN -> "Russian";
                case SCOTS_GAELIC -> "Scots Gaelic";
                case SERBIAN -> "Serbian";
                case SLOVAK -> "Slovak";
                case SLOVENIAN -> "Slovenian";
                case SPANISH -> "Spanish";
                case SWEDISH -> "Swedish";
                case TELUGU -> "Telugu";
                case THAI -> "Thai";
                case TURKISH -> "Turkish";
                case UKRAINIAN -> "Ukrainian";
                case UZBEK -> "Uzbek";
                case VIETNAMESE -> "Vietnamese";
                case WELSH -> "Welsh";
                case ZULU -> "Zulu";
                case HEBREW -> "Hebrew";
                case CHINESE_SIMPLIFIED -> "Chinese (Simplified)";
            };
        }

        public String getType() {
            return type;
        }
    }
}
