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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefactoringPostProcessorTest {

    @TempDir
    Path tempDir;

    private RefactoringPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        postProcessor = new RefactoringPostProcessor();
    }

    @Test
    void testProcessRemovesDeadCode() throws IOException {
        // Create a program with one dead script (Never event) and one live script
        Program program = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, tempDir, Path.of("test.json"));

        // Verify that the dead script was removed
        // Stage is the first actor in our helper
        assertEquals(1, program.getActorDefinitionList().getDefinitions().get(0).getScripts().getSize());
        assertTrue(program.getActorDefinitionList().getDefinitions().get(0).getScripts().getScriptList().get(0).getEvent() instanceof GreenFlag);
    }

    @Test
    void testProcessRemovesControlOnlyScripts() throws IOException {
        // Create a program with a script that only has control structures
        Program program = createProgramWithControlOnlyScript();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, tempDir, Path.of("test.json"));

        // Verify that the script was removed
        assertEquals(0, program.getActorDefinitionList().getDefinitions().get(0).getScripts().getSize());
    }

    @Test
    void testProcessPreservesWaitUntil() throws IOException {
        // Create a program with a script that has a WaitUntil (which is not ControlStmt)
        Program program = createProgramWithWaitUntilScript();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, tempDir, Path.of("test.json"));

        // Verify that the script was NOT removed
        assertEquals(1, program.getActorDefinitionList().getDefinitions().get(0).getScripts().getSize());
    }

    @Test
    void testProcessFiltersUniquePrograms() throws IOException {
        // Create shared mocks for ALL components to ensure structural equality
        LocalIdentifier programIdent = mock(LocalIdentifier.class);
        when(programIdent.getName()).thenReturn("test");
        
        ProgramMetadata programMetadata = mock(ProgramMetadata.class);
        when(programMetadata.getExtension()).thenReturn(mock(ExtensionMetadata.class));
        when(programMetadata.getExtension().getExtensionNames()).thenReturn(Collections.emptyList());
        MetaMetadata metaMetadata = mock(MetaMetadata.class);
        when(metaMetadata.getSemver()).thenReturn("3.0.0");
        when(metaMetadata.getVm()).thenReturn("0.2.0");
        when(metaMetadata.getAgent()).thenReturn("Mozilla/5.0");
        when(programMetadata.getMeta()).thenReturn(metaMetadata);
        when(programMetadata.getMonitor()).thenReturn(mock(MonitorMetadataList.class));
        when(programMetadata.getMonitor().getList()).thenReturn(Collections.emptyList());

        LocalIdentifier stageIdent = mock(LocalIdentifier.class);
        when(stageIdent.getName()).thenReturn("Stage");

        ActorMetadata actorMetadata = mock(ActorMetadata.class);
        when(actorMetadata.getCommentsMetadata()).thenReturn(mock(CommentMetadataList.class));
        when(actorMetadata.getCommentsMetadata().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getCostumes()).thenReturn(mock(ImageMetadataList.class));
        when(actorMetadata.getCostumes().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getSounds()).thenReturn(mock(SoundMetadataList.class));
        when(actorMetadata.getSounds().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getCurrentCostume()).thenReturn(0);

        de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList setStmtList = mock(de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList.class);
        when(setStmtList.getStmts()).thenReturn(Collections.emptyList());

        ProcedureDefinitionList procList = mock(ProcedureDefinitionList.class);
        when(procList.getList()).thenReturn(Collections.emptyList());

        SymbolTable symbolTable = mock(SymbolTable.class);
        ProcedureDefinitionNameMapping procMapping = mock(ProcedureDefinitionNameMapping.class);
        ActorType actorType = mock(ActorType.class);
        DeclarationStmtList declList = mock(DeclarationStmtList.class);

        // Create scripts (shared)
        Script deadScript = new Script(new Never(), new StmtList());
        TopNonDataBlockMetadata greenFlagMeta = mock(TopNonDataBlockMetadata.class);
        when(greenFlagMeta.getBlockId()).thenReturn("greenFlag1");
        NonDataBlockMetadata waitMeta = mock(NonDataBlockMetadata.class);
        when(waitMeta.getBlockId()).thenReturn("wait1");
        WaitUntil waitStmt = new WaitUntil(mock(BoolExpr.class), waitMeta);
        Script liveScript = new Script(new GreenFlag(greenFlagMeta), new StmtList(waitStmt));
        ScriptList scripts = new ScriptList(new ArrayList<>(List.of(deadScript, liveScript)));

        // Helper to create program with these exact mocks
        java.util.function.Supplier<Program> programCreator = () -> {
            ActorDefinition stage = new ActorDefinition(
                    actorType, 
                    stageIdent, 
                    declList,
                    setStmtList,
                    procList,
                    scripts, // Shared scripts
                    actorMetadata
            );
            when(stage.isStage()).thenReturn(true);
            ActorDefinitionList actorList = new ActorDefinitionList(List.of(stage));
            return new Program(programIdent, actorList, symbolTable, procMapping, programMetadata);
        };

        Program program1 = programCreator.get();
        Program program2 = programCreator.get();

        RefactorSequence sequence1 = mock(RefactorSequence.class);
        when(sequence1.getRefactoredProgram()).thenReturn(program1);

        RefactorSequence sequence2 = mock(RefactorSequence.class);
        when(sequence2.getRefactoredProgram()).thenReturn(program2);

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence1);
        sequences.add(sequence2);

        postProcessor.process(sequences, tempDir, Path.of("test.json"));

        // Check that only one file was written
        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        assertFalse(tempDir.resolve("test_refactored_1.json").toFile().exists());
    }

    // Helper methods to create programs...
    private Program createProgramWithDeadAndLiveScripts() {
        return createProgramWithDeadAndLiveScripts(createDefaultProgramIdent(), createDefaultProgramMetadata());
    }

    private Program createProgramWithDeadAndLiveScripts(LocalIdentifier programIdent, ProgramMetadata programMetadata) {
        Script deadScript = new Script(new Never(), new StmtList());
        
        // Live script needs a non-control statement to survive "control-only" filtering
        TopNonDataBlockMetadata greenFlagMeta = mock(TopNonDataBlockMetadata.class);
        when(greenFlagMeta.getBlockId()).thenReturn("greenFlag1");
        
        NonDataBlockMetadata waitMeta = mock(NonDataBlockMetadata.class);
        when(waitMeta.getBlockId()).thenReturn("wait1");
        
        WaitUntil waitStmt = new WaitUntil(mock(BoolExpr.class), waitMeta);
        Script liveScript = new Script(new GreenFlag(greenFlagMeta), new StmtList(waitStmt));
        
        ScriptList scripts = new ScriptList(new ArrayList<>(List.of(deadScript, liveScript)));
        return createProgramWithScripts(scripts, programIdent, programMetadata);
    }

    private Program createProgramWithControlOnlyScript() {
        // IfThenStmt with empty body
        TopNonDataBlockMetadata greenFlagMeta = mock(TopNonDataBlockMetadata.class);
        when(greenFlagMeta.getBlockId()).thenReturn("greenFlag2");
        
        NonDataBlockMetadata ifMeta = mock(NonDataBlockMetadata.class);
        when(ifMeta.getBlockId()).thenReturn("if1");
        
        IfThenStmt ifStmt = new IfThenStmt(mock(BoolExpr.class), new StmtList(), ifMeta);
        Script controlScript = new Script(new GreenFlag(greenFlagMeta), new StmtList(ifStmt));
        
        ScriptList scripts = new ScriptList(new ArrayList<>(List.of(controlScript)));
        return createProgramWithScripts(scripts, createDefaultProgramIdent(), createDefaultProgramMetadata());
    }

    private Program createProgramWithWaitUntilScript() {
        TopNonDataBlockMetadata greenFlagMeta = mock(TopNonDataBlockMetadata.class);
        when(greenFlagMeta.getBlockId()).thenReturn("greenFlag3");
        
        NonDataBlockMetadata waitMeta = mock(NonDataBlockMetadata.class);
        when(waitMeta.getBlockId()).thenReturn("wait2");
        
        WaitUntil waitStmt = new WaitUntil(mock(BoolExpr.class), waitMeta);
        Script waitScript = new Script(new GreenFlag(greenFlagMeta), new StmtList(waitStmt));
        
        ScriptList scripts = new ScriptList(new ArrayList<>(List.of(waitScript)));
        return createProgramWithScripts(scripts, createDefaultProgramIdent(), createDefaultProgramMetadata());
    }

    private LocalIdentifier createDefaultProgramIdent() {
        LocalIdentifier programIdent = mock(LocalIdentifier.class);
        when(programIdent.getName()).thenReturn("test");
        return programIdent;
    }

    private ProgramMetadata createDefaultProgramMetadata() {
        ProgramMetadata programMetadata = mock(ProgramMetadata.class);
        when(programMetadata.getExtension()).thenReturn(mock(ExtensionMetadata.class));
        when(programMetadata.getExtension().getExtensionNames()).thenReturn(Collections.emptyList());
        
        MetaMetadata metaMetadata = mock(MetaMetadata.class);
        when(metaMetadata.getSemver()).thenReturn("3.0.0");
        when(metaMetadata.getVm()).thenReturn("0.2.0");
        when(metaMetadata.getAgent()).thenReturn("Mozilla/5.0");
        when(programMetadata.getMeta()).thenReturn(metaMetadata);

        when(programMetadata.getMonitor()).thenReturn(mock(MonitorMetadataList.class));
        when(programMetadata.getMonitor().getList()).thenReturn(Collections.emptyList());
        return programMetadata;
    }

    private Program createProgramWithScripts(ScriptList scripts, LocalIdentifier programIdent, ProgramMetadata programMetadata) {
        LocalIdentifier stageIdent = mock(LocalIdentifier.class);
        when(stageIdent.getName()).thenReturn("Stage");

        ActorMetadata actorMetadata = mock(ActorMetadata.class);
        when(actorMetadata.getCommentsMetadata()).thenReturn(mock(CommentMetadataList.class));
        when(actorMetadata.getCommentsMetadata().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getCostumes()).thenReturn(mock(ImageMetadataList.class));
        when(actorMetadata.getCostumes().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getSounds()).thenReturn(mock(SoundMetadataList.class));
        when(actorMetadata.getSounds().getList()).thenReturn(Collections.emptyList());
        when(actorMetadata.getCurrentCostume()).thenReturn(0);

        de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList setStmtList = mock(de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList.class);
        when(setStmtList.getStmts()).thenReturn(Collections.emptyList());

        ProcedureDefinitionList procList = mock(ProcedureDefinitionList.class);
        when(procList.getList()).thenReturn(Collections.emptyList());

        ActorDefinition stage = new ActorDefinition(
                mock(ActorType.class), 
                stageIdent, 
                mock(DeclarationStmtList.class),
                setStmtList,
                procList,
                scripts,
                actorMetadata
        );
        when(stage.isStage()).thenReturn(true);
        
        ActorDefinitionList actorList = new ActorDefinitionList(List.of(stage));
        
        return new Program(
                programIdent,
                actorList,
                mock(SymbolTable.class),
                mock(ProcedureDefinitionNameMapping.class),
                programMetadata
        );
    }
}
