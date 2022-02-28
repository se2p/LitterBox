package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TokenVisitorTest {

    @Test
    public void testIntegerNumber() {
        NumberLiteral literal = new NumberLiteral(1234);
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("1234");
    }

    @Test
    public void testDoubleNumber() {
        NumberLiteral literal = new NumberLiteral(3.14);
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("3.14");
    }

    @Test
    public void testStringWithWhitespace() {
        StringLiteral literal = new StringLiteral("hello\t world \n");
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("helloworld");
    }

    @Test
    public void testStringWithComma() {
        StringLiteral literal = new StringLiteral("a,b,c");
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("abc");
    }
}
