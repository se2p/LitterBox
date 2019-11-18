package scratch.newast.model;

public interface ASTLeaf extends ASTNode {

    default String[] toSimpleStringArray(){
        return new String[0];
    };
}
