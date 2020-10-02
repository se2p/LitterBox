/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import java.io.PrintStream;

public abstract class PrintVisitor implements ScratchVisitor {

    protected static final String INDENT = "    ";
    protected PrintStream printStream;
    protected int level;

    public PrintVisitor(PrintStream printStream) {
        this.printStream = printStream;
        level = 0;
    }

    protected void emitToken(String string) {
        emitNoSpace(string);
        emitNoSpace(" ");
    }

    protected void emitNoSpace(String string) {
        printStream.append(string);
    }

    protected void endIndentation() {
        level--;
    }

    protected void beginIndentation() {
        level++;
    }

    protected void appendIndentation() {
        String currentIndent = new String(new char[level]).replace("\0", INDENT);
        emitNoSpace(currentIndent);
    }

    protected void begin() {
        emitToken(" begin");
    }

    protected void end() {
        newLine();
        appendIndentation();
        emitToken("end");
    }

    protected void newLine() {
        emitNoSpace("\n");
    }
}
