package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementDeletionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractEventsFromForever extends OnlyCodeCloneVisitor implements Refactoring{

    public static final String NAME = "extract_event_handler";

    private RepeatForeverStmt loop;
    private Script script;
    private ArrayList<Script> eventScripts = new ArrayList<>();
    private boolean onlyIfThenStmts = false;
    // The new ForeverLoop if not only IfThen Statements;
    private RepeatForeverStmt replacement = null;

    public ExtractEventsFromForever(Script script, RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        this.script = Preconditions.checkNotNull(script);


        // New Script for each if then with KeyPressed Event
        ArrayList<Stmt> replacementStmts = new ArrayList<>();
        for (Stmt stmt : this.loop.getStmtList().getStmts()) {
            if(stmt instanceof IfThenStmt) {
                IfThenStmt ifThenStmt = (IfThenStmt) stmt;
                BoolExpr expr = ifThenStmt.getBoolExpr();
                if(expr instanceof IsKeyPressed) {
                    NonDataBlockMetadata keyMetaData = NonDataBlockMetadata.emptyNonBlockMetadata();
                    Event e = new KeyPressed(((IsKeyPressed) expr).getKey(), keyMetaData);
                    Script event = new Script(e, ifThenStmt.getThenStmts());
                    eventScripts.add(event);
                }
            } else {
                replacementStmts.add(stmt);
            }
        }

        if (replacementStmts.size() > 0) {
            replacement = new RepeatForeverStmt(new StmtList(replacementStmts), loop.getMetadata());
        } else {
            onlyIfThenStmts = true;
        }
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            scripts.add(apply(currentScript));
        }

        // Add all scripts
        for(Script currentScript : eventScripts) {
            scripts.add(currentScript);
        }
        return new ScriptList(scripts);
    }

    @Override
    public Program apply(Program program) {
        if(onlyIfThenStmts) {
            program = (Program) program.accept(new StatementDeletionVisitor(loop));
        } else {
            program = (Program) program.accept(new StatementReplacementVisitor(loop, replacement));
        }
        return (Program) program.accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtractEventsFromForever)) return false;
        ExtractEventsFromForever that = (ExtractEventsFromForever) o;
        boolean equals = true;

        if(this.eventScripts.size() != that.eventScripts.size()) {
            return false;
        }

        for (int i = 0; i < this.eventScripts.size(); i++) {
            if (this.eventScripts.get(i).equals(that.eventScripts.get(i)))
                equals = false;
        }
        return equals && Objects.equals(this.loop, that.loop) && Objects.equals(this.script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, eventScripts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Script script : eventScripts) {
            sb.append(System.lineSeparator());
            sb.append(script.getScratchBlocks());
            sb.append(" and ");
        }
        sb.delete(sb.length()-6 , sb.length()-1);
        return NAME + System.lineSeparator() + "Extracting" + loop.getScratchBlocks() +  System.lineSeparator() +
                " to:" + System.lineSeparator() + sb +  System.lineSeparator();
    }

}
