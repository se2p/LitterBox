/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;

public enum RobotCode {
    NO_ROBOT("noRobot"),
    CODEY("codey"),
    MCORE("mcore"),
    AURIGA("auriga"),
    MEGAPI("megapi");

    private final String name;

    RobotCode(String name) {
        this.name = name;
    }

    public static RobotCode getRobot(String name, SetStmtList setStmtList) {
        boolean visible = false;
        for (SetStmt setStmt : setStmtList.getStmts()) {
            if (setStmt instanceof SetAttributeTo) {
                if (((SetAttributeTo) setStmt).getStringExpr() instanceof StringLiteral) {
                    if (((StringLiteral) ((SetAttributeTo) setStmt).getStringExpr()).getText().equals("visible")) {
                        visible = ((BoolLiteral) ((SetAttributeTo) setStmt).getExpr()).getValue();
                    }
                }
            }
        }
        if (!visible) {
            if (name.contains("codey")) {
                return CODEY;
            } else if (name.contains("mcore")) {
                return MCORE;
            } else if (name.contains("auriga")) {
                return AURIGA;
            } else if (name.contains("megapi")) {
                return MEGAPI;
            }
        }
        return NO_ROBOT;
    }

    public String getName() {
        return name;
    }

    public boolean isRobot() {
        return !(this.equals(NO_ROBOT));
    }
}
