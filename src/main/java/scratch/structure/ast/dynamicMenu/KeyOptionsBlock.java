package scratch.structure.ast.dynamicMenu;

public class KeyOptionsBlock extends DynamicMenuBlock {

    String keyOption;

    public KeyOptionsBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public String getKeyOption() {
        return keyOption;
    }

    public void setKeyOption(String keyOption) {
        this.keyOption = keyOption;
    }
}
