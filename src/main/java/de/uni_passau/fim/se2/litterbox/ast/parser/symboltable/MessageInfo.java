/*
 * Copyright (C) 2020 LitterBox contributors
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
    String actor;
    String identifier;
    Message message;

    public MessageInfo(boolean global, String actor, String ident,
                       Message message) {
        this.global = global;
        this.actor = actor;
        this.identifier = ident;
        this.message = message;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getActor() {
        return actor;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Message getMessage() {
        return message;
    }
}
