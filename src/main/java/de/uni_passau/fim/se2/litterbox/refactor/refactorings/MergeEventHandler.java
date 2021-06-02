package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MergeEventHandler extends ScratchBlocksVisitor implements Refactoring {

    public static final String NAME = "merge_event_handler";
    private ArrayList<Event> eventList;
    private Script replacement;
    private StmtList newStmts;

    public MergeEventHandler(ArrayList<Event> eventList) {
        this.eventList = Preconditions.checkNotNull(eventList);

        // Create forever loop.
        replacement = new Script(new GreenFlag(new NoBlockMetadata()), newStmts);
    }


    @Override
    public void visit(KeyPressed keyPressed) {
        Key key = keyPressed.getKey();
        IsKeyPressed isKeyPressed = new IsKeyPressed(key, keyPressed.getMetadata());
        System.out.println("Test");
    }

    @Override
    public Program apply(Program program) {
        for(Event e : eventList)
            visit(e);

        return program;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
