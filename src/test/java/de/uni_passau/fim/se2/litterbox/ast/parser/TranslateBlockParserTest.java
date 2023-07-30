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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TranslateBlockParserTest implements JsonTest {
    @Test
    void testParseTranslateBlocks() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/ml_preprocessing/shared/translate_blocks.json");
        assertNoUnspecifiedBlocks(program);

        final var translateTo = NodeFilteringVisitor.getBlocks(program, TranslateTo.class);
        assertThat(translateTo).hasSize(2);

        final var fixedLanguages = NodeFilteringVisitor.getBlocks(program, TFixedLanguage.class);
        assertThat(fixedLanguages).hasSize(1);
        assertThat(fixedLanguages.get(0).getType()).isEqualTo(TFixedLanguage.TFixedLanguageType.CZECH);

        final var viewerLanguage = NodeFilteringVisitor.getBlocks(program, ViewerLanguage.class);
        assertThat(viewerLanguage).hasSize(1);
    }
}
