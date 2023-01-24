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

        if (visitor instanceof TranslateExtensionVisitor) {
            ((TranslateExtensionVisitor) visitor).visit(this);
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
        KURDISH_KURMANJI("ku"), LATIN("la"), LATVIAN("lv"), LITHUANIAN("lt"), MACEDONIAN("mk"), MALAY("ms"), MALAYALAM("ml"),
        MALTESE("mt"), MAORI("mi"), MARATHI("mr"), MONGOLIAN("mn"), MYANMAR_BURMESE("my"), PERSIAN("fa"), POLISH("pl"),
        PORTUGUESE("pt"), ROMANIAN("ro"), RUSSIAN("ru"), SCOTS_GAELIC("gd"), SERBIAN("sr"), SLOVAK("sk"), SLOVENIAN("sl"),
        SPANISH("es"), SWEDISH("sv"), TELUGU("te"), THAI("th"), TURKISH("tr"), UKRAINIAN("uk"), UZBEK("uz"), VIETNAMESE("vi"),
        WELSH("cy"), ZULU("zu"), HEBREW("he"), CHINESE_SIMPLIFIED("zh-cn");

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
            switch (this) {

                case ALBANIAN:
                    return "Albanian";

                case AMHARIC:
                    return "Amharic";

                case ARABIC:
                    return "Arabic";

                case ARMENIAN:
                    return "Armenian";

                case AZERBAIJANI:
                    return "Azerbaijani";

                case BASQUE:
                    return "Basque";

                case BELARUSIAN:
                    return "Belarusian";

                case BULGARIAN:
                    return "Bulgarian";

                case CATALAN:
                    return "Catalan";

                case CHINESE_TRADITIONAL:
                    return "Chinese (Traditional)";

                case CROATIAN:
                    return "Croatian";

                case CZECH:
                    return "Czech";

                case DANISH:
                    return "Danish";

                case DUTCH:
                    return "Dutch";

                case ENGLISH:
                    return "English";

                case ESPERANTO:
                    return "Esperanto";
                case ESTONIAN:
                    return "Estonian";

                case FINNISH:
                    return "Finnish";

                case FRENCH:
                    return "French";

                case GALICIAN:
                    return "Galician";

                case GERMAN:
                    return "German";
                case GREEK:
                    return "";
                case HAITIAN_CREOLE:
                    return "Haitian Creole";
                case HINDI:
                    return "Hindi";
                case HUNGARIAN:
                    return "Hungarian";
                case ICELANDIC:
                    return "Icelandic";
                case INDONESIAN:
                    return "Indonesian";
                case IRISH:
                    return "Irish";
                case ITALIAN:
                    return "Italian";
                case JAPANESE:
                    return "Japanese";
                case KANNADA:
                    return "Kannada";
                case KOREAN:
                    return "Korean";
                case KURDISH_KURMANJI:
                    return "Kurdish (Kurmanji)";
                case LATIN:
                    return "Latin";
                case LATVIAN:
                    return "Latvian";
                case LITHUANIAN:
                    return "Lithuanian";
                case MACEDONIAN:
                    return "Macedonian";
                case MALAY:
                    return "Malay";
                case MALAYALAM:
                    return "Malayalam";
                case MALTESE:
                    return "Maltese";
                case MAORI:
                    return "Maori";
                case MARATHI:
                    return "Marathi";
                case MONGOLIAN:
                    return "Mongolian";
                case MYANMAR_BURMESE:
                    return "Myanmar (Burmese)";
                case PERSIAN:
                    return "Persian";
                case POLISH:
                    return "Polish";
                case PORTUGUESE:
                    return "Portuguese";
                case ROMANIAN:
                    return "Romanian";
                case RUSSIAN:
                    return "Russian";
                case SCOTS_GAELIC:
                    return "Scots Gaelic";
                case SERBIAN:
                    return "Serbian";
                case SLOVAK:
                    return "Slovak";
                case SLOVENIAN:
                    return "Slovenian";
                case SPANISH:
                    return "Spanish";
                case SWEDISH:
                    return "Swedish";
                case TELUGU:
                    return "Telugu";
                case THAI:
                    return "Thai";
                case TURKISH:
                    return "Turkish";
                case UKRAINIAN:
                    return "Ukrainian";
                case UZBEK:
                    return "Uzbek";
                case VIETNAMESE:
                    return "Vietnamese";
                case WELSH:
                    return "Welsh";
                case ZULU:
                    return "Zulu";
                case HEBREW:
                    return "Hebrew";
                case CHINESE_SIMPLIFIED:
                    return "Chinese (Simplified)";
                default:
                    throw new IllegalArgumentException("Unknown FixedLanguage: " + type);
            }
        }

        public String getType() {
            return type;
        }
    }
}
