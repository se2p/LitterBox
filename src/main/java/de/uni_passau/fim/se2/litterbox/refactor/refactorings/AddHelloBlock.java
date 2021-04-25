package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;

public class AddHelloBlock implements Refactoring {

    private final Script script;
    private final StmtList stmtList;
    private static final String NAME = "add_hello_block";

    public AddHelloBlock(Script script) {
        this.script = script;
        this.stmtList = script.getStmtList();
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        Say helloBlock = new Say(new StringLiteral("Hello!"), new NoBlockMetadata());
        stmtList.getStmts().add(helloBlock);
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + script.getUniqueName() + ")";
    }

}
