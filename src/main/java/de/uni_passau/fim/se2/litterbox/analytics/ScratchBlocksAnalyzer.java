/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ScratchBlocksAnalyzer extends Analyzer<String> {
    private static final Logger log = Logger.getLogger(ExtractionAnalyzer.class.getName());

    public ScratchBlocksAnalyzer(Path input, Path output, boolean delete) {
        super(input, output, delete);
    }

    public ScratchBlocksAnalyzer() {
        super(null, null, false);
    }

    @Override
    public String check(Program program) {
        ScratchBlocksVisitor vis = new ScratchBlocksVisitor();
        vis.setAddActorNames(true);
        program.accept(vis);
        return vis.getScratchBlocks();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, String checkResult) throws IOException {

    }
}
