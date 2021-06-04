package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementDeletionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ExtractEventsFromForever extends CloneVisitor implements Refactoring{

    public static final String NAME = "extract_event_handler";

    private RepeatForeverStmt loop;
    private Script script;
    private ArrayList<Script> eventScripts = new ArrayList<>();

    public ExtractEventsFromForever(Script script, RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        this.script = Preconditions.checkNotNull(script);


        // New Script for each if then with KeyPressed Event
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
            }
        }
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            scripts.add(apply(currentScript));
        }
        for(Script currentScript : eventScripts) {
            scripts.add(currentScript);
        }
        return new ScriptList(scripts);
    }

    @Override
    public Program apply(Program program) {
        program = (Program) program.accept(new StatementDeletionVisitor(loop));
        return (Program) program.accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
