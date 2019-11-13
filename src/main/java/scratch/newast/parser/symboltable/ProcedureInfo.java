package scratch.newast.parser.symboltable;

public class ProcedureInfo {
    private String name;
    private ArgumentInfo[] arguments;

    public ProcedureInfo(String name, ArgumentInfo[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public ArgumentInfo[] getArguments() {
        return arguments;
    }
}
