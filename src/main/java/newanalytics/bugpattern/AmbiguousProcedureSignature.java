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
package newanalytics.bugpattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import newanalytics.IssueTool;
import scratch.ast.model.Program;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.symboltable.ProcedureInfo;
import utils.Preconditions;

public class AmbiguousProcedureSignature implements IssueFinder {
    private static final String NOTE1 = "There are no ambiguous procedure signatures in your project.";
    private static final String NOTE2 = "Some of the procedures signatures are ambiguous.";
    public static final String NAME = "ambiguous_procedure_signature";
    public static final String SHORT_NAME = "ambProcSign";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        List<String> found = new ArrayList<>();
        Map<String, Map<Identifier, ProcedureInfo>> procs = program.getProcedureMapping().getProcedures();
        Set<String> actors = procs.keySet();
        for (String actor : actors){
            Map<Identifier, ProcedureInfo> currentMap = procs.get(actor);
            List<ProcedureInfo> procedureInfos = new ArrayList<>(currentMap.values());
            for (int i = 0; i < procedureInfos.size(); i++) {
                ProcedureInfo current = procedureInfos.get(i);
                for (int j = 0; j < procedureInfos.size(); j++) {
                    if (i != j && current.getName().equals(procedureInfos.get(j).getName())
                            && current.getActorName().equals(procedureInfos.get(j).getActorName())) {
                        found.add(current.getActorName());
                    }
                }
            }}
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), IssueTool.getOnlyUniqueActorList(found), notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
