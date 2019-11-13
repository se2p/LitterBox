package scratch.newast.opcodes;

public enum SpriteMotionStmtOpcode {

    motion_movesteps,       // "move"  NumExpr  "steps"
    motion_turnright,       // |  "turn" "right"  NumExpr "degrees"
    motion_turnleft,        // |  "turn" "left"  NumExpr "degrees"
    motion_gotoxy,
    motion_goto,            // |  "go" "to"  Position
    motion_glideto,
    motion_glidesecstoxy,   // |  "glide"  NumExpr  "secs" "to" Position
    motion_pointindirection,// |  "point" "in" "direction" NumExpr
    motion_pointtowards,    // |  "point" "towards"  Position
    motion_changexby,       // |  "change" "x" "by"  NumExpr
    motion_changeyby,       // |  "change" "y" "by"  NumExpr
    motion_setx,            // |  "set" "x" "to"  NumExpr
    motion_sety,            // |  "set" "y" "to"  NumExpr
    motion_ifonedgebounce;  // |  "if" "on" "edge" "bounce"

    public static boolean contains(String opcode) {
        for (SpriteMotionStmtOpcode value : SpriteMotionStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
