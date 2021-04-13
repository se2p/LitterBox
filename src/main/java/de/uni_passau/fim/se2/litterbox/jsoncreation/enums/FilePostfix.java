package de.uni_passau.fim.se2.litterbox.jsoncreation.enums;

public enum FilePostfix {

    ANNOTATED("_annotated"),
    REFACTORED("_refactored"),
    ;

    private final String value;

    FilePostfix(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
