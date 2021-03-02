package de.uni_passau.fim.se2.litterbox.analytics.pqGram;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class PQGramProfile {
    private Multiset<LabelTuple> tuples;

    public PQGramProfile() {
        tuples = HashMultiset.create();
    }

    public void addLabelTuple(LabelTuple tuple) {
        tuples.add(tuple);
    }

    public Multiset<LabelTuple> getTuples() {
        return tuples;
    }

    @Override
    public String toString() {
        return "PQGramProfile{"
                + "tuples=" + tuples + '}';
    }
}
