/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.pqgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LabelTuple {
    private final List<Label> labels;

    public LabelTuple(List<Label> anc, List<Label> sib) {
//        Preconditions.checkArgument(anc.size() == PQGramProfileUtil.getP(),
//                "Too many ancestors for the specified p.");
//        Preconditions.checkArgument(sib.size() == PQGramProfileUtil.getQ(),
//                "Too many siblings for the specified q.");
        labels = new ArrayList<>();
        labels.addAll(anc);
        labels.addAll(sib);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LabelTuple that = (LabelTuple) o;
        return Objects.equals(getLabels(), that.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabels());
    }

    public List<Label> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return labels.toString();
    }
}
