package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoboscriptGraphicEffectsTest {

    @Test
    public void testAllGraphicEffects() {
        NoBlockMetadata meta = new NoBlockMetadata();
        GoboscriptVisitor visitor = new GoboscriptVisitor();

        for (GraphicEffect.GraphicEffectType type : GraphicEffect.GraphicEffectType.values()) {
            GraphicEffect effect = new GraphicEffect(type.getToken());
            
            // Test ChangeGraphicEffectBy
            ChangeGraphicEffectBy change = new ChangeGraphicEffectBy(effect, new NumberLiteral(10), meta);
            change.accept(visitor);
            String output = visitor.getOutput();
            assertThat(output).contains("change_" + type.getToken() + "_effect 10;");
            
            // Test SetGraphicEffectTo
            visitor = new GoboscriptVisitor(); // Reset visitor
            SetGraphicEffectTo set = new SetGraphicEffectTo(effect, new NumberLiteral(50), meta);
            set.accept(visitor);
            output = visitor.getOutput();
            assertThat(output).contains("set_" + type.getToken() + "_effect 50;");
        }
    }
}
