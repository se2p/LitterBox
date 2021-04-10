package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.LinkedList;
import java.util.List;

public class RefactorSequenceMutation implements Mutation<RefactorSequence> {

    @Override
    public RefactorSequence apply(RefactorSequence refactorSequence) {

        List<Integer> productions = refactorSequence.getProductions();

        // TODO add/ remove or swap a random number based on size of current production list
        List<Integer> mutatedProductions = new LinkedList<>();


        return new RefactorSequence(refactorSequence.getMutation(), refactorSequence.getCrossover(), mutatedProductions);
    }
}
