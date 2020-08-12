package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicatedScript extends TopBlockFinder {

    private static final String NAME = "duplicated_script";

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ScriptList node) {
        Set<Script> checked = new HashSet();
        List<Script> scripts = node.getScriptList();
        for (Script s : scripts) {
            setHint = false;
            for (Script other : scripts) {
                if (s == other || checked.contains(other)) {
                    continue;
                }

                s.accept(this);
                if (s.equals(other)) {
                    checked.add(s);
                    setHint = true;
                    if (!(s.getEvent() instanceof Never)) {
                        s.getEvent().accept(this);
                    } else {
                        s.getStmtList().accept(this);
                    }

                    break;
                }
            }
        }
    }
}
