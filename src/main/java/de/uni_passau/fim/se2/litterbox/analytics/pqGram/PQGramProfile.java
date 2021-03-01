package de.uni_passau.fim.se2.litterbox.analytics.pqGram;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

public class PQGramProfile {
    private Bag<LabelTuple> tuples;

    public PQGramProfile() {
        tuples = new HashBag<>();
    }

    public void addLabelTuple(LabelTuple tuple) {
        tuples.add(tuple);
    }

    public Bag<LabelTuple> getTuples() {
        return tuples;
    }

    @Override
    public String toString() {
        return "PQGramProfile{"
                + "tuples=" + tuples + '}';
    }
}
