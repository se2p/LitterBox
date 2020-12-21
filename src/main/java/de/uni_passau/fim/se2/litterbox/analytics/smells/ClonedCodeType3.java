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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CodeClone;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.NormalizationVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.List;
import java.util.stream.Collectors;

public class ClonedCodeType3 extends ClonedCode {

    public ClonedCodeType3() {
        super(CodeClone.CloneType.TYPE3, "clone_type_3");
    }

    @Override
    protected boolean compareStatements(List<Stmt> statements1, List<Stmt> statements2) {
        NormalizationVisitor normalizationVisitor = new NormalizationVisitor();
        List<Stmt> normalizedStatements1 = statements1.stream().map(normalizationVisitor::apply).collect(Collectors.toList());
        List<Stmt> normalizedStatements2 = statements2.stream().map(normalizationVisitor::apply).collect(Collectors.toList());

        return normalizedStatements1.equals(normalizedStatements2);
    }

}
