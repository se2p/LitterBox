/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package analytics.bugpattern;

import analytics.IssueFinder;
import analytics.IssueReport;
import ast.model.ASTNode;
import ast.model.ActorDefinition;
import ast.model.Program;
import ast.model.Script;
import ast.model.elementchoice.Random;
import ast.model.elementchoice.*;
import ast.model.event.BackdropSwitchTo;
import ast.model.literals.StringLiteral;
import ast.model.statement.actorlook.SwitchBackdrop;
import ast.model.statement.actorlook.SwitchBackdropAndWait;
import ast.model.variable.StrId;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.*;

public class MissingBackdropSwitch implements IssueFinder, ScratchVisitor {

    public static final String NAME = "missing_backdrop_switch";
    public static final String SHORT_NAME = "mssBackdrSwitch";

    private List<Pair> switched = new ArrayList<>();
    private List<Pair> switchReceived = new ArrayList<>();
    private ActorDefinition currentActor;
    private boolean nextRandPrev = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        switched = new ArrayList<>();
        switchReceived = new ArrayList<>();
        nextRandPrev = false;
        program.accept(this);

        final LinkedHashSet<Pair> nonSyncedPairs = new LinkedHashSet<>();
        if(!nextRandPrev) {
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
        if (msgName instanceof Next || msgName instanceof Prev || msgName instanceof Random) {
            nextRandPrev = true;
        } else if (msgName instanceof WithId) {
            if (((WithId) msgName).getStringExpr() instanceof StrId)
                switched.add(new Pair(actorName, ((StrId) ((WithId) msgName).getStringExpr()).getName()));
            if (((WithId) msgName).getStringExpr() instanceof StringLiteral)
                switched.add(new Pair(actorName, ((StringLiteral) ((WithId) msgName).getStringExpr()).getText()));
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        final String actorName = currentActor.getIdent().getName();
        final ElementChoice msgName = node.getElementChoice();
        if (msgName instanceof Next || msgName instanceof Prev || msgName instanceof Random) {
            nextRandPrev = true;
        } else if (msgName instanceof WithId) {
            if (((WithId) msgName).getStringExpr() instanceof StringLiteral)
                switched.add(new Pair(actorName, ((StringLiteral) ((WithId) msgName).getStringExpr()).getText()));
            if (((WithId) msgName).getStringExpr() instanceof StrId)
                switched.add(new Pair(actorName, ((StrId) ((WithId) msgName).getStringExpr()).getName()));
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getStmtList().getStmts().getListOfStmt().size() > 0 && node.getEvent() instanceof BackdropSwitchTo) {
            BackdropSwitchTo event = (BackdropSwitchTo) node.getEvent();

            final String actorName = currentActor.getIdent().getName();
            final String msgName = event.getBackdrop().getName();
            switchReceived.add(new Pair(actorName, msgName));

        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
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
