package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.stack.SingleIntInputBlock;
import scratch.structure.ast.stack.StackBlock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.Stack;

public class NoInputTransformerFactory {

    public static Transformer buildTransformer(String noInputBlockClass, Set<String> identifiers) {
        return new Transformer() {

            @Override
            Set<String> getIdentifiers() {
                return identifiers;
            }

            public StackBlock transform(JsonNode node, Ast ast) {

                extractStandardValues(node);

                StackBlock block;
                try {
                        if (!topLevel) {
                            Class<?> clazz = Class.forName(noInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Extendable.class, Stackable.class, Boolean.class, Boolean.class);
                            block = (StackBlock) constructor.newInstance(opcode, null, null, shadow, topLevel);
                        } else {
                            int x = node.get("x").intValue();
                            int y = node.get("y").intValue();
                            Class<?> clazz = Class.forName(noInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Extendable.class, Stackable.class, Boolean.class, Boolean.class, Integer.class, Integer.class);
                            block = (StackBlock) constructor.newInstance(opcode, null, null, shadow, topLevel, x, y);
                        }
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Excuse me?"); //Todo use an exception that is also acceptable when code is published on github
                }

                return block;
            }
        };
    }
}
