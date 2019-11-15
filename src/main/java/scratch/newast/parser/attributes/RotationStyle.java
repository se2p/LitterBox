package scratch.newast.parser.attributes;

public enum RotationStyle {
    dont_rotate, left_right, all_around;


    @Override
    public String toString() {
        if (this == dont_rotate) {
            return "don't rotate";
        } else if (this == left_right) {
            return "left-right";
        } else {
            return "all around";
        }
    }

    public static boolean contains(String opcode) {
        for (RotationStyle value : RotationStyle.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
