package io.github.ashwithpoojary98.clausses.orderbyclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderByRandom extends AbstractOrderBy {

    @Override
    public AbstractClause copy() {
        OrderByRandom orderByRandom = new OrderByRandom();
        orderByRandom.setEngine(this.getEngine());
        return orderByRandom;
    }
}
