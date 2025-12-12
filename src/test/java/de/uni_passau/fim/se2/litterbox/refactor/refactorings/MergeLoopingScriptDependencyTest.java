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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeLoopingScriptFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergeLoopingScriptDependencyTest {

    private Program createProgram(ScriptList scriptList) {
        ActorMetadata actorMetadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(Collections.emptyList()),
                new SoundMetadataList(Collections.emptyList())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                scriptList,
                actorMetadata
        );

        ProgramMetadata programMetadata = new ProgramMetadata(
                new MonitorMetadataList(Collections.emptyList()),
                new ExtensionMetadata(Collections.emptyList()),
                new MetaMetadata("3.0.0", "0.2.0", "Mozilla/5.0")
        );

        return new Program(
                new StrId("Program"),
                new ActorDefinitionList(List.of(actor)),
                null,
                null,
                programMetadata
        );
    }

    @Test
    public void testMergeLoopingScriptWithSharedVariable() {
        // Given: Two scripts with Forever loops that modify the same variable
        // Script 1: Forever { Set x to 1 }
        RepeatForeverStmt loop1 = new RepeatForeverStmt(
            new StmtList(new SetVariableTo(new StrId("x"), new NumberLiteral(1.0), new NoBlockMetadata())), 
            new NoBlockMetadata()
        );
        Script script1 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop1));
        
        // Script 2: Forever { Set x to 2 }
        RepeatForeverStmt loop2 = new RepeatForeverStmt(
            new StmtList(new SetVariableTo(new StrId("x"), new NumberLiteral(2.0), new NoBlockMetadata())), 
            new NoBlockMetadata()
        );
        Script script2 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop2));
        
        ScriptList scriptList = new ScriptList(List.of(script1, script2));
        Program program = createProgram(scriptList);

        // When: Finder is run
        MergeLoopingScriptFinder finder = new MergeLoopingScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: No refactoring should be found because of dependency
        assertEquals(0, refactorings.size(), "Should not merge scripts with shared variable dependencies");
    }

    @Test
    public void testMergeLoopingScriptWithIndependentVariables() {
        // Given: Two scripts with Forever loops that modify DIFFERENT variables
        // Script 1: Forever { Set x to 1 }
        RepeatForeverStmt loop1 = new RepeatForeverStmt(
            new StmtList(new SetVariableTo(new StrId("x"), new NumberLiteral(1.0), new NoBlockMetadata())), 
            new NoBlockMetadata()
        );
        Script script1 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop1));
        
        // Script 2: Forever { Set y to 2 }
        RepeatForeverStmt loop2 = new RepeatForeverStmt(
            new StmtList(new SetVariableTo(new StrId("y"), new NumberLiteral(2.0), new NoBlockMetadata())), 
            new NoBlockMetadata()
        );
        Script script2 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop2));
        
        ScriptList scriptList = new ScriptList(List.of(script1, script2));
        Program program = createProgram(scriptList);

        // When: Finder is run
        MergeLoopingScriptFinder finder = new MergeLoopingScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size(), "Should merge independent scripts");
    }
}
