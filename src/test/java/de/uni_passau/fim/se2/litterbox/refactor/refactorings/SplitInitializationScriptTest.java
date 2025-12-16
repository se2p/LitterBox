/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitInitializationScriptFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitInitializationScriptTest {

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
    public void testSplitInitialization() {
        // Given: A script with initialization (GoTo, SetVar) followed by a loop
        GoToPosXY goTo = new GoToPosXY(new UnspecifiedNumExpr(), new UnspecifiedNumExpr(), new NoBlockMetadata());
        SetVariableTo setVar = new SetVariableTo(new StrId("var"), new UnspecifiedStringExpr(), new NoBlockMetadata());
        RepeatForeverStmt loop = new RepeatForeverStmt(new StmtList(new Say(new UnspecifiedStringExpr(), new NoBlockMetadata())), new NoBlockMetadata());
        
        Script script = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(goTo, setVar, loop));
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitInitializationScriptFinder finder = new SplitInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.getFirst() instanceof SplitInitializationScript);
        
        SplitInitializationScript refactoring = (SplitInitializationScript) refactorings.getFirst();
        
        // And: Applying the refactoring should result in two scripts
        Program refactoredProgram = (Program) refactoring.apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().getFirst().getScripts();
        assertEquals(2, result.getScriptList().size());
        
        Script script1 = result.getScriptList().getFirst();
        Script script2 = result.getScriptList().get(1);
        
        assertEquals(2, script1.getStmtList().getNumberOfStatements()); // GoTo, SetVar
        assertEquals(1, script2.getStmtList().getNumberOfStatements()); // Loop
        
        assertTrue(script1.getStmtList().getStmts().getFirst() instanceof GoToPosXY);
        assertTrue(script1.getStmtList().getStmts().get(1) instanceof SetVariableTo);
        assertTrue(script2.getStmtList().getStmts().getFirst() instanceof RepeatForeverStmt);
    }
    
    @Test
    public void testNoSplitIfNoTimeConsuming() {
        // Given: A script with only initialization
        GoToPosXY goTo = new GoToPosXY(new UnspecifiedNumExpr(), new UnspecifiedNumExpr(), new NoBlockMetadata());
        SetVariableTo setVar = new SetVariableTo(new StrId("var"), new UnspecifiedStringExpr(), new NoBlockMetadata());
        
        Script script = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(goTo, setVar));
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitInitializationScriptFinder finder = new SplitInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: No refactoring should be found
        assertEquals(0, refactorings.size());
    }

    @Test
    public void testNoSplitIfNoInitialization() {
        // Given: A script with only loop
        RepeatForeverStmt loop = new RepeatForeverStmt(new StmtList(new Say(new UnspecifiedStringExpr(), new NoBlockMetadata())), new NoBlockMetadata());
        
        Script script = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop));
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitInitializationScriptFinder finder = new SplitInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: No refactoring should be found
        assertEquals(0, refactorings.size());
    }
    @Test
    public void testSplitInitializationWithScratchBlocks() {
        // Given: A script with initialization and loop defined using scratchblocks
        String code = """
                when green flag clicked
                go to x: (0) y: (0)
                set [var v] to [0]
                forever
                    say [Hello!]
                end
                """;
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Script script = parser.parseScript("Stage", code);
        ScriptList scriptList = new ScriptList(List.of(script));
        Program program = createProgram(scriptList);

        // When: Finder is run
        SplitInitializationScriptFinder finder = new SplitInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.getFirst() instanceof SplitInitializationScript);

        // And: Applying the refactoring should result in two scripts
        Program refactoredProgram = (Program) refactorings.getFirst().apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().getFirst().getScripts();
        assertEquals(2, result.getScriptList().size());
    }
}
