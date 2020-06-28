/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * Names for custom blocks are non-unique. Two custom blocks with the same name can only be distinguished if they have a
 * different number or order of parameters.
 * When two blocks have the same name and parameter order, no matter which call block is used, the program will
 * always call the matching custom block which was defined earlier.
 */
public class AmbiguousCustomBlockSignature implements IssueFinder, ScratchVisitor {
    public static final String NAME = "ambiguous_custom_block_signature";
    public static final String SHORT_NAME = "ambCustBlSign";
    public static final String HINT_TEXT = "ambiguous custom block signature";
    private int count = 0;
    private Set<Issue> issues = new LinkedHashSet<>();
    private ActorDefinition currentActor;
    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        if (!node.getStmtList().getStmts().isEmpty()) {
            checkProc(node);
        }

        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    private void checkProc(ProcedureDefinition node) {
        LocalIdentifier ident = node.getIdent();
        List<ProcedureInfo> procedureInfos = new ArrayList<>(procMap.values());
        ProcedureInfo current = procMap.get(ident);
        for (ProcedureInfo procedureInfo : procedureInfos) {
            if (procedureInfo != current && current.getName().equals(procedureInfo.getName())
                    && current.getActorName().equals(procedureInfo.getActorName())) {
                issues.add(new Issue(this, currentActor, ident));
                count++;
                addBlockComment((NonDataBlockMetadata) node.getMetadata().getDefinition(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
