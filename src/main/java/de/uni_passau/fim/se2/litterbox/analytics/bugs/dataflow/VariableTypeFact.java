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
package de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow;

import de.uni_passau.fim.se2.litterbox.analytics.bugs.Type;
import de.uni_passau.fim.se2.litterbox.cfg.DataflowFact;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;

import java.util.Objects;

public class VariableTypeFact implements DataflowFact {
    private final Defineable variable;
    private final Type type;

    public VariableTypeFact(Defineable variable, Type type) {
        this.variable = variable;
        this.type = type;
    }

    public Defineable getVariable() {
        return variable;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableTypeFact that = (VariableTypeFact) o;
        return Objects.equals(variable, that.variable) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, type);
    }

    @Override
    public String toString() {
        return variable + " -> " + type;
    }
}
