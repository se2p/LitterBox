package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.elementchoice.ElementChoice;
import scratch.ast.model.elementchoice.WithId;
import scratch.ast.model.event.BackdropSwitchTo;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.actorlook.SwitchBackdrop;
import scratch.ast.model.statement.actorlook.SwitchBackdropAndWait;
import scratch.ast.visitor.ScratchVisitor;

import java.util.*;

public class MissingBackdropSwitch implements IssueFinder, ScratchVisitor {

    public static final String NAME = "missing_backdrop_switch";
    public static final String SHORT_NAME = "mssngbckdrpswtch";

    private List<Pair> switched = new ArrayList<>();
    private List<Pair> switchReceived = new ArrayList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        program.accept(this);

        final LinkedHashSet<Pair> nonSyncedPairs = new LinkedHashSet<>();
        for (Pair received : switchReceived) {
            boolean isReceived = false;
            for (Pair sent : switched) {
                if (received.msgName.equals(sent.msgName)) {
                    isReceived = true;
                    break;
                }
            }
            if (!isReceived) {
                nonSyncedPairs.add(received);
            }
        }

        final Set<String> actorNames = new LinkedHashSet<>();
        nonSyncedPairs.forEach(p -> actorNames.add(p.getActorName()));

        return new IssueReport(NAME, nonSyncedPairs.size(), new LinkedList<>(actorNames), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {

        final String actorName = currentActor.getIdent().getName();
        final ElementChoice msgName = node.getElementChoice();
        if (msgName instanceof WithId) {
            if(((WithId) msgName).getStringExpr() instanceof StringLiteral)
            switched.add(new Pair(actorName, ((StringLiteral) ((WithId) msgName).getStringExpr()).getText()));
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        final String actorName = currentActor.getIdent().getName();
        final ElementChoice msgName = node.getElementChoice();
        if (msgName instanceof WithId) {
            if(((WithId) msgName).getStringExpr() instanceof StringLiteral)
                switched.add(new Pair(actorName, ((StringLiteral) ((WithId) msgName).getStringExpr()).getText()));
        }
    }


    @Override
    public void visit(BackdropSwitchTo node) {
            final String actorName = currentActor.getIdent().getName();
            final String msgName = node.getBackdrop().getName();
            switchReceived.add(new Pair(actorName, msgName));
    }

    /**
     * Helper class to map which messages are sent / received by which actor
     */
    private static class Pair {
        private String actorName;
        String msgName;

        public Pair(String actorName, String msgName) {
            this.setActorName(actorName);
            this.msgName = msgName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair pair = (Pair) o;

            if (!Objects.equals(getActorName(), pair.getActorName())) {
                return false;
            }
            return Objects.equals(msgName, pair.msgName);
        }

        @Override
        public int hashCode() {
            int result = getActorName() != null ? getActorName().hashCode() : 0;
            result = 31 * result + (msgName != null ? msgName.hashCode() : 0);
            return result;
        }

        String getActorName() {
            return actorName;
        }

        void setActorName(String actorName) {
            this.actorName = actorName;
        }
    }
}
