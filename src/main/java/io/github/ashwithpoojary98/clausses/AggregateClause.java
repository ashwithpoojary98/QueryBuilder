package io.github.ashwithpoojary98.clausses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AggregateClause extends AbstractClause {

    private List<String> columns;

    private String type;

    public AggregateClause(){}

    public AggregateClause(String type, List<String> columns) {
        this.type = type;
        this.columns = columns;
    }

    @Override
    public AbstractClause copy() {
        AggregateClause aggregateClause = new AggregateClause();
        aggregateClause.setEngine(this.getEngine());
        aggregateClause.setComponent(this.getComponent());
        aggregateClause.setColumns(this.columns);
        aggregateClause.setType(this.type);
        return aggregateClause;
    }
}
