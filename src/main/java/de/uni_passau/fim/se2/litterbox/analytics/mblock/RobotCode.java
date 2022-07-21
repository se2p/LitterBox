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
