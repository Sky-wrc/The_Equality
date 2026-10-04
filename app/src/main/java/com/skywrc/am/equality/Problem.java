package com.skywrc.am.equality;

public class Problem {

    private final String id;
    private final String name;
    private final String formula;
    private final InputShape inputShape;

    public Problem(String id, String name, String formula, InputShape inputShape) {
        this.id = id;
        this.name = name;
        this.formula = formula;
        this.inputShape = inputShape;
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

    public InputShape getInputShape() {
        return inputShape;
    }

    public int getParameterCount() {
        return inputShape.getCellCount();
    }
}
