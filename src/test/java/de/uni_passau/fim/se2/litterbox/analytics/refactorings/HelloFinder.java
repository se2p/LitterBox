package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.AddHelloBlock;

import java.util.List;

public class HelloFinder extends AbstractRefactoringFinder {

    private static final String NAME = "hello_finder";

    @Override
    public void visit(ScriptList node) {
        final List<Script> scriptList = node.getScriptList();

        for (Script script : scriptList) {
            refactorings.add(new AddHelloBlock(script));
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
