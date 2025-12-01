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

import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitLoopingScriptFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitLoopingScriptTest {

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
    public void testSplitLoopingScript() {
        // Given: A script with a loop containing two independent If statements
        IfThenStmt if1 = new IfThenStmt(new BoolLiteral(true), new StmtList(new Say(new StringLiteral("a"), new NoBlockMetadata())), new NoBlockMetadata());
        IfThenStmt if2 = new IfThenStmt(new BoolLiteral(true), new StmtList(new Say(new StringLiteral("b"), new NoBlockMetadata())), new NoBlockMetadata());
        
        RepeatForeverStmt loop = new RepeatForeverStmt(new StmtList(if1, if2), new NoBlockMetadata());
        Script script = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop));
        
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitLoopingScriptFinder finder = new SplitLoopingScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.get(0) instanceof SplitLoopingScript);
        
        SplitLoopingScript refactoring = (SplitLoopingScript) refactorings.get(0);
        
        // And: Applying the refactoring should result in two scripts
        Program refactoredProgram = (Program) refactoring.apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().get(0).getScripts();
        assertEquals(2, result.getScriptList().size());
        
        Script script1 = result.getScriptList().get(0);
        Script script2 = result.getScriptList().get(1);
        
        assertEquals(1, script1.getStmtList().getNumberOfStatements()); // Loop
        assertEquals(1, script2.getStmtList().getNumberOfStatements()); // Loop
        
        assertTrue(script1.getStmtList().getStmts().get(0) instanceof RepeatForeverStmt);
        assertTrue(script2.getStmtList().getStmts().get(0) instanceof RepeatForeverStmt);
        
        RepeatForeverStmt loop1 = (RepeatForeverStmt) script1.getStmtList().getStmts().get(0);
        RepeatForeverStmt loop2 = (RepeatForeverStmt) script2.getStmtList().getStmts().get(0);
        
        assertEquals(1, loop1.getStmtList().getNumberOfStatements());
        assertEquals(1, loop2.getStmtList().getNumberOfStatements());
        
        assertTrue(loop1.getStmtList().getStmts().get(0) instanceof IfThenStmt);
        assertTrue(loop2.getStmtList().getStmts().get(0) instanceof IfThenStmt);
    }
    @Test
    public void testSplitLoopingWithScratchBlocks() {
        // Given: A script with a loop containing two independent If statements defined using scratchblocks
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        say [Hello!]
                    end
                    if <mouse down?> then
                        say [Bye!]
                    end
                end
                """;
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Script script = parser.parseScript("Stage", code);
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitLoopingScriptFinder finder = new SplitLoopingScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.get(0) instanceof SplitLoopingScript);

        // And: Applying the refactoring should result in two scripts
        Program refactoredProgram = (Program) refactorings.get(0).apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().get(0).getScripts();
        assertEquals(2, result.getScriptList().size());
    }
}
