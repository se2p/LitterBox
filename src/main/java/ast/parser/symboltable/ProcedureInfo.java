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
package ast.parser.symboltable;

import java.util.Arrays;
import java.util.Objects;

public class ProcedureInfo {

    private String name;
    private String actorName;
    private ArgumentInfo[] arguments;

    public ProcedureInfo(String name, ArgumentInfo[] arguments, String actorName) {
        this.name = name;
        this.arguments = arguments;
        this.actorName=actorName;
    }

    public String getName() {
        return name;
    }

    public ArgumentInfo[] getArguments() {
        return arguments;
    }

    public String getActorName() {
        return actorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcedureInfo that = (ProcedureInfo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(actorName, that.actorName) &&
                Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, actorName);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
