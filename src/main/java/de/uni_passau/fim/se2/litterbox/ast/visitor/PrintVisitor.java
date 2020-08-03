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
        emitToken(currentIndent);
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
