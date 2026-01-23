/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.util.ProcedureMapping;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

abstract class GgnnGraphEdgesVisitor implements ScratchVisitor {

    private static final Set<Class<? extends ASTNode>> IGNORED_NODE_TYPES = Set.of(
        DeclarationStmtList.class,
        DeclarationStmt.class,
        SetStmtList.class
    );

    protected final List<Pair<ASTNode>> edges = new ArrayList<>();

    static List<Pair<ASTNode>> getChildEdges(final ASTNode node) {
        return getEdges(new ChildEdgesVisitor(), node);
    }

    static List<Pair<ASTNode>> getNextTokenEdges(final ASTNode node) {
        return getEdges(new NextTokenVisitor(), node);
    }

    static List<Pair<ASTNode>> getGuardedByEdges(final ASTNode node) {
        return getEdges(new GuardedByVisitor(), node);
    }

    static List<Pair<ASTNode>> getComputedFromEdges(final ASTNode node) {
        return getEdges(new ComputedFromVisitor(), node);
    }

    static List<Pair<ASTNode>> getParameterPassingEdges(final Program program, final ASTNode node) {
        return getEdges(new ParameterPassingVisitor(program), node);
    }

    static List<Pair<ASTNode>> getMessagePassingEdges(final ASTNode node) {
        return getEdges(new MessagePassingVisitor(), node);
    }

    static List<Pair<ASTNode>> getReturnToEdges(final ASTNode node) {
        return getEdges(new ReturnToVisitor(), node);
    }

    private static List<Pair<ASTNode>> getEdges(final GgnnGraphEdgesVisitor v, final ASTNode node) {
        node.accept(v);
        return v.getEdges();
    }

    protected List<Pair<ASTNode>> getEdges() {
        return edges;
    }

