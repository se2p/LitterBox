/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import java.util.Objects;

public class Use implements DataflowFact {

    private final CFGNode target;

    private final Defineable defineable;

    public Use(CFGNode target, Defineable defineable) {
        this.target = target;
        this.defineable = defineable;
    }

    public CFGNode getUseTarget() {
        return target;
    }

    public Defineable getDefinable() {
        return defineable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Use use = (Use) o;
        return Objects.equals(target, use.target) && Objects.equals(defineable, use.defineable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, defineable);
    }

    @Override
    public String toString() {
        return "Use{" + defineable + "}";
    }
}
