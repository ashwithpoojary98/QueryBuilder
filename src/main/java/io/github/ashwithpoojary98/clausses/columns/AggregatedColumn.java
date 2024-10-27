package io.github.ashwithpoojary98.clausses.columns;


import io.github.ashwithpoojary98.Query;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregatedColumn extends AbstractColumn {
    private Query filter;

    private String aggregate;

    private AbstractColumn column;

    public AggregatedColumn() {
    }

    public AggregatedColumn(AbstractColumn column, String aggregate, Query filter) {
        this.column = column;
        this.aggregate = aggregate;
        this.filter = filter;

    }

    @Override
    public AbstractClause copy() {
        AggregatedColumn aggregatedColumn = new AggregatedColumn();
        aggregatedColumn.setEngine(this.getEngine());
        aggregatedColumn.setFilter(this.getFilter() != null ? this.getFilter().copy() : null);
        aggregatedColumn.setColumn((AbstractColumn) this.getColumn().copy());
        aggregatedColumn.setAggregate(this.getAggregate());
        aggregatedColumn.setComponent(this.getComponent());
        return aggregatedColumn;
    }
}
