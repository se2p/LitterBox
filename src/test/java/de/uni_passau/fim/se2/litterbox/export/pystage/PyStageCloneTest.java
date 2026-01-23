
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class PyStageCloneTest {

    @Test
    public void testCreateCloneOfLiteral() {
        StringLiteral spriteName = new StringLiteral("Beetle");
        NoBlockMetadata meta = new NoBlockMetadata();
        WithCloneExpr cloneExpr = new WithCloneExpr(spriteName, meta);
        CreateCloneOf createCloneOf = new CreateCloneOf(cloneExpr, meta);

        String result = PyStageVisitor.of(createCloneOf);

        assertThat(result.trim()).isEqualTo("self._core.control_create_clone_of_sprite(Beetle)");
    }
}
