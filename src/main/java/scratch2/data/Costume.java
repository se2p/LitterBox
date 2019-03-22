package scratch2.data;

/**
 * Wrapper for Scratch Costumes
 * Default background: White
 */
public class Costume {

    private String name = "#FFF";
    private int baseLayerID;

    public Costume() {

    }

    @Override
    public String toString() {
        return "Costume{" +
                "name='" + name + '\'' +
                ", baseLayerID=" + baseLayerID +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBaseLayerID() {
        return baseLayerID;
    }

    public void setBaseLayerID(int baseLayerID) {
        this.baseLayerID = baseLayerID;
    }
}
