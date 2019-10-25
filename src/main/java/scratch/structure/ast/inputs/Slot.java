package scratch.structure.ast.inputs;

import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;

public class Slot {
    private String name;
    private int shadowIndicator;
    private Input primary;
    private Input shadow;

    public Slot(String name, int shadowIndicator, Input primary, Input shadow) {
        this.name = name;
        this.shadowIndicator = shadowIndicator;
        this.primary = primary;
        this.shadow = shadow;
    }

    public Slot(String name, int shadowIndicator, Literal primary) {
        this.name = name;
        this.shadowIndicator = shadowIndicator;
        this.primary = primary;
        this.shadow = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShadowIndicator() {
        return shadowIndicator;
    }

    public void setShadowIndicator(int shadowIndicator) {
        this.shadowIndicator = shadowIndicator;
    }

    public Input getPrimary() {
        return primary;
    }

    public void setPrimary(Input primary) {
        this.primary = primary;
    }

    public Input getShadow() {
        return shadow;
    }

    public void setShadow(Input shadow) {
        this.shadow = shadow;
    }

    @Override
    public String toString() {
        if (primary instanceof Literal || primary instanceof ListBlock || primary instanceof VariableBlock) {
            if (shadow == null) {
                return "\"" + name + "\": [" + shadowIndicator + ", [" + primary + "]]";
            } else {
                return "\"" + name + "\": [" + shadowIndicator + ", [" + primary + "], [" + shadow + "]";
            }
        } else {
            if (shadow == null) {
                return "\"" + name + "\": [" + shadowIndicator + ", " + ((ScratchBlock) primary).getId() + "]";
            } else {
                return "\"" + name + "\": [" + shadowIndicator + ", " + ((ScratchBlock) primary).getId() + ", [" + shadow + "]";
            }
        }
    }
}
