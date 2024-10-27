package io.github.ashwithpoojary98.clausses.limits;

import io.github.ashwithpoojary98.clausses.AbstractClause;

public class LimitClause extends AbstractClause {

    private int limit;

    public LimitClause(int limit) {
        this.limit = limit;
    }

    public LimitClause() {
        this.limit = 0;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int value) {
        if (value > 0) {
            this.limit = value;
        }
    }

    public boolean hasLimit() {
        return limit > 0;
    }

    public LimitClause clear() {
        limit = 0;
        return this;
    }

    @Override
    public AbstractClause copy() {
        LimitClause limitClause = new LimitClause();
        limitClause.setEngine(this.getEngine());
        limitClause.setLimit(this.limit);
        limitClause.setComponent(this.getComponent());
        return limitClause;
    }
}
