/*
 * Copyright (C) 2019 LitterBox contributors
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

package de.uni_passau.fim.se2.litterbox.cfg;


import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;

import java.util.Objects;

public class Variable implements Defineable {

    private Qualified qualified;

    private String first;

    private String second;

    public Variable(Qualified qualified) {
        this.qualified = qualified;
        this.first = qualified.getFirst().getName();
        this.second = qualified.getSecond().getName().getName();
    }

    public Qualified getQualified() {
        return qualified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(first, variable.first) &&
                Objects.equals(second, variable.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
