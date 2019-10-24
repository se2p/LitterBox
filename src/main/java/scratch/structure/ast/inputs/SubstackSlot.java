package scratch.structure.ast.inputs;

import scratch.structure.ast.ScriptBodyBlock;

public class SubstackSlot {

    private String name;
    private int shadowIndicator;
    private ScriptBodyBlock substack;

    public SubstackSlot(String name, int shadowIndicator, ScriptBodyBlock scriptBodyBlock) {
        this.name = name;
        this.shadowIndicator = shadowIndicator;
        this.substack = scriptBodyBlock;
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

    public ScriptBodyBlock getSubstack() {
        return substack;
    }

    public void setSubstack(ScriptBodyBlock substack) {
        this.substack = substack;
    }

    @Override
    public String toString() {
        return "\"" + name + "\": [" + shadowIndicator + ", " + substack + "]";
    }
}
