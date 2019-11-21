package scratch.utils;

import scratch.ast.model.ASTNode;

import java.util.*;

public class UnmodifiableListBuilder<E> {

    private List<E> list = new ArrayList<>();

    public UnmodifiableListBuilder<E> add(E e) {
        list.add(e);
        return this;
    }

    public UnmodifiableListBuilder<E> addAll(Collection<? extends E> collection) {
        list.addAll(collection);
        return this;
    }

    public UnmodifiableListBuilder<E> asLinked() {
        list = new LinkedList<>(list);
        return this;
    }

    public List<E> build() {
        return Collections.unmodifiableList(list);
    }

    public static <E> UnmodifiableListBuilder<E> builder() {
        return new UnmodifiableListBuilder<>();
    }

}
