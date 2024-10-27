package io.github.ashwithpoojary98.clausses.incrementclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;

public class IncrementClause extends AbstractClause {
    private String column;
    private int value = 1;

    public IncrementClause() {
    }

    public IncrementClause(String column, int value) {
        this.column = column;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public IncrementClause copy() {
        IncrementClause incrementClause = new IncrementClause();
        incrementClause.setEngine(this.getEngine());
        incrementClause.setComponent(this.getComponent());
        incrementClause.setColumn(this.column);
        incrementClause.setValue(this.value);
        return incrementClause;
    }
}

