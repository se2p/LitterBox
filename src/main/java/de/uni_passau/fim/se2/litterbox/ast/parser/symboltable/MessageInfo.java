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
package de.uni_passau.fim.se2.litterbox.ast.parser.symboltable;

import de.uni_passau.fim.se2.litterbox.ast.model.Message;

public class MessageInfo {

    boolean global;
    String scriptGroup;
    String variableName;
    Message message;

    public MessageInfo(boolean global, String scriptGroup, String ident,
        Message message) {
        this.global = global;
        this.scriptGroup = scriptGroup;
        this.variableName = ident;
        this.message = message;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getScriptGroup() {
        return scriptGroup;
    }

    public String getVariableName() {
        return variableName;
    }

    public Message getMessage() {
        return message;
    }
}
