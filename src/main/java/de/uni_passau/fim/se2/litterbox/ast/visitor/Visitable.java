package de.uni_passau.fim.se2.litterbox.ast.visitor;

public interface Visitable<T extends Visitable> {

    void accept(ScratchVisitor visitor);

    T accept(CloneVisitor visitor);
}
