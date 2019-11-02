package scratch.newast.model.expression.num;

public class Number implements NumExpr {

    private float value;

    public Number(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}