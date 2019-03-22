package scratch2.data;

/**
 * Wrapper for Scratch Variables
 */
public class ScVariable {

    private String name;
    private String value;
    private boolean isNumber;

    public ScVariable() {

    }

    @Override
    public String toString() {
        return "ScVariable{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", isNumber=" + isNumber +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public void setNumber(boolean number) {
        isNumber = number;
    }

}
