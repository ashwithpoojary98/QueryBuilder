package io.github.ashwithpoojary98.clausses.offsetclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;

public class OffsetClause extends AbstractClause {

    private int offset;

    public OffsetClause(int offset) {
        this.offset = offset;
    }

    public OffsetClause() {
        this.offset = 0;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int value) {
        if (value > 0) {
            this.offset = value;
        }
    }

    public boolean hasOffset() {
        return offset > 0;
    }

    public OffsetClause clear() {
        offset = 0;
        return this;
    }

    @Override
    public AbstractClause copy() {
        OffsetClause offsetClause = new OffsetClause();
        offsetClause.setEngine(this.getEngine());
        offsetClause.setOffset(this.offset);
        offsetClause.setComponent(this.getComponent());
        return offsetClause;
    }
}

