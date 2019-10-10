package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.stack.SingleIntInputBlock;
import scratch.structure.ast.stack.TurnDegreesBlock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class SingleInputTransformerFactory {

    public static Transformer buildTransformer(String singleInputBlockClass, Set<String> identifiers, int expectedInputType, String inputName) {
        return new Transformer() {

            @Override
            Set<String> getIdentifiers() {
                return identifiers;
            }

            public SingleIntInputBlock transform(JsonNode node, Ast ast) {

                extractStandardValues(node);

                SingleIntInputBlock block;
                ArrayNode inputArray = (ArrayNode) node.get("inputs").get(inputName);
                int inputType = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_TYPE).asInt();
                int inputShadow = inputArray.get(POS_INPUT_SHADOW).asInt();

                try {
                    if (inputType == expectedInputType) {
                        int inputValue = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asInt();
                        if (!topLevel) {
                            Class<?> clazz = Class.forName(singleInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Stackable.class, Extendable.class, Boolean.class, Boolean.class, Integer.class, String.class, Integer.class, Integer.class);
                            block = (SingleIntInputBlock) constructor.newInstance(opcode, null, null, shadow, topLevel, inputType, inputName, inputValue, inputShadow);
                        } else {
                            int x = node.get("x").intValue();
                            int y = node.get("y").intValue();
                            Class<?> clazz = Class.forName(singleInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Stackable.class, Extendable.class, Boolean.class, Boolean.class, Integer.class, Integer.class, Integer.class, String.class, Integer.class, Integer.class);
                            block = (SingleIntInputBlock) constructor.newInstance(opcode, null, null, shadow, topLevel, x, y, inputType, inputName, inputValue, inputShadow);
                        }
                    } else if (inputType == VAR_PRIMITIVE) { // FIXME also store the value of the obscured input
                        String inputVariableID = inputArray.get(POS_DATA_ARRAY).get(POS_VAR_ID).toString().replaceAll("^\"|\"$", "");
                        if (!topLevel) {
                            Class<?> clazz = Class.forName(singleInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Stackable.class, Extendable.class, Boolean.class, Boolean.class, Integer.class, String.class, String.class, Integer.class);
                            block = (SingleIntInputBlock) constructor.newInstance(opcode, null, null, shadow, topLevel, inputType, inputName, inputVariableID, inputShadow);
                        } else {
                            int x = node.get("x").intValue();
                            int y = node.get("y").intValue();
                            Class<?> clazz = Class.forName(singleInputBlockClass);
                            Constructor<?> constructor = clazz.getConstructor(String.class, Stackable.class, Extendable.class, Boolean.class, Boolean.class, Integer.class, Integer.class, Integer.class, String.class, String.class, Integer.class);
                            block = (SingleIntInputBlock) constructor.newInstance(opcode, null, null, shadow, topLevel, x, y, inputType, inputName, inputVariableID, inputShadow);

                        }

                    } else {
                        throw new RuntimeException("Unexpected input type: " + inputType); // TODO what is an appropriate error handling strategy here?
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Excuse me?"); //Todo use an exception that is also acceptable when code is published on github
                }

                return block;
            }
        };
    }
}
