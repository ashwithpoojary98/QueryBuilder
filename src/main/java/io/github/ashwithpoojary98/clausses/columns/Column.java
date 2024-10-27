package io.github.ashwithpoojary98.clausses.columns;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Column extends AbstractColumn {

    private String name;

    public Column(String name) {
        this.name = name;
    }

    public Column(String engine, String name, String component) {
        this.setEngine(engine);
        this.name = name;
        this.setComponent(component);
    }

    @Override
    public AbstractClause copy() {
        return new Column(this.getEngine(), this.getName(), this.getComponent());
    }
}