    @Override
    public void visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (!AstNodeUtil.isMetadata(node)) {
                child.accept(this);
            }
        }
    }

    protected boolean shouldBeIgnored(final ASTNode node) {
        return IGNORED_NODE_TYPES.contains(node.getClass());
    }

    @Override
    public void visit(DeclarationStmtList node) {
        // intentionally empty
    }

    @Override
    public void visit(SetStmtList node) {
        // intentionally empty
    }

    @Override
    public void visit(Metadata node) {
        // intentionally empty
    }

    protected Stream<ASTNode> childrenWithoutIgnored(final ASTNode node) {
        return node.getChildren().stream()
            .filter(c -> !AstNodeUtil.isMetadata(c))
            .filter(c -> !shouldBeIgnored(c))
            .filter(c -> {
                if (node instanceof StrId) {
                    return !(c instanceof StringLiteral);
                }
                else {
                    return true;
                }
            })
            .map(ASTNode.class::cast);
    }

    static class ChildEdgesVisitor extends GgnnGraphEdgesVisitor {

        @Override
        public void visit(ASTNode node) {
            childrenWithoutIgnored(node).forEach(child -> edges.add(Pair.of(node, child)));
            super.visit(node);
        }
    }

    static class NextTokenVisitor extends GgnnGraphEdgesVisitor {

        @Override
        public void visit(ASTNode node) {
            List<? extends ASTNode> children = node.getChildren();
            for (int i = 0; i < children.size() - 1; ++i) {
                ASTNode curr = children.get(i);
                ASTNode next = children.get(i + 1);

                if (!ignore(curr, next)) {
                    edges.add(Pair.of(curr, next));
                }
            }

            super.visit(node);
        }

        private boolean ignore(final ASTNode curr, final ASTNode next) {
            boolean isMetadata = AstNodeUtil.isMetadata(curr) || AstNodeUtil.isMetadata(next);
            boolean isIgnored = shouldBeIgnored(curr) || shouldBeIgnored(next);

            return isMetadata || isIgnored;
        }
    }

    private static class GuardedByVisitor extends GgnnGraphEdgesVisitor {

        @Override
        public void visit(IfElseStmt node) {
            DefineableUsesVisitor guardsVisitor = DefineableUsesVisitor.visitNode(node.getBoolExpr());
            DefineableUsesVisitor thenStmtVisitor = DefineableUsesVisitor.visitNode(node.getThenStmts());
            DefineableUsesVisitor elseStmtVisitor = DefineableUsesVisitor.visitNode(node.getElseStmts());

            connectVars(node.getBoolExpr(), guardsVisitor.getVariables(), thenStmtVisitor.getVariables());
            connectVars(node.getBoolExpr(), guardsVisitor.getLists(), thenStmtVisitor.getLists());
            connectAttributes(node.getBoolExpr(), guardsVisitor.getAttributes(), thenStmtVisitor.getAttributes());

            connectVars(node.getBoolExpr(), guardsVisitor.getVariables(), elseStmtVisitor.getVariables());
            connectVars(node.getBoolExpr(), guardsVisitor.getLists(), elseStmtVisitor.getLists());
            connectAttributes(node.getBoolExpr(), guardsVisitor.getAttributes(), elseStmtVisitor.getAttributes());
        }

        @Override
        public void visit(IfThenStmt node) {
            guardedByCBlock(node.getBoolExpr(), node.getThenStmts());
        }

        @Override
        public void visit(RepeatTimesStmt node) {
            guardedByCBlock(node.getTimes(), node.getStmtList());
        }

        @Override
        public void visit(UntilStmt node) {
            guardedByCBlock(node.getBoolExpr(), node.getStmtList());
        }

        private void guardedByCBlock(final Expression guardExpression, final ASTNode body) {
            DefineableUsesVisitor guardsVisitor = DefineableUsesVisitor.visitNode(guardExpression);
            DefineableUsesVisitor usesVisitor = DefineableUsesVisitor.visitNode(body);

            connectVars(guardExpression, guardsVisitor.getVariables(), usesVisitor.getVariables());
            connectVars(guardExpression, guardsVisitor.getLists(), usesVisitor.getLists());
            connectAttributes(guardExpression, guardsVisitor.getAttributes(), usesVisitor.getAttributes());
        }

        private <T extends DataExpr> void connectVars(
            final Expression guardExpression, final Map<String, List<T>> guards,
            final Map<String, List<T>> inBlock
        ) {
            for (Map.Entry<String, List<T>> usedVar : inBlock.entrySet()) {
                if (!guards.containsKey(usedVar.getKey())) {
                    continue;
                }

                for (final T v : usedVar.getValue()) {
                    edges.add(Pair.of(v, guardExpression));
                }
            }
        }

        private void connectAttributes(
            final Expression guardExpression, final List<ASTNode> guards,
            final List<ASTNode> inBlock
        ) {
            for (ASTNode guard : guards) {
                for (ASTNode used : inBlock) {
                    if (guard.equals(used)) {
                        edges.add(Pair.of(used, guardExpression));
                    }
                }
            }
        }
    }

    private static class ComputedFromVisitor extends GgnnGraphEdgesVisitor {

        @Override
        public void visit(ChangeVariableBy node) {
            if (node.getIdentifier() instanceof Qualified qualified) {
                addEdges(qualified, node.getExpr());
            }
        }

        @Override
        public void visit(SetVariableTo node) {
            if (node.getIdentifier() instanceof Qualified qualified) {
                addEdges(qualified, node.getExpr());
            }
        }

        private void addEdges(final Qualified assignTo, final ASTNode expr) {
            DefineableUsesVisitor v = DefineableUsesVisitor.visitNode(expr);
            Stream<Expression> variables = v.getVariables().values().stream().flatMap(List::stream);
            Stream<ASTNode> attributes = v.getAttributes().stream();

            Stream.concat(variables, attributes)
                .forEach(variable -> edges.add(Pair.of(assignTo.getSecond(), variable)));
        }
    }

    private static class ParameterPassingVisitor extends GgnnGraphEdgesVisitor {

        private static final Logger log = Logger.getLogger(ParameterPassingVisitor.class.getName());

        private final ProcedureMapping procedureMapping;

        ParameterPassingVisitor(final Program program) {
            this.procedureMapping = new ProcedureMapping(program);
        }

        @Override
        public void visit(CallStmt node) {
            final Optional<ProcedureDefinition> procedure = procedureMapping.findCalledProcedure(node);
            if (procedure.isEmpty()) {
                // note: This might happen in case the call stmt block was
                // dragged to another sprite. In this case the scratch-vm does
                // not call the procedure in the other sprite, but instead does
                // nothing. Therefore, ignoring this case by not adding an edge
                // is fine.
                log.info("No procedure for calling custom block statement: " + node.getIdent().getName());
            }

            procedure.ifPresent(p -> connectParameters(node, p));
        }

        private void connectParameters(final CallStmt callStmt, final ProcedureDefinition procedure) {
            List<Expression> passedArguments = callStmt.getExpressions().getExpressions();
            List<ParameterDefinition> parameters = procedure.getParameterDefinitionList().getParameterDefinitions();

            if (passedArguments.isEmpty()) {
                edges.add(Pair.of(callStmt, procedure));
            }
            else {
                for (int i = 0; i < passedArguments.size(); ++i) {
                    edges.add(Pair.of(passedArguments.get(i), parameters.get(i)));
                }
            }
        }
    }

    private static class MessagePassingVisitor extends GgnnGraphEdgesVisitor {

        private final Map<String, List<ASTNode>> senders = new HashMap<>();
        private final Map<String, List<ReceptionOfMessage>> receivers = new HashMap<>();

        @Override
        protected List<Pair<ASTNode>> getEdges() {
            List<Pair<ASTNode>> edges = new ArrayList<>();

            for (Map.Entry<String, List<ASTNode>> messageSenders : senders.entrySet()) {
                String message = messageSenders.getKey();
                if (!receivers.containsKey(message)) {
                    continue;
                }

                List<ASTNode> sendingNodes = messageSenders.getValue();
                List<ReceptionOfMessage> receivingNodes = receivers.get(message);

                for (ASTNode sender : sendingNodes) {
                    for (ASTNode receiver : receivingNodes) {
                        edges.add(Pair.of(sender, receiver));
                    }
                }
            }

            return edges;
        }

        @Override
        public void visit(ReceptionOfMessage node) {
            addReceiver(node.getMsg().getMessage().toString(), node);
            super.visit(node);
        }

        @Override
        public void visit(Broadcast node) {
            addSender(node.getMessage().getMessage().toString(), node);
            super.visit(node);
        }

        @Override
        public void visit(BroadcastAndWait node) {
            addSender(node.getMessage().getMessage().toString(), node);
            super.visit(node);
        }

        private void addSender(String message, ASTNode sender) {
            senders.compute(message, (msg, senderList) -> addToListOrCreate(senderList, sender));
        }

        private void addReceiver(String message, ReceptionOfMessage receiver) {
            receivers.compute(message, (msg, receiverList) -> addToListOrCreate(receiverList, receiver));
        }

        private static <T> List<T> addToListOrCreate(List<T> list, T element) {
            List<T> nonNullList = list;
            if (list == null) {
                nonNullList = new ArrayList<>();
            }
            nonNullList.add(element);
            return nonNullList;
        }
    }

    private static class ReturnToVisitor extends GgnnGraphEdgesVisitor {

        @Override
        public void visit(ProcedureDefinition node) {
            final List<Stmt> statements = node.getStmtList().getStmts();
            if (statements.isEmpty()) {
                return;
            }

            final Stmt lastStmt = statements.getLast();
            edges.add(Pair.of(lastStmt, node));
        }
    }
}
