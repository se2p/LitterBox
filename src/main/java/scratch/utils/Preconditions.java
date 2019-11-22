package scratch.utils;

public class Preconditions {

    public static <T> T checkNotNull(T o) {
        if (o == null) {
            throw new NullPointerException("Variable must not be null");
        }
        return o;
    }

    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T[] checkAllArgsNotNull(T ... args)
    {
        int i = 0;
        for (Object o: args) {

            if (o == null) {
                throw new NullPointerException(String.format("Argument %d must not be null", i));
            }
            i ++;
        }
        return args;
    }
}
