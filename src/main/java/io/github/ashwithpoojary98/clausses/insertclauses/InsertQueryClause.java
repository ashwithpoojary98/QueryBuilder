package io.github.ashwithpoojary98.clausses.insertclauses;

import io.github.ashwithpoojary98.Query;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InsertQueryClause extends AbstractInsertClause {

    private List<String> columns = new ArrayList<>();

    private Query query;

    public InsertQueryClause() {
    }

    public InsertQueryClause(List<String> columns, Query query) {
        this.columns = columns;
        this.query = query;

    }

    @Override
    public AbstractClause copy() {
        InsertQueryClause insertQueryClause = new InsertQueryClause();
        insertQueryClause.setEngine(this.getEngine());
        insertQueryClause.setComponent(this.getComponent());
        insertQueryClause.setColumns(this.columns);
        insertQueryClause.setQuery(this.query.copy());
        return insertQueryClause;
    }
}
