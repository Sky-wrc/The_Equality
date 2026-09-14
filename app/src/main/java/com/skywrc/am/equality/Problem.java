package com.skywrc.am.equality;

public class Problem {

    private final String id;
    private final String name;
    private final String formula;
    private final int parameterCount;

    public Problem(String id, String name, String formula, int parameterCount) {
        this.id = id;
        this.name = name;
        this.formula = formula;
        this.parameterCount = parameterCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public int getParameterCount() {
        return parameterCount;
    }
}
