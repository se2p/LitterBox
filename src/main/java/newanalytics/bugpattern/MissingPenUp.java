package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.statement.pen.PenDownStmt;
import scratch.ast.model.statement.pen.PenUpStmt;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.List;

public class MissingPenUp implements IssueFinder {

    public static final String NAME = "missing_pen_up";
    public static final String SHORT_NAME = "msspup";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);


        final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefintions();
        for (ActorDefinition defintion : defintions) {
            // Should we also check procedure definitions? Maybe they have a pen up?
            final List<Script> scriptList = defintion.getScripts().getScriptList();
            CheckVisitor visitor = new CheckVisitor();
            for (Script script : scriptList) {
                script.getStmtList().getStmts().accept(visitor);
                if (visitor.getResult()) {
                    System.out.println("Actor " + defintion.getIdent().getName() + "has a penDown at the end of script but no penUp");
                }
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private class CheckVisitor implements ScratchVisitor {
        private boolean penUpSet = false;
        private boolean penDownSet = false;

        @Override
        public void visit(ASTNode node) {
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(PenUpStmt node) {
            penUpSet = true;
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(PenDownStmt node) {
            penDownSet = true;
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        public void reset() {
            penUpSet = false;
            penDownSet = false;
        }

        public boolean getResult() {
            return penDownSet && !penUpSet;
        }
    }
}
