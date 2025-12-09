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

import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeInitializationScriptFinder;
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
import de.uni_passau.fim.se2.litterbox.scratchblocks.ActorContent;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergeInitializationScriptTest {

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
    public void testMergeInitialization() {
        // Given: Script 1 (Init), Script 2 (Loop)
        GoToPosXY goTo = new GoToPosXY(new UnspecifiedNumExpr(), new UnspecifiedNumExpr(), new NoBlockMetadata());
        SetVariableTo setVar = new SetVariableTo(new StrId("var"), new UnspecifiedStringExpr(), new NoBlockMetadata());
        Script script1 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(goTo, setVar));

        RepeatForeverStmt loop = new RepeatForeverStmt(new StmtList(new Say(new UnspecifiedStringExpr(), new NoBlockMetadata())), new NoBlockMetadata());
        Script script2 = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(loop));
        
        ScriptList scriptList = new ScriptList(List.of(script1, script2));
        Program program = createProgram(scriptList);

        // When: Finder is run
        MergeInitializationScriptFinder finder = new MergeInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.getFirst() instanceof MergeInitializationScript);
        
        MergeInitializationScript refactoring = (MergeInitializationScript) refactorings.getFirst();
        
        // And: Applying the refactoring should result in one script
        Program refactoredProgram = (Program) refactoring.apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().getFirst().getScripts();
        assertEquals(1, result.getScriptList().size());
        
        Script mergedScript = result.getScriptList().getFirst();
        assertEquals(3, mergedScript.getStmtList().getNumberOfStatements()); // GoTo, SetVar, Loop
        
        assertTrue(mergedScript.getStmtList().getStmts().getFirst() instanceof GoToPosXY);
        assertTrue(mergedScript.getStmtList().getStmts().get(1) instanceof SetVariableTo);
        assertTrue(mergedScript.getStmtList().getStmts().get(2) instanceof RepeatForeverStmt);
    }
    @Test
    public void testMergeInitializationWithScratchBlocks() {
        // Given: Script 1 (Init), Script 2 (Loop) defined using scratchblocks
        String code = """
                when green flag clicked
                go to x: (0) y: (0)
                set [var v] to [0]
                
                when green flag clicked
                forever
                    say [Hello!]
                end
                """;
        ScratchBlocksParser parser = new ScratchBlocksParser();
        ActorContent content = parser.parseActorContent(code);
        Program program = createProgram(content.scripts());

        // When: Finder is run
        MergeInitializationScriptFinder finder = new MergeInitializationScriptFinder();
        List<Refactoring> refactorings = finder.check(program);

        // Then: One refactoring should be found
        assertEquals(1, refactorings.size());
        assertTrue(refactorings.getFirst() instanceof MergeInitializationScript);

        // And: Applying the refactoring should result in one script
        Program refactoredProgram = (Program) refactorings.getFirst().apply(program);
        ScriptList result = refactoredProgram.getActorDefinitionList().getDefinitions().getFirst().getScripts();
        assertEquals(1, result.getScriptList().size());
    }
}
