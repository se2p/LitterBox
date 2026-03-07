/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptSanitizer} utility class.
 */
public class GoboscriptSanitizerTest {

    // ==================== Keyword Escaping ====================

    @Test
    public void testLogKeywordEscaped() {
        // 'log' is a built-in math function in Goboscript
        String result = GoboscriptSanitizer.escapeIdentifier("log");
        assertThat(result).isEqualTo("_log");
    }

    @Test
    public void testLengthKeywordEscaped() {
        // 'length' is a built-in function in Goboscript
        String result = GoboscriptSanitizer.escapeIdentifier("length");
        assertThat(result).isEqualTo("_length");
    }

    @Test
    public void testCloudKeywordEscaped() {
        // 'cloud' is a type modifier in Goboscript
        String result = GoboscriptSanitizer.escapeIdentifier("cloud");
        assertThat(result).isEqualTo("_cloud");
    }

    @Test
    public void testShowKeywordEscaped() {
        // 'show' is a keyword in Goboscript
        String result = GoboscriptSanitizer.escapeIdentifier("show");
        assertThat(result).isEqualTo("_show");
    }

    @Test
    public void testHideKeywordEscaped() {
        // 'hide' is a keyword in Goboscript
        String result = GoboscriptSanitizer.escapeIdentifier("hide");
        assertThat(result).isEqualTo("_hide");
    }

    @Test
    public void testMathFunctionKeywordsEscaped() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("sin")).isEqualTo("_sin");
        assertThat(GoboscriptSanitizer.escapeIdentifier("cos")).isEqualTo("_cos");
        assertThat(GoboscriptSanitizer.escapeIdentifier("tan")).isEqualTo("_tan");
        assertThat(GoboscriptSanitizer.escapeIdentifier("sqrt")).isEqualTo("_sqrt");
        assertThat(GoboscriptSanitizer.escapeIdentifier("abs")).isEqualTo("_abs");
        assertThat(GoboscriptSanitizer.escapeIdentifier("round")).isEqualTo("_round");
        assertThat(GoboscriptSanitizer.escapeIdentifier("floor")).isEqualTo("_floor");
        assertThat(GoboscriptSanitizer.escapeIdentifier("ceil")).isEqualTo("_ceil");
        assertThat(GoboscriptSanitizer.escapeIdentifier("ln")).isEqualTo("_ln");
        assertThat(GoboscriptSanitizer.escapeIdentifier("antiln")).isEqualTo("_antiln");
        assertThat(GoboscriptSanitizer.escapeIdentifier("antilog")).isEqualTo("_antilog");
    }

    @Test
    public void testLogicKeywordsEscaped() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("and")).isEqualTo("_and");
        assertThat(GoboscriptSanitizer.escapeIdentifier("or")).isEqualTo("_or");
        assertThat(GoboscriptSanitizer.escapeIdentifier("not")).isEqualTo("_not");
        assertThat(GoboscriptSanitizer.escapeIdentifier("true")).isEqualTo("_true");
        assertThat(GoboscriptSanitizer.escapeIdentifier("false")).isEqualTo("_false");
    }

    @Test
    public void testControlFlowKeywordsEscaped() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("if")).isEqualTo("_if");
        assertThat(GoboscriptSanitizer.escapeIdentifier("else")).isEqualTo("_else");
        assertThat(GoboscriptSanitizer.escapeIdentifier("elif")).isEqualTo("_elif");
        assertThat(GoboscriptSanitizer.escapeIdentifier("return")).isEqualTo("_return");
    }

    @Test
    public void testListOperationKeywordsEscaped() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("add")).isEqualTo("_add");
        assertThat(GoboscriptSanitizer.escapeIdentifier("delete")).isEqualTo("_delete");
        assertThat(GoboscriptSanitizer.escapeIdentifier("insert")).isEqualTo("_insert");
        assertThat(GoboscriptSanitizer.escapeIdentifier("at")).isEqualTo("_at");
        assertThat(GoboscriptSanitizer.escapeIdentifier("to")).isEqualTo("_to");
        assertThat(GoboscriptSanitizer.escapeIdentifier("as")).isEqualTo("_as");
        assertThat(GoboscriptSanitizer.escapeIdentifier("in")).isEqualTo("_in");
    }

    @Test
    public void testKeywordsCaseInsensitive() {
        // Keywords should be escaped regardless of case
        assertThat(GoboscriptSanitizer.escapeIdentifier("LOG")).isEqualTo("_LOG");
        assertThat(GoboscriptSanitizer.escapeIdentifier("Length")).isEqualTo("_Length");
        assertThat(GoboscriptSanitizer.escapeIdentifier("CLOUD")).isEqualTo("_CLOUD");
    }

    // ==================== Non-Keywords Unchanged ====================

    @Test
    public void testNonKeywordsUnchanged() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("myVariable")).isEqualTo("myVariable");
        assertThat(GoboscriptSanitizer.escapeIdentifier("counter")).isEqualTo("counter");
        assertThat(GoboscriptSanitizer.escapeIdentifier("score")).isEqualTo("score");
    }

    // ==================== Special Character Handling ====================

    @Test
    public void testGermanUmlautsReplaced() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("größe")).isEqualTo("groesse");
        assertThat(GoboscriptSanitizer.escapeIdentifier("Züge")).isEqualTo("Zuege");
        assertThat(GoboscriptSanitizer.escapeIdentifier("Öffnen")).isEqualTo("Oeffnen");
    }

    @Test
    public void testAccentsStripped() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("café")).isEqualTo("cafe");
        assertThat(GoboscriptSanitizer.escapeIdentifier("naïve")).isEqualTo("naive");
    }

    @Test
    public void testSpacesReplacedWithUnderscore() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("my variable")).isEqualTo("my_variable");
    }

    @Test
    public void testDigitPrefixHandled() {
        assertThat(GoboscriptSanitizer.escapeIdentifier("123abc")).isEqualTo("_123abc");
    }

    // ==================== Procedure Name Escaping ====================

    @Test
    public void testProcedureNameReservedCommand() {
        // 'move' is a reserved command, should get _proc suffix
        assertThat(GoboscriptSanitizer.escapeProcedureName("move")).isEqualTo("move_proc");
        assertThat(GoboscriptSanitizer.escapeProcedureName("show")).isEqualTo("show_proc");
        assertThat(GoboscriptSanitizer.escapeProcedureName("hide")).isEqualTo("hide_proc");
    }

    @Test
    public void testProcedureNameNotReserved() {
        assertThat(GoboscriptSanitizer.escapeProcedureName("myProcedure")).isEqualTo("myProcedure");
    }

    // ==================== List Identifier Escaping ====================

    @Test
    public void testListIdentifierSuffix() {
        assertThat(GoboscriptSanitizer.escapeListIdentifier("scores")).isEqualTo("scores_list");
        assertThat(GoboscriptSanitizer.escapeListIdentifier("log")).isEqualTo("_log_list");
    }
}
