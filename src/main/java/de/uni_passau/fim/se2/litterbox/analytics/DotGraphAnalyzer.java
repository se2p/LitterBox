/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.DotVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.*;

public class DotGraphAnalyzer implements ProgramAnalyzer<String> {

    private final GraphType graphType;

    public DotGraphAnalyzer(GraphType graphType) {
        this.graphType = graphType;
    }

    public DotGraphAnalyzer() {
        this(GraphType.AST);
    }

    @Override
    public String analyze(Program program) {
        if (graphType == GraphType.AST) {
            return DotVisitor.buildDotGraph(program);
        }

        ControlFlowGraphVisitor cfgVisitor = new ControlFlowGraphVisitor(program, null);
        cfgVisitor.visit(program);
        ControlFlowGraph cfg = cfgVisitor.getControlFlowGraph();

        return switch (graphType) {
            case CFG -> cfg.toDotString();
            case CDG -> new ControlDependenceGraph(cfg).toDotString();
            case DDG -> new DataDependenceGraph(cfg).toDotString();
            case DT -> new DominatorTree(cfg).toDotString();
            case PDT -> new PostDominatorTree(cfg).toDotString();
            case TDG -> new TimeDependenceGraph(cfg).toDotString();
            case PDG -> new ProgramDependenceGraph(cfg).toDotString();
            default -> throw new IllegalStateException("Unexpected value: " + graphType);
        };
    }
}
