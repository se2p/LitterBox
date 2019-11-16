package scratch.newast.parser.attributes;

public enum DragMode {
    not_draggable, draggable;

    @Override
    public String toString() {
        if(this==not_draggable){
            return("not draggable");
        }else{
            return ("draggable");
        }
    }

    public static boolean contains(String opcode) {
        for (DragMode value : DragMode.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
