package io.github.ashwithpoojary98.clausses.combines;


import io.github.ashwithpoojary98.Query;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Combine extends AbstractCombine {

    private Query query;

    private String operation;

    private boolean all;

    public Combine() {
    }

    public Combine(Query query, String operation, boolean all) {
        this.query = query;
        this.operation = operation;
        this.all = all;
    }

    @Override
    public AbstractClause copy() {
        Combine combine = new Combine();
        combine.setEngine(this.getEngine());
        combine.setComponent(this.getComponent());
        combine.setOperation(this.operation);
        combine.setQuery(this.query);
        combine.setAll(this.all);
        return combine;
    }
}
