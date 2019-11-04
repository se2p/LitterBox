package scratch.newast.model.procedure;

import java.util.List;

public class ParameterListPlain {

    List<Parameter> parameters;

    public ParameterListPlain(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}
