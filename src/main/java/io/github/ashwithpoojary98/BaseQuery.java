package io.github.ashwithpoojary98;


import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.clausses.conditions.BasicCondition;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;
import io.github.ashwithpoojary98.clausses.conditions.BasicStringCondition;
import io.github.ashwithpoojary98.clausses.conditions.BetweenCondition;
import io.github.ashwithpoojary98.clausses.conditions.BooleanCondition;
import io.github.ashwithpoojary98.clausses.conditions.ExistsCondition;
import io.github.ashwithpoojary98.clausses.conditions.InCondition;
import io.github.ashwithpoojary98.clausses.conditions.InQueryCondition;
import io.github.ashwithpoojary98.clausses.conditions.NestedCondition;
import io.github.ashwithpoojary98.clausses.conditions.NullCondition;
import io.github.ashwithpoojary98.clausses.conditions.QueryCondition;
import io.github.ashwithpoojary98.clausses.conditions.RawCondition;
import io.github.ashwithpoojary98.clausses.conditions.SubQueryCondition;
import io.github.ashwithpoojary98.clausses.conditions.TwoColumnsCondition;
import io.github.ashwithpoojary98.clausses.fromclausses.FromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.QueryFromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.RawFromClause;
import lombok.Getter;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseQuery<Q extends BaseQuery<Q>> extends AbstractQuery {

    @Getter
    private List<AbstractClause> clauses = new ArrayList<>();
    private boolean orFlag = false;
    private boolean notFlag = false;
    protected String engineScope;

    public Q setEngineScope(String engine) {
        this.engineScope = engine;
        return (Q) this;
    }

    public Q setClauses(List<AbstractClause> clauses) {
        this.clauses = clauses;
        return (Q) this;
    }


    protected BaseQuery() {
    }

    public Q copy() {
        Q q = newQuery();
        q.setClauses(this.getClauses().stream()
                .map(AbstractClause::copy)
                .collect(Collectors.toList()));
        return q;
    }


    public Q setParent(AbstractQuery parent) {
        if (this == parent) {
            throw new IllegalArgumentException("Cannot set the same AbstractQuery as a parent of itself");
        }
        this.parent = parent;
        return (Q) this;
    }

    public abstract Q newQuery();

    public Q newChild() {
        Q newQuery = newQuery().setParent(this);
        newQuery.engineScope = this.engineScope;
        return newQuery;
    }

    public Q addComponent(String component, AbstractClause clause, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }
        clause.setEngine(engineCode);
        clause.setComponent(component);
        clauses.add(clause);
        return (Q) this;
    }

    public Q addComponent(String component, AbstractClause clause) {
        return addComponent(component, clause, null);
    }


    public Q addOrReplaceComponent(String component, AbstractClause clause, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }

        String finalEngineCode = engineCode;

        AbstractClause current = getComponents(component, engineCode).stream()
                .filter(c -> c.getEngine() != null && c.getEngine().equals(finalEngineCode))
                .findFirst()
                .orElse(null);

        if (current != null) {
            clauses.remove(current);
        }
        return addComponent(component, clause, engineCode);
    }

    public Q addOrReplaceComponent(String component, AbstractClause clause) {
        return addComponent(component, clause, null);
    }

    public <C extends AbstractClause> List<C> getComponents(String component, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }

        String finalEngineCode = engineCode;
        return clauses.stream()
                .filter(x -> x.getComponent().equals(component) &&
                        (finalEngineCode == null || x.getEngine() == null || finalEngineCode.equals(x.getEngine())))
                .map(c -> (C) c)
                .collect(Collectors.toList());
    }

    public <C extends AbstractClause> List<C> getComponents(String component) {
        return getComponents(component, null);
    }


    public <C extends AbstractClause> C getOneComponent(String component, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }

        List<C> all = getComponents(component, engineCode);
        String finalEngineCode = engineCode;
        Optional<C> matchingItem = all.stream()
                .filter(c -> c.getEngine() != null && c.getEngine().equals(finalEngineCode))
                .findFirst();

        // If not found, try to find the first item with a null engine
        return matchingItem.orElseGet(() ->
                all.stream()
                        .filter(c -> c.getEngine() == null)
                        .findFirst()
                        .orElse(null));

    }


    public AbstractClause getOneComponent(String component) {
        return getOneComponent(component, null);
    }

    public boolean hasComponent(String component, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }
        return !getComponents(component, engineCode).isEmpty();
    }

    public boolean hasComponent(String component) {
        return hasComponent(component, null);
    }

    public Q clearComponent(String component, String engineCode) {
        if (engineCode == null) {
            engineCode = engineScope;
        }
        String finalEngineCode = engineCode;
        clauses = clauses.stream()
                .filter(x -> !(x.getComponent().equals(component) &&
                        (finalEngineCode == null || x.getEngine() == null || finalEngineCode.equals(x.getEngine()))))
                .collect(Collectors.toList());
        return (Q) this;
    }

    public Q clearComponent(String component) {
        return clearComponent(component, null);
    }

    protected Q and() {
        orFlag = false;
        return (Q) this;
    }

    public Q or() {
        orFlag = true;
        return (Q) this;
    }

    public Q not(boolean flag) {
        notFlag = flag;
        return (Q) this;
    }

    public Q not() {
        notFlag = true;
        return (Q) this;
    }

    protected boolean getOr() {
        boolean ret = orFlag;
        orFlag = false;
        return ret;
    }

    protected boolean getNot() {
        boolean ret = notFlag;
        notFlag = false;
        return ret;
    }

    public Q from(String table) {
        return addOrReplaceComponent("from", new FromClause(table), null);
    }

    public Q from(Query query) {
        return from(query, null);
    }

    public Q from(Query query, String alias) {
        query = query.copy();
        query.setParent(this);
        if (alias != null) {
            query.as(alias);
        }
        return addOrReplaceComponent("from", new QueryFromClause(query), null);
    }

    public Q fromRaw(String sql, Object... bindings) {
        return addOrReplaceComponent("from", new RawFromClause(sql, bindings), null);
    }


    public Q from(Function<Query, Query> callback, String alias) {
        Query query = new Query();
        query.setParent(this); // Assuming 'this' refers to the current instance of Q
        return from(callback.apply(query), alias);
    }

    public Q from(Function<Query, Query> callback) {
        return from(callback, null);
    }


    public Q where(String column, String op, Object value) {
        // If the value is null, we will assume the developer wants to add a where null clause
        if (value == null) {
            return not(!op.equals("=")).whereNull(column);
        }

        if (value instanceof Boolean) {
            Boolean boolValue = (Boolean) value;
            if (!op.equals("=")) {
                not();
            }
            return Boolean.TRUE.equals(boolValue) ? whereTrue(column) : whereFalse(column);
        }

        return addComponent("where", new BasicCondition(column, op, value, getOr(), getNot()));
    }

    public Q whereNot(String column, String op, Object value) {
        return not().where(column, op, value);
    }

    public Q whereNull(String column) {
        return addComponent("where", new NullCondition(column, getOr(), getNot()));
    }

    public Q orWhere(String column, String op, Object value) {
        return or().where(column, op, value);
    }

    public Q orWhereNot(String column, String op, Object value) {
        return this.or().not().where(column, op, value);
    }

    public Q where(String column, Object value) {
        return where(column, "=", value);
    }

    public Q whereNot(String column, Object value) {
        return whereNot(column, "=", value);
    }

    public Q orWhere(String column, Object value) {
        return orWhere(column, "=", value);
    }

    public Q orWhereNot(String column, Object value) {
        return orWhereNot(column, "=", value);
    }

    public Q where(Object constraints) {
        Map<String, Object> dictionary = new HashMap<>();
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(constraints.getClass()).getPropertyDescriptors()) {
                String propertyName = property.getName();
                Object propertyValue = property.getReadMethod().invoke(constraints);
                dictionary.put(propertyName, propertyValue);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException introspectionException) {
            //ignore this exception
        }
        return where(dictionary);
    }

    public Q where(Map<String, Object> values) {
        Q currentQuery = (Q) this;
        boolean isOr = getOr();
        boolean isNot = getNot();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (isOr) {
                currentQuery = currentQuery.or();
            } else {
                currentQuery.and();
            }

            currentQuery = this.not(isNot).where(entry.getKey(), entry.getValue());
        }

        return currentQuery;
    }

    public Q whereRaw(String sql, Object... bindings) {
        return addComponent("where", new RawCondition(sql, bindings, getOr(), getNot()));
    }

    public Q orWhereRaw(String sql, Object... bindings) {
        return or().whereRaw(sql, bindings);
    }

    public Q where(Function<Q, Q> callback) {
        Q query = callback.apply(newChild());

        // omit empty queries
        if (query.getClauses().stream().noneMatch(x -> "where".equals(x.getComponent()))) {
            return (Q) this;
        }

        return addComponent("where", new NestedCondition<>(query, getNot(), getOr()));
    }


    public Q whereNot(Function<Q, Q> callback) {
        return not().where(callback);
    }

    public Q orWhere(Function<Q, Q> callback) {
        return or().where(callback);
    }

    public Q orWhereNot(Function<Q, Q> callback) {
        return not().or().where(callback);
    }

    public Q whereColumns(String first, String op, String second) {
        return addComponent("where", new TwoColumnsCondition(first, second, op, getOr(), getNot()));
    }

    public Q OrWhereColumns(String first, String op, String second) {
        return or().whereColumns(first, op, second);
    }

    public Q whereNotNull(String column) {
        return not().whereNull(column);
    }

    public Q OrWhereNull(String column) {
        return this.or().whereNull(column);
    }

    public Q OrWhereNotNull(String column) {
        return or().not().whereNull(column);
    }

    public Q whereTrue(String column) {
        return addComponent("where", new BooleanCondition(column, true, getOr(), getNot()));
    }

    public Q OrWhereTrue(String column) {
        return or().whereTrue(column);
    }

    public Q whereFalse(String column) {
        return addComponent("where", new BooleanCondition(column, false, getOr(), getNot()));
    }


    public Q OrWhereFalse(String column) {
        return or().whereFalse(column);
    }

    public Q whereLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return addComponent("where", new BasicStringCondition(
                "like",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot()
        ));
    }


    // Overloaded method for default values
    public Q whereLike(String column, Object value) {
        return whereLike(column, value, false, null);
    }

    public Q whereNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().whereLike(column, value, caseSensitive, escapeCharacter);
    }

    public Q whereNotLike(String column, Object value) {
        return whereNotLike(column, value, false, null);
    }

    public Q orWhereLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().whereLike(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereLike(String column, Object value, boolean caseSensitive) {
        return orWhereLike(column, value, caseSensitive, null);
    }

    public Q orWhereLike(String column, Object value) {
        return orWhereLike(column, value, false, null);
    }


    public Q orWhereNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().whereLike(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereNotLike(String column, Object value) {
        return orWhereNotLike(column, value, false, null);
    }

    public Q whereStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return addComponent("where", new BasicStringCondition(
                "starts",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot()
        ));
    }

    // Overloaded method for default values
    public Q whereStarts(String column, Object value) {
        return whereStarts(column, value, false, null);
    }

    public Q whereNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().whereStarts(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().whereStarts(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().whereStarts(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Q whereNotStarts(String column, Object value) {
        return whereNotStarts(column, value, false, null);
    }

    public Q orWhereStarts(String column, Object value) {
        return orWhereStarts(column, value, false, null);
    }

    public Q orWhereNotStarts(String column, Object value) {
        return orWhereNotStarts(column, value, false, null);
    }

    public Q whereEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return addComponent("where", new BasicStringCondition(
                "ends",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot()
        ));
    }

    // Overloaded method for default values
    public Q whereEnds(String column, Object value) {
        return whereEnds(column, value, false, null);
    }

    public Q whereNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().whereEnds(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().whereEnds(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().whereEnds(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Q whereNotEnds(String column, Object value) {
        return whereNotEnds(column, value, false, null);
    }

    public Q orWhereEnds(String column, Object value) {
        return orWhereEnds(column, value, false, null);
    }

    public Q orWhereNotEnds(String column, Object value) {
        return orWhereNotEnds(column, value, false, null);
    }

    public Q whereContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return addComponent("where", new BasicStringCondition(
                "contains",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot()
        ));
    }

    // Overloaded method for default values
    public Q whereContains(String column, Object value) {
        return whereContains(column, value, false, null);
    }


    public Q whereNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().whereContains(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().whereContains(column, value, caseSensitive, escapeCharacter);
    }

    public Q orWhereNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().whereContains(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Q whereNotContains(String column, Object value) {
        return whereNotContains(column, value, false, null);
    }

    public Q orWhereContains(String column, Object value) {
        return orWhereContains(column, value, false, null);
    }

    public Q orWhereNotContains(String column, Object value) {
        return orWhereNotContains(column, value, false, null);
    }


    public <T> Q whereBetween(String column, T lower, T higher) {
        return addComponent("where", new BetweenCondition<>(
                column,
                getOr(),
                getNot(),
                lower,
                higher
        ));
    }

    public <T> Q orWhereBetween(String column, T lower, T higher) {
        return or().whereBetween(column, lower, higher);
    }

    public <T> Q whereNotBetween(String column, T lower, T higher) {
        return not().whereBetween(column, lower, higher);
    }

    public <T> Q orWhereNotBetween(String column, T lower, T higher) {
        return or().not().whereBetween(column, lower, higher);
    }

    public <T> Q whereIn(String column, List<T> values) {
        List<T> distinctValues = new ArrayList<>();
        for (T value : values) {
            if (!distinctValues.contains(value)) {
                distinctValues.add(value);
            }
        }
        return addComponent("where", new InCondition<T>(
                column,
                getOr(),
                getNot(),
                distinctValues
        ));
    }


    public <T> Q whereIn(String column, T value) {
        return whereIn(column, List.of(value));
    }

    public <T> Q orWhereIn(String column, List<T> values) {
        return or().whereIn(column, values);
    }

    public <T> Q whereNotIn(String column, List<T> values) {
        return not().whereIn(column, values);
    }

    public <T> Q orWhereNotIn(String column, List<T> values) {
        return or().not().whereIn(column, values);
    }

    public Q whereIn(String column, Query query) {
        return addComponent("where", new InQueryCondition(
                column,
                getOr(),
                getNot(),
                query
        ));
    }

    public Q whereIn(String column, Function<Query, Query> callback) {
        Query query = callback.apply(new Query().setParent(this));
        return whereIn(column, query);
    }

    public Q orWhereIn(String column, Query query) {
        return or().whereIn(column, query);
    }

    public Q orWhereIn(String column, Function<Query, Query> callback) {
        return or().whereIn(column, callback);
    }

    public Q whereNotIn(String column, Query query) {
        return not().whereIn(column, query);
    }

    public Q whereNotIn(String column, Function<Query, Query> callback) {
        return not().whereIn(column, callback);
    }

    public Q orWhereNotIn(String column, Query query) {
        return or().not().whereIn(column, query);
    }

    public Q orWhereNotIn(String column, Function<Query, Query> callback) {
        return or().not().whereIn(column, callback);
    }

    public Q where(String column, String op, Function<Q, Q> callback) {
        Q query = callback.apply(newChild());
        return where(column, op, query);
    }

    public Q where(String column, String op, Query query) {
        return addComponent("where", new QueryCondition<Query>(
                column,
                op,
                query,
                getOr(),
                getNot()
        ));
    }

    public Q whereSub(Query query, Object value) {
        return whereSub(query, "=", value);
    }

    public Q whereSub(Query query, String op, Object value) {
        return addComponent("where", new SubQueryCondition<Query>(
                value,
                op,
                query,
                getOr(),
                getNot()
        ));
    }

    public Q orWhereSub(Query query, Object value) {
        return or().whereSub(query, value);
    }

    public Q orWhereSub(Query query, String op, Object value) {
        return or().whereSub(query, op, value);
    }

    public Q orWhere(String column, String op, Query query) {
        return or().where(column, op, query);
    }

    public Q orWhere(String column, String op, Function<Query, Query> callback) {
        return or().where(column, op, callback);
    }

    public Q whereExists(Query query) {
        if (!query.hasComponent("from")) {
            throw new IllegalArgumentException("'" + FromClause.class.getSimpleName() + "' cannot be empty if used inside a whereExists' condition");
        }

        return addComponent("where", new ExistsCondition(
                query,
                getOr(),
                getNot()
        ));
    }

    public Q whereExists(Function<Query, Query> callback) {
        Query childQuery = new Query().setParent(this);
        return whereExists(callback.apply(childQuery));
    }

    public Q whereNotExists(Query query) {
        return not().whereExists(query);
    }

    public Q whereNotExists(Function<Query, Query> callback) {
        return not().whereExists(callback);
    }

    public Q orWhereExists(Query query) {
        return or().whereExists(query);
    }

    public Q orWhereExists(Function<Query, Query> callback) {
        return or().whereExists(callback);
    }

    public Q orWhereNotExists(Query query) {
        return or().not().whereExists(query);
    }

    public Q orWhereNotExists(Function<Query, Query> callback) {
        return or().not().whereExists(callback);
    }


    public Q whereDatePart(String part, String column, String op, Object value) {
        return addComponent("where", new BasicDateCondition(
                op,
                column,
                value,
                part != null ? part.toLowerCase() : null,
                getOr(),
                getNot()
        ));
    }

    // WhereNotDatePart
    public Q whereNotDatePart(String part, String column, String op, Object value) {
        return not().whereDatePart(part, column, op, value);
    }

    // OrWhereDatePart
    public Q orWhereDatePart(String part, String column, String op, Object value) {
        return or().whereDatePart(part, column, op, value);
    }

    // OrWhereNotDatePart
    public Q orWhereNotDatePart(String part, String column, String op, Object value) {
        return or().not().whereDatePart(part, column, op, value);
    }

    // WhereDate
    public Q whereDate(String column, String op, Object value) {
        return whereDatePart("date", column, op, value);
    }

    // WhereNotDate
    public Q whereNotDate(String column, String op, Object value) {
        return not().whereDate(column, op, value);
    }

    // OrWhereDate
    public Q orWhereDate(String column, String op, Object value) {
        return or().whereDate(column, op, value);
    }

    // OrWhereNotDate
    public Q orWhereNotDate(String column, String op, Object value) {
        return or().not().whereDate(column, op, value);
    }

    // WhereTime
    public Q whereTime(String column, String op, Object value) {
        return whereDatePart("time", column, op, value);
    }

    // WhereNotTime
    public Q whereNotTime(String column, String op, Object value) {
        return not().whereTime(column, op, value);
    }

    // OrWhereTime
    public Q orWhereTime(String column, String op, Object value) {
        return or().whereTime(column, op, value);
    }

    // OrWhereNotTime
    public Q orWhereNotTime(String column, String op, Object value) {
        return or().not().whereTime(column, op, value);
    }

    // WhereDatePart with equality
    public Q whereDatePart(String part, String column, Object value) {
        return whereDatePart(part, column, "=", value);
    }

    // WhereNotDatePart with equality
    public Q whereNotDatePart(String part, String column, Object value) {
        return whereNotDatePart(part, column, "=", value);
    }

    // OrWhereDatePart with equality
    public Q orWhereDatePart(String part, String column, Object value) {
        return orWhereDatePart(part, column, "=", value);
    }

    // OrWhereNotDatePart with equality
    public Q orWhereNotDatePart(String part, String column, Object value) {
        return orWhereNotDatePart(part, column, "=", value);
    }

    // WhereDate with equality
    public Q whereDate(String column, Object value) {
        return whereDate(column, "=", value);
    }

    // WhereNotDate with equality
    public Q whereNotDate(String column, Object value) {
        return whereNotDate(column, "=", value);
    }

    // OrWhereDate with equality
    public Q orWhereDate(String column, Object value) {
        return orWhereDate(column, "=", value);
    }

    // OrWhereNotDate with equality
    public Q orWhereNotDate(String column, Object value) {
        return orWhereNotDate(column, "=", value);
    }

    // WhereTime with equality
    public Q whereTime(String column, Object value) {
        return whereTime(column, "=", value);
    }

    // WhereNotTime with equality
    public Q whereNotTime(String column, Object value) {
        return whereNotTime(column, "=", value);
    }

    // OrWhereTime with equality
    public Q orWhereTime(String column, Object value) {
        return orWhereTime(column, "=", value);
    }

    // OrWhereNotTime with equality
    public Q orWhereNotTime(String column, Object value) {
        return orWhereNotTime(column, "=", value);
    }


}
