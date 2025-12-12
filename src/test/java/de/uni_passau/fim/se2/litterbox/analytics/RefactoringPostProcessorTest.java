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

import de.uni_passau.fim.se2.litterbox.refactor.postprocessing.DominationStep;
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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefactoringPostProcessorTest {

    @TempDir
    Path tempDir;

    private RefactoringPostProcessor postProcessor;
    private FitnessFunction<RefactorSequence> fitness;

    @BeforeEach
    void setUp() {
        // Mock a fitness function that makes the solution dominate the original
        fitness = mock(FitnessFunction.class);
        // Default to 0.0 for solutions (mocks)
        when(fitness.getFitness(any())).thenReturn(0.0);
        when(fitness.isMinimizing()).thenReturn(true);

        // For the original program (real object created in process), return a high value (worse)
        // We identify the real object because it's not a mock
        when(fitness.getFitness(argThat(seq -> seq != null && !Mockito.mockingDetails(seq).isMock()))).thenReturn(100.0);

        postProcessor = new RefactoringPostProcessor(List.of(fitness));
    }

    @Test
    void testProcessRemovesDeadCode() throws IOException, ParsingException {
        // Create a program with one dead script (Never event) and one live script
        Program program = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);
        when(sequence.getFitnessMap()).thenReturn(Map.of(fitness, -10.0));
        when(sequence.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertFalse(content.contains("dead1"), "Dead code (dead1) should be removed");
        assertTrue(content.contains("greenFlag1"), "Live code (greenFlag1) should be preserved");
        assertTrue(content.contains("wait1"), "Live code (wait1) should be preserved");
    }

    @Test
    void testProcessRemovesControlOnlyScripts() throws IOException, ParsingException {
        // Create a program with a script that only has control structures
        Program program = createProgramWithControlOnlyScript();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);
        when(sequence.getFitnessMap()).thenReturn(Map.of(fitness, -10.0));
        when(sequence.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertFalse(content.contains("greenFlag2"), "Control-only script (greenFlag2) should be removed");
        assertFalse(content.contains("if1"), "Control-only script (if1) should be removed");
    }

    @Test
    void testProcessPreservesWaitUntil() throws IOException, ParsingException {
        // Create a program with a script that has a WaitUntil (which is not ControlStmt)
        Program program = createProgramWithWaitUntilScript();
        RefactorSequence sequence = mock(RefactorSequence.class);
        when(sequence.getRefactoredProgram()).thenReturn(program);
        when(sequence.getFitnessMap()).thenReturn(java.util.Map.of(fitness, -10.0));
        when(sequence.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence);

        postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertTrue(content.contains("greenFlag3"), "WaitUntil script (greenFlag3) should be preserved");
        assertTrue(content.contains("wait2"), "WaitUntil script (wait2) should be preserved");
    }

    @Test
    void testProcessFiltersUniquePrograms() throws IOException {
        // Create shared mocks for ALL components to ensure structural equality
        LocalIdentifier programIdent = mock(LocalIdentifier.class);
        when(programIdent.getName()).thenReturn("test");

        ProgramMetadata programMetadata = mock(ProgramMetadata.class);
        when(programMetadata.getExtension()).thenReturn(new ExtensionMetadata(Collections.emptyList()));
        MetaMetadata metaMetadata = new MetaMetadata("3.0.0", "0.2.0", "Mozilla/5.0");
        when(programMetadata.getMeta()).thenReturn(metaMetadata);
        when(programMetadata.getMonitor()).thenReturn(new MonitorMetadataList(Collections.emptyList()));

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

        de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList setStmtList = new de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList(Collections.emptyList());

        ProcedureDefinitionList procList = mock(ProcedureDefinitionList.class);
        when(procList.getList()).thenReturn(Collections.emptyList());

        SymbolTable symbolTable = new SymbolTable();
        ProcedureDefinitionNameMapping procMapping = new ProcedureDefinitionNameMapping();
        ActorType actorType = ActorType.getStage();
        DeclarationStmtList declList = new DeclarationStmtList(Collections.emptyList());

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
            // when(stage.isStage()).thenReturn(true); - Removed as stage is real object
            ActorDefinitionList actorList = new ActorDefinitionList(List.of(stage));
            return new Program(programIdent, actorList, symbolTable, procMapping, programMetadata);
        };

        Program program1 = programCreator.get();
        Program program2 = programCreator.get();

        RefactorSequence sequence1 = mock(RefactorSequence.class);
        when(sequence1.getRefactoredProgram()).thenReturn(program1);
        when(sequence1.getFitnessMap()).thenReturn(java.util.Map.of(fitness, 1.0));
        when(sequence1.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence1.getCrossover()).thenReturn(mock(Crossover.class));

        RefactorSequence sequence2 = mock(RefactorSequence.class);
        when(sequence2.getRefactoredProgram()).thenReturn(program2);
        when(sequence2.getFitnessMap()).thenReturn(java.util.Map.of(fitness, 2.0)); // Different fitness to pass first filter
        when(sequence2.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence2.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence1);
        sequences.add(sequence2);

        postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

        // Check that only one file was written
        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        assertFalse(tempDir.resolve("test_refactored_1.json").toFile().exists());

        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertTrue(content.contains("greenFlag1"), "Live code (greenFlag1) should be preserved in unique program");
        assertTrue(content.contains("wait1"), "Live code (wait1) should be preserved in unique program");
    }

    // Helper methods to create programs...
    private Program createProgramWithDeadAndLiveScripts() {
        return createProgramWithDeadAndLiveScripts(createDefaultProgramIdent(), createDefaultProgramMetadata());
    }

    private Program createProgramWithDeadAndLiveScripts(LocalIdentifier programIdent, ProgramMetadata programMetadata) {
        // Use TestNever to have a specific ID for verification while preserving structural equality
        Script deadScript = new Script(new TestNever("dead1"), new StmtList());

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
        when(programMetadata.getExtension()).thenReturn(new ExtensionMetadata(Collections.emptyList()));

        MetaMetadata metaMetadata = new MetaMetadata("3.0.0", "0.2.0", "Mozilla/5.0");
        when(programMetadata.getMeta()).thenReturn(metaMetadata);

        when(programMetadata.getMonitor()).thenReturn(new MonitorMetadataList(Collections.emptyList()));
        return programMetadata;
    }

    @Test
    void testProcessFiltersStructurallyEqualPrograms() throws IOException {
        // Create two programs that are structurally equal but different instances
        // We use the helper that creates a fresh program structure each time
        Program program1 = createProgramWithDeadAndLiveScripts();
        Program program2 = createProgramWithDeadAndLiveScripts();

        // Ensure they are NOT the same instance
        assertNotSame(program1, program2);
        // Ensure they ARE structurally equal
        assertTrue(StructuralEquality.areStructurallyEqual(program1, program2));

        FitnessFunction<RefactorSequence> f1 = mock(FitnessFunction.class);

        RefactorSequence sequence1 = mock(RefactorSequence.class);
        when(sequence1.getRefactoredProgram()).thenReturn(program1);
        when(sequence1.getFitnessMap()).thenReturn(java.util.Map.of(fitness, 1.0));
        when(sequence1.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence1.getCrossover()).thenReturn(mock(Crossover.class));

        RefactorSequence sequence2 = mock(RefactorSequence.class);
        when(sequence2.getRefactoredProgram()).thenReturn(program2);
        when(sequence2.getFitnessMap()).thenReturn(java.util.Map.of(fitness, 2.0));
        when(sequence2.getMutation()).thenReturn(mock(Mutation.class));
        when(sequence2.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequence1);
        sequences.add(sequence2);

        postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

        // Check that only one file was written
        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        assertFalse(tempDir.resolve("test_refactored_1.json").toFile().exists(), "Duplicate structurally equal program should be filtered out");

        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertFalse(content.contains("dead1"), "Dead code (dead1) should be removed from structurally equal program");
        assertTrue(content.contains("greenFlag1"), "Live code (greenFlag1) should be preserved in structurally equal program");
    }

    @Test
    void testProcessReportsChanges() throws IOException {
        // Capture logs for the entire analytics package
        Logger logger = Logger.getLogger("de.uni_passau.fim.se2.litterbox");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(outContent, new SimpleFormatter());
        logger.addHandler(handler);

        try {
            Program program = createProgramWithDeadAndLiveScripts();
            RefactorSequence sequence = mock(RefactorSequence.class);
            when(sequence.getRefactoredProgram()).thenReturn(program);
            when(sequence.getFitnessMap()).thenReturn(java.util.Map.of(fitness, 0.0));
            when(sequence.getMutation()).thenReturn(mock(Mutation.class));
            when(sequence.getCrossover()).thenReturn(mock(Crossover.class));

            List<RefactorSequence> sequences = new ArrayList<>();
            sequences.add(sequence);

            postProcessor.process(sequences, createDummyOriginalProgram(), tempDir, Path.of("test.json"));

            handler.flush();
            String output = outContent.toString();

            assertTrue(output.contains("Found 1 solutions"), "Output was: " + output);
            assertTrue(output.contains("Unique solutions: 1"), "Output was: " + output);
            assertTrue(output.contains("Dominating solutions: 1"), "Output was: " + output);
            // We expect details about removed scripts.
            // Since we use createProgramWithDeadAndLiveScripts, it has 1 dead script (Never) and 1 live script.
            // The dead script should be removed.
            assertTrue(output.contains("Removed 1 dead scripts"), "Output was: " + output);
            assertTrue(output.contains("Removed 0 control-only scripts"), "Output was: " + output);

        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void testProcessReturnsNonDominatingIfNoDominatingFound() throws IOException {
        // Setup:
        // Original Program: f1=10, f2=10 (worse is higher)
        // Solution A (Incomparable): f1=5, f2=15 (better in f1, worse in f2) -> Should be returned
        // Solution B (Dominated by Original): f1=15, f2=15 (worse in both) -> Should NOT be returned
        // Solution C (Dominating): None

        FitnessFunction<RefactorSequence> f1 = mock(FitnessFunction.class);
        when(f1.isMinimizing()).thenReturn(true);
        FitnessFunction<RefactorSequence> f2 = mock(FitnessFunction.class);
        when(f2.isMinimizing()).thenReturn(true);

        // Re-initialize postProcessor with 2 fitness functions
        postProcessor = new RefactoringPostProcessor(List.of(f1, f2));

        // Original Program fitness
        Program originalProgram = createDummyOriginalProgram();

        when(f1.getFitness(argThat(seq -> seq != null && seq.getRefactoredProgram() == originalProgram))).thenReturn(10.0);
        when(f2.getFitness(argThat(seq -> seq != null && seq.getRefactoredProgram() == originalProgram))).thenReturn(10.0);

        // Solution A
        Program programA = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequenceA = mock(RefactorSequence.class);
        when(sequenceA.getRefactoredProgram()).thenReturn(programA);
        when(sequenceA.getFitness(f1)).thenReturn(5.0);
        when(sequenceA.getFitness(f2)).thenReturn(15.0);
        when(sequenceA.getFitnessMap()).thenReturn(java.util.Map.of(f1, 5.0, f2, 15.0));
        when(sequenceA.getMutation()).thenReturn(mock(Mutation.class));
        when(sequenceA.getCrossover()).thenReturn(mock(Crossover.class));

        // Solution B
        Program programB = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequenceB = mock(RefactorSequence.class);
        when(sequenceB.getRefactoredProgram()).thenReturn(programB);
        when(sequenceB.getFitness(f1)).thenReturn(15.0);
        when(sequenceB.getFitness(f2)).thenReturn(15.0);
        when(sequenceB.getFitnessMap()).thenReturn(java.util.Map.of(f1, 15.0, f2, 15.0));
        when(sequenceB.getMutation()).thenReturn(mock(Mutation.class));
        when(sequenceB.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequenceA);
        sequences.add(sequenceB);

        // Capture logs to verify we hit the fallback path
        Logger logger = Logger.getLogger(DominationStep.class.getName());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(outContent, new SimpleFormatter());
        logger.addHandler(handler);

        try {
            postProcessor.process(sequences, originalProgram, tempDir, Path.of("test.json"));

            handler.flush();
            String output = outContent.toString();
            assertTrue(output.contains("No solutions found that dominate the original program"), "Should have logged that no dominating solutions were found");
            assertTrue(output.contains("Non-dominated solutions: 1"), "Should have found 1 non-dominated solution");

            // Verify that only Solution A was written (we can check file existence)
            // Note: The file naming depends on the index in the filtered list.
            assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
            assertFalse(tempDir.resolve("test_refactored_1.json").toFile().exists());

        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void testProcessFiltersEqualSolutionsWhenNoDominatingFound() throws IOException {
        // Setup:
        // Original Program: f1=10, f2=10
        // Solution A (Equal): f1=10, f2=10 -> Should NOT be returned
        // Solution B (Better in one): f1=5, f2=15 -> Should be returned
        // Solution C (Worse in both): f1=15, f2=15 -> Should NOT be returned (dominated)

        FitnessFunction<RefactorSequence> f1 = mock(FitnessFunction.class);
        when(f1.isMinimizing()).thenReturn(true);
        FitnessFunction<RefactorSequence> f2 = mock(FitnessFunction.class);
        when(f2.isMinimizing()).thenReturn(true);

        postProcessor = new RefactoringPostProcessor(List.of(f1, f2));

        Program originalProgram = createDummyOriginalProgram();
        when(f1.getFitness(any())).thenAnswer(inv -> {
            java.util.logging.Logger.getAnonymousLogger().warning("Mock f1 called with: " + inv.getArgument(0));
            return 10.0;
        });
        when(f2.getFitness(any())).thenAnswer(inv -> {
            java.util.logging.Logger.getAnonymousLogger().warning("Mock f2 called with: " + inv.getArgument(0));
            return 10.0;
        });

        // Solution A (Equal)
        Program programA = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequenceA = mock(RefactorSequence.class);
        when(sequenceA.getRefactoredProgram()).thenReturn(programA);
        doReturn(5.0).when(sequenceA).getFitness(f1);
        doReturn(15.0).when(sequenceA).getFitness(f2);
        when(sequenceA.getFitnessMap()).thenReturn(java.util.Map.of(f1, 5.0, f2, 15.0));
        when(sequenceA.getMutation()).thenReturn(mock(Mutation.class));
        when(sequenceA.getCrossover()).thenReturn(mock(Crossover.class));

        when(sequenceA.toString()).thenReturn("SequenceA");
        // Solution B (Better in one)
        Program programB = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequenceB = mock(RefactorSequence.class);
        when(sequenceB.toString()).thenReturn("SequenceB");
        when(sequenceB.getRefactoredProgram()).thenReturn(programB);
        doReturn(5.0).when(sequenceB).getFitness(f1);
        doReturn(15.0).when(sequenceB).getFitness(f2);
        when(sequenceB.getFitnessMap()).thenReturn(java.util.Map.of(f1, 5.0, f2, 15.0));
        when(sequenceB.getMutation()).thenReturn(mock(Mutation.class));
        when(sequenceB.getCrossover()).thenReturn(mock(Crossover.class));

        // Solution C (Dominated)
        Program programC = createProgramWithDeadAndLiveScripts();
        RefactorSequence sequenceC = mock(RefactorSequence.class);
        when(sequenceC.toString()).thenReturn("SequenceC");
        when(sequenceC.getRefactoredProgram()).thenReturn(programC);
        doReturn(15.0).when(sequenceC).getFitness(f1);
        doReturn(15.0).when(sequenceC).getFitness(f2);
        when(sequenceC.getFitnessMap()).thenReturn(java.util.Map.of(f1, 15.0, f2, 15.0));
        when(sequenceC.getMutation()).thenReturn(mock(Mutation.class));
        when(sequenceC.getCrossover()).thenReturn(mock(Crossover.class));

        List<RefactorSequence> sequences = new ArrayList<>();
        sequences.add(sequenceA);
        sequences.add(sequenceB);
        sequences.add(sequenceC);

        postProcessor.process(sequences, originalProgram, tempDir, Path.of("test.json"));

        assertTrue(tempDir.resolve("test_refactored_0.json").toFile().exists());
        assertFalse(tempDir.resolve("test_refactored_1.json").toFile().exists());

        String content = Files.readString(tempDir.resolve("test_refactored_0.json"));
        assertTrue(content.contains("greenFlag1"), "Live code (greenFlag1) should be preserved");
        assertFalse(content.contains("dead1"), "Dead code (dead1) should be removed");
    }



    private Program createProgramWithScripts(ScriptList scripts, LocalIdentifier programIdent, ProgramMetadata programMetadata) {
        LocalIdentifier stageIdent = mock(LocalIdentifier.class);
        when(stageIdent.getName()).thenReturn("Stage");

        ActorMetadata actorMetadata = mock(ActorMetadata.class);
        when(actorMetadata.getCommentsMetadata()).thenReturn(new CommentMetadataList(Collections.emptyList()));
        when(actorMetadata.getCostumes()).thenReturn(new ImageMetadataList(Collections.emptyList()));
        when(actorMetadata.getSounds()).thenReturn(new SoundMetadataList(Collections.emptyList()));
        when(actorMetadata.getCurrentCostume()).thenReturn(0);

        SetStmtList setStmtList = new SetStmtList(Collections.emptyList());

        ProcedureDefinitionList procList = new ProcedureDefinitionList(Collections.emptyList());

        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                stageIdent,
                new DeclarationStmtList(Collections.emptyList()),
                setStmtList,
                procList,
                scripts,
                actorMetadata
        );
        // when(stage.isStage()).thenReturn(true); - Removed

        ActorDefinitionList actorList = new ActorDefinitionList(List.of(stage));

        return new Program(
                programIdent,
                actorList,
                new SymbolTable(),
                new ProcedureDefinitionNameMapping(),
                programMetadata
        );
    }

    private Program createDummyOriginalProgram() {
        Program program = mock(Program.class);
        ActorDefinitionList actorList = mock(ActorDefinitionList.class);
        when(actorList.getDefinitions()).thenReturn(Collections.emptyList());
        when(program.getActorDefinitionList()).thenReturn(actorList);
        return program;
    }

    // Inner class to allow setting ID on Never event for testing
    static class TestNever extends Never {
        private final TopNonDataBlockMetadata metadata;

        public TestNever(String id) {
            this.metadata = mock(TopNonDataBlockMetadata.class);
            when(this.metadata.getBlockId()).thenReturn(id);
        }

        @Override
        public BlockMetadata getMetadata() {
            return metadata;
        }

        // AbstractNode.equals checks children. Since Never has no children,
        // two TestNever instances are equal if they are same class (TestNever).
        // This satisfies StructuralEquality.
    }
}
