package scratch.newast.opcodes;

public enum EventOpcode {
    event_whenflagclicked,
    event_whenkeypressed,
    event_whenthisspriteclicked,
    event_whenbroadcastreceived,
    event_whenbackdropswitchesto,
    control_start_as_clone,
    event_whengreaterthan;


    public static boolean contains(String opcode) {
        for (EventOpcode value : EventOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
