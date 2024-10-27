package io.github.ashwithpoojary98.clausses;

import lombok.Data;

@Data
public abstract class AbstractClause {

    private String engine;

    private String component;

    public abstract AbstractClause copy();


}
