package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.stmt.TerminationStmtParser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InlineLoopCondition extends CloneVisitor implements Refactoring {

    public static final String NAME = "inline_loop_condition";

    private final UntilStmt untilLoop;
    private final TerminationStmt terminationStmt;
    private final RepeatForeverStmt replacementLoop;

    public InlineLoopCondition(UntilStmt untilLoop) {
        this(untilLoop, null);
    }

    public InlineLoopCondition(UntilStmt untilLoop, TerminationStmt terminationStmt) {
        this.untilLoop = Preconditions.checkNotNull(untilLoop);
        if (terminationStmt == null) {
            // TODO: Find a way to do this without all the metadata handling
            BlockMetadata blockMetadata = new NonDataBlockMetadata(null, CloneVisitor.generateUID(),
                    new InputMetadataList(Collections.emptyList()),
                    new FieldsMetadataList(Arrays.asList(new FieldsMetadata(TerminationStmtParser.STOP_OPTION, TerminationStmtParser.STOP_THIS, null))), false, false, new NoMutationMetadata());
            this.terminationStmt = new StopThisScript(blockMetadata);
        } else {
            this.terminationStmt = apply(terminationStmt);
        }

        IfThenStmt ifThenStmt = new IfThenStmt(apply(untilLoop.getBoolExpr()), new StmtList(this.terminationStmt), apply(untilLoop.getMetadata()));
        List<Stmt> loopBody = apply(untilLoop.getStmtList()).getStmts();
        loopBody.add(ifThenStmt);

        replacementLoop = new RepeatForeverStmt(new StmtList(loopBody), apply(untilLoop.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(untilLoop, replacementLoop));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced until loop:" + System.lineSeparator() + untilLoop.getScratchBlocks() + System.lineSeparator() +
                "with forever loop:" + System.lineSeparator() + replacementLoop.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InlineLoopCondition that = (InlineLoopCondition) o;
        return Objects.equals(untilLoop, that.untilLoop) && Objects.equals(terminationStmt, that.terminationStmt) && Objects.equals(replacementLoop, that.replacementLoop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(untilLoop, terminationStmt, replacementLoop);
    }
}
