package io.github.ashwithpoojary98.clausses.columns;


import io.github.ashwithpoojary98.Query;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryColumn extends AbstractColumn {

    private Query query;

    public QueryColumn() {
    }

    public QueryColumn(Query query) {
        this.query = query;
    }

    public QueryColumn(String engine, Query query, String component) {
        setEngine(engine);
        this.query = query;
        setComponent(component);
    }

    @Override
    public AbstractClause copy() {
        return new QueryColumn(this.getEngine(), this.query.copy(), this.getComponent());
    }
}
