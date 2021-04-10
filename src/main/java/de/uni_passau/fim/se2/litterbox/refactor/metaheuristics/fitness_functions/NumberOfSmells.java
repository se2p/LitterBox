package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NumberOfSmells implements MinimizingFitnessFunction<RefactorSequence> {

    private final Program program;
    private final List<IssueFinder> issueFinders;
    private final boolean ignoreLooseBlocks;

    public NumberOfSmells(Program program, List<IssueFinder> issueFinders, boolean ignoreLooseBlocks) {
        this.program = program;
        this.issueFinders = issueFinders;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    /**
     * <p>
     * Computes and returns the fitness value of the given solution {@code c}.
     *
     * @param refactorSequence the solution to rate
     * @return the fitness value of the given solution
     * @throws NullPointerException if {@code null} is given
     */
    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program copy = new Program(program.getIdent(), program.getActorDefinitionList(), program.getSymbolTable(),
                program.getProcedureMapping(), program.getProgramMetadata());
        for (Refactoring refactoring : refactorSequence.getProductions()) {
            copy = refactoring.apply(copy);
        }

        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder iF : issueFinders) {
            iF.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(iF.check(program));
        }

        return issues.size();
    }
}
