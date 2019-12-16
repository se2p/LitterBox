package newanalytics;

import scratch.ast.model.Program;
import scratch.ast.visitor.ScratchVisitor;

import java.util.ArrayList;

public class SpriteCount implements ScratchVisitor, IssueFinder {
    public static final String NAME = "sprite_count";
    public static final String SHORT_NAME = "sprtcnt";

    @Override
    public IssueReport check(Program program) {
        int count = program.getActorDefinitionList().getDefintions().size()-1;
        return new IssueReport(NAME, count, new ArrayList<>(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
