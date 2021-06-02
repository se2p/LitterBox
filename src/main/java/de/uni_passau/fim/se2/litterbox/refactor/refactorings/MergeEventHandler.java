package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeEventHandler extends CloneVisitor implements Refactoring {

    public static final String NAME = "merge_event_handler";
    private ArrayList<Script> scriptList;

    private Script replacement;

    public MergeEventHandler(ArrayList<Script> eventList) {
        Preconditions.checkNotNull(eventList);
        Preconditions.checkArgument(eventList.size() > 1);
        this.scriptList = eventList;

        // Create statement list with if then blocks for each event script.
        ArrayList<Stmt> ifThenArrayList = new ArrayList<>();
        for (Script script : scriptList) {
            ifThenArrayList.add(getIfStmtFromEventScript(script));
        }

        // Create forever loop.
        StmtList foreverStmt = new StmtList(new RepeatForeverStmt(new StmtList(ifThenArrayList),
                apply(scriptList.get(0).getStmtList().getStatement(0).getMetadata())));

        GreenFlag greenFlag = new GreenFlag(apply(scriptList.get(0).getEvent().getMetadata()));
        replacement = new Script(greenFlag, foreverStmt);
    }

    private Stmt getIfStmtFromEventScript(Script script) {
        Preconditions.checkArgument(script.getEvent() instanceof KeyPressed);
        Preconditions.checkArgument(script.getStmtList().getNumberOfStatements() > 0);

        Key pressed = ((KeyPressed) script.getEvent()).getKey();
        NonDataBlockMetadata keyMetaData = NonDataBlockMetadata.emptyNonBlockMetadata();
        IsKeyPressed expression = new IsKeyPressed(apply(pressed), keyMetaData);
        List<Stmt> stmts = apply(script.getStmtList()).getStmts();

        return new IfThenStmt(expression, new StmtList(stmts), apply(stmts.get(0).getMetadata()));
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        boolean inserted = false;
        for (Script currentScript : node.getScriptList()) {
            if (scriptList.contains(currentScript)) {
                if (!inserted) {
                    scripts.add(replacement);
                    inserted = true;
                }
            } else {
                scripts.add(apply(currentScript));
            }
        }
        return new ScriptList(scripts);
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergeEventHandler)) return false;
        MergeEventHandler that = (MergeEventHandler) o;
        boolean equals = true;

        if(this.scriptList.size() != that.scriptList.size())
            return false;

        for (int i = 0; i < this.scriptList.size(); i++) {
            if (this.scriptList.get(i).equals(that.scriptList.get(i)))
                    equals = false;
        }
        return equals && Objects.equals(this.replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scriptList, replacement);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Script script : scriptList) {
            sb.append(System.lineSeparator());
            sb.append(script.getScratchBlocks());
            sb.append(" and ");
        }
        sb.delete(sb.length()-6 , sb.length()-1);
        return NAME + System.lineSeparator() + "Merging" + sb +  System.lineSeparator() +
                " to:" + System.lineSeparator() + replacement.getScratchBlocks() +  System.lineSeparator();
    }

}
