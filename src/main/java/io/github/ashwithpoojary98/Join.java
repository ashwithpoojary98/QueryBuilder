package io.github.ashwithpoojary98;

import io.github.ashwithpoojary98.clausses.conditions.TwoColumnsCondition;

import java.util.function.Function;

public class Join extends BaseQuery<Join> {
    protected String type = "INNER JOIN";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public Join() {
        super();
    }

    @Override
    public Join copy() {
        Join clone = super.copy();
        clone.type = this.type;
        return clone;
    }

    public Join asType(String type) {
        setType(type);
        return this;
    }


    public Join joinWith(String table) {
        return from(table);
    }

    public Join joinWith(Query query) {
        return from(query);
    }

    public Join joinWith(Function<Query, Query> callback) {
        return from(callback);
    }

    public Join asInner() {
        return asType("INNER JOIN");
    }

    public Join asOuter() {
        return asType("OUTER JOIN");
    }

    public Join asLeft() {
        return asType("LEFT JOIN");
    }

    public Join asRight() {
        return asType("RIGHT JOIN");
    }

    public Join asCross() {
        return asType("CROSS JOIN");
    }

    public Join on(String first, String second, String op) {
        if (op == null) op = "=";
        return addComponent("where", new TwoColumnsCondition(first, second, op, getOr(), getNot()));
    }

    public Join orOn(String first, String second, String op) {
        return or().on(first, second, op);
    }

    @Override
    public Join newQuery() {
        return new Join();
    }
}

