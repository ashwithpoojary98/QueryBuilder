package io.github.ashwithpoojary98.clausses.fromclausses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryFromClause extends AbstractFrom {

    private Query query;

    public QueryFromClause(Query query, String alias) {
        this.alias = alias;
        this.query = query;
    }

    public QueryFromClause(Query query) {
        this.query = query;
    }

    public QueryFromClause() {
    }

    @Override
    public AbstractClause copy() {
        QueryFromClause queryFromClause = new QueryFromClause();
        queryFromClause.setEngine(this.getEngine());
        queryFromClause.setComponent(this.getComponent());
        queryFromClause.setAlias(this.alias);
        queryFromClause.setQuery(query.copy());
        return queryFromClause;
    }
}
