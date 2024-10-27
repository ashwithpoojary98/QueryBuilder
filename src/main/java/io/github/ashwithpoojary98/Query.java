package io.github.ashwithpoojary98;

import io.github.ashwithpoojary98.annotations.ColumnAttribute;
import io.github.ashwithpoojary98.annotations.IgnoreAttribute;
import io.github.ashwithpoojary98.annotations.KeyAttribute;
import io.github.ashwithpoojary98.clausses.AggregateClause;
import io.github.ashwithpoojary98.clausses.columns.AggregatedColumn;
import io.github.ashwithpoojary98.clausses.columns.Column;
import io.github.ashwithpoojary98.clausses.columns.QueryColumn;
import io.github.ashwithpoojary98.clausses.columns.RawColumn;
import io.github.ashwithpoojary98.clausses.combines.Combine;
import io.github.ashwithpoojary98.clausses.combines.RawCombine;
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
import io.github.ashwithpoojary98.clausses.conditions.TwoColumnsCondition;
import io.github.ashwithpoojary98.clausses.fromclausses.AdHocTableFromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.FromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.QueryFromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.RawFromClause;
import io.github.ashwithpoojary98.clausses.incrementclauses.IncrementClause;
import io.github.ashwithpoojary98.clausses.insertclauses.InsertClause;
import io.github.ashwithpoojary98.clausses.insertclauses.InsertQueryClause;
import io.github.ashwithpoojary98.clausses.joins.BaseJoin;
import io.github.ashwithpoojary98.clausses.limits.LimitClause;
import io.github.ashwithpoojary98.clausses.offsetclauses.OffsetClause;
import io.github.ashwithpoojary98.clausses.orderbyclauses.OrderBy;
import io.github.ashwithpoojary98.clausses.orderbyclauses.OrderByRandom;
import io.github.ashwithpoojary98.clausses.orderbyclauses.RawOrderBy;
import io.github.ashwithpoojary98.compilers.EngineCodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Query extends BaseQuery<Query> {
    private String comment;
    private boolean isDistinct = false;
    private String queryAlias;
    private String method = "select";
    private List<Include> includes = new ArrayList<>();
    private Map<String, Object> variables = new HashMap<>();
    private static final Map<Class<?>, Field[]> cacheDictionaryProperties = new ConcurrentHashMap<>();


    public Query() {
        super();
    }

    public Query(String table) {
        super();
        from(table);
    }

    public Query(String table, String comment) {
        super();
        from(table);
        comment(comment);
    }

    public void setIsDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
    }

    public boolean getIsDistinct() {
        return isDistinct;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public String getComment() {
        return comment != null ? comment : "";
    }

    public void setQueryAlias(String queryAlias) {
        this.queryAlias = queryAlias;
    }

    public String getQueryAlias() {
        return this.queryAlias;
    }

    public boolean hasOffset(String engineCode) {
        return getOffset(engineCode) > 0;
    }

    public boolean hasLimit(String engineCode) {
        return getLimit(engineCode) > 0;
    }

    public int getOffset(String engineCode) {
        engineCode = engineCode != null ? engineCode : engineScope;
        OffsetClause offset = getOneComponent("offset", engineCode);
        return offset != null ? offset.getOffset() : 0;
    }

    public int getLimit(String engineCode) {
        engineCode = engineCode != null ? engineCode : engineScope;
        LimitClause limit = getOneComponent("limit", engineCode);
        return limit != null ? limit.getLimit() : 0;
    }

    @Override
    public Query copy() {
        Query clone = super.copy();
        clone.parent = this.parent;
        clone.queryAlias = this.queryAlias;
        clone.isDistinct = this.isDistinct;
        clone.method = this.method;
        clone.includes = new ArrayList<>(this.includes);
        clone.variables = new HashMap<>(this.variables);
        return clone;
    }

    public Query as(String alias) {
        this.queryAlias = alias;
        return this;
    }

    public Query comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Query forEngine(String engine, Function<Query, Query> fn) {
        String previousEngineScope = engineScope;
        engineScope = engine;
        Query result = fn.apply(this);
        engineScope = previousEngineScope;
        return result;
    }

    public Query forEngine(EngineCodes engine, Function<Query, Query> fn) {
        return forEngine(engine.getCode(), fn);
    }

    public Query with(Query query) {
        if (query.queryAlias == null || query.queryAlias.trim().isEmpty()) {
            throw new IllegalStateException("No Alias found for the CTE query");
        }
        Query clonedQuery = query.copy();
        String alias = clonedQuery.queryAlias.trim();
        clonedQuery.queryAlias = null;
        return addComponent("cte", new QueryFromClause(clonedQuery, alias));
    }

    public Query with(Function<Query, Query> fn) {
        return with(fn.apply(new Query()));
    }

    public Query with(String alias, Query query) {
        return with(query.as(alias));
    }

    public Query with(String alias, Function<Query, Query> fn) {
        return with(alias, fn.apply(new Query()));
    }

    public Query with(String alias, Iterable<String> columns, Iterable<Iterable<Object>> valuesCollection) {
        List<String> columnsList = columns != null ? StreamSupport.stream(columns.spliterator(), false).collect(Collectors.toList()) : null;
        List<Iterable<Object>> valuesCollectionList = valuesCollection != null ? StreamSupport.stream(valuesCollection.spliterator(), false).collect(Collectors.toList()) : null;

        if ((columnsList == null || columnsList.isEmpty()) || (valuesCollectionList == null || valuesCollectionList.isEmpty())) {
            throw new IllegalArgumentException("Columns and valuesCollection cannot be null or empty");
        }

        AdHocTableFromClause clause = new AdHocTableFromClause(alias, columnsList, new ArrayList<>());
        for (Iterable<Object> values : valuesCollectionList) {
            List<Object> valuesList = StreamSupport.stream(values.spliterator(), false).collect(Collectors.toList());
            if (columnsList.size() != valuesList.size()) {
                throw new IllegalArgumentException("Columns count should be equal to each Values count");
            }
            clause.getValues().addAll(valuesList);
        }

        return addComponent("cte", clause);
    }


    public Query withRaw(String alias, String sql, Object... bindings) {
        return addComponent("cte", new RawFromClause(alias, sql, bindings));
    }

    public Query limit(int value) {
        LimitClause newClause = new LimitClause(value);
        return addOrReplaceComponent("limit", newClause);
    }


    public Query offset(int value) {
        OffsetClause newClause = new OffsetClause(value);
        return addOrReplaceComponent("offset", newClause);
    }

    public Query take(int limit) {
        return limit(limit);
    }

    public Query skip(int offset) {
        return offset(offset);
    }

    public Query forPage(int page, int perPage) {
        return skip((page - 1) * perPage).take(perPage);
    }

    public Query distinct() {
        isDistinct = true;
        return this;
    }

    public Query when(boolean condition, Function<Query, Query> whenTrue, Function<Query, Query> whenFalse) {
        if (condition && whenTrue != null) {
            return whenTrue.apply(this);
        }
        if (!condition && whenFalse != null) {
            return whenFalse.apply(this);
        }
        return this;
    }

    public Query whenNot(boolean condition, Function<Query, Query> callback) {
        if (!condition) {
            return callback.apply(this);
        }
        return this;
    }

    public Query orderBy(String... columns) {
        for (String column : columns) {
            addComponent("order", new OrderBy(column, true));
        }
        return this;
    }

    public Query orderByDesc(String... columns) {
        for (String column : columns) {
            addComponent("order", new OrderBy(column, false));
        }
        return this;
    }

    public Query orderByRaw(String expression, Object... bindings) {
        return addComponent("order", new RawOrderBy(expression, bindings));
    }

    public Query orderByRandom(String seed) {
        return addComponent("order", new OrderByRandom());
    }

    public Query groupBy(String... columns) {
        for (String column : columns) {
            addComponent("group", new Column(column));
        }
        return this;
    }

    public Query groupByRaw(String expression, Object... bindings) {
        addComponent("group", new RawColumn(expression, bindings));
        return this;
    }

    @Override
    public Query newQuery() {
        return new Query();
    }

    public Query include(String relationName, Query query, String foreignKey, String localKey, boolean isMany) {
        includes.add(new Include(relationName, query, foreignKey, localKey, isMany));
        return this;
    }

    public Query includeMany(String relationName, Query query, String foreignKey, String localKey) {
        return include(relationName, query, foreignKey, localKey, true);
    }

    public Query define(String variable, Object value) {
        variables.put(variable, value);
        return this;
    }

    public Object findVariable(String variable) {
        if (variables.containsKey(variable)) {
            return variables.get(variable);
        }
        if (parent != null) {
            return ((Query) parent).findVariable(variable);
        }
        throw new IllegalArgumentException("Variable '" + variable + "' not found");
    }

    public Query having(String column, String op, Object value) {
        if (value == null) {
            return not(!op.equals("=")).havingNull(column);
        }

        BasicCondition condition = new BasicCondition(column, op, value, getOr(), getNot());

        return addComponent("having", condition);
    }

    public Query havingNot(String column, String op, Object value) {
        return not().having(column, op, value);
    }

    public Query orHaving(String column, String op, Object value) {
        return or().having(column, op, value);
    }

    public Query orHavingNot(String column, String op, Object value) {
        return or().not().having(column, op, value);
    }

    public Query having(String column, Object value) {
        return having(column, "=", value);
    }

    public Query havingNot(String column, Object value) {
        return havingNot(column, "=", value);
    }

    public Query orHaving(String column, Object value) {
        return orHaving(column, "=", value);
    }

    public Query orHavingNot(String column, Object value) {
        return orHavingNot(column, "=", value);
    }

    public Query having(Object constraints) {
        Map<String, Object> dictionary = new HashMap<>();

        // Use reflection to get properties from the constraints object
        for (Field field : constraints.getClass().getDeclaredFields()) {
            field.setAccessible(true); // Make private fields accessible
            try {
                dictionary.put(field.getName(), field.get(constraints));
            } catch (IllegalAccessException e) {
                // Handle the exception appropriately (log, rethrow, etc.)
            }
        }

        return having(dictionary);
    }

    public Query having(Map<String, Object> values) {
        Query query = this;
        boolean orFlag = getOr();
        boolean notFlag = getNot();

        for (Map.Entry<String, Object> tuple : values.entrySet()) {
            if (orFlag) {
                query = query.or();
            } else {
                query.and();
            }

            query = this.not(notFlag).having(tuple.getKey(), tuple.getValue());
        }

        return query;
    }

    public Query havingRaw(String sql, Object... bindings) {
        RawCondition condition = new RawCondition(sql, bindings, getOr(), getNot());
        return addComponent("having", condition);
    }

    public Query orHavingRaw(String sql, Object... bindings) {
        return or().havingRaw(sql, bindings);
    }

    public Query having(Function<Query, Query> callback) {
        Query query = callback.apply(newChild());
        NestedCondition<Query> condition = new NestedCondition<>(query, getOr(), getNot());
        return addComponent("having", condition);
    }

    public Query havingNot(Function<Query, Query> callback) {
        return not().having(callback);
    }

    public Query orHaving(Function<Query, Query> callback) {
        return or().having(callback);
    }

    public Query orHavingNot(Function<Query, Query> callback) {
        return not().or().having(callback);
    }

    public Query havingColumns(String first, String op, String second) {
        TwoColumnsCondition condition = new TwoColumnsCondition(
                first,
                second,
                op,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    public Query orHavingColumns(String first, String op, String second) {
        return or().havingColumns(first, op, second);
    }

    public Query havingNull(String column) {
        NullCondition condition = new NullCondition(column, getOr(), getNot());
        return addComponent("having", condition);
    }

    public Query havingNotNull(String column) {
        return not().havingNull(column);
    }

    public Query orHavingNull(String column) {
        return or().havingNull(column);
    }

    public Query orHavingNotNull(String column) {
        return or().not().havingNull(column);
    }

    public Query havingTrue(String column) {
        return addComponent("having", new BooleanCondition(column, true));
    }

    public Query orHavingTrue(String column) {
        return or().havingTrue(column);
    }

    public Query havingFalse(String column) {
        return addComponent("having", new BooleanCondition(column, false));
    }

    public Query orHavingFalse(String column) {
        return or().havingFalse(column);
    }

    public Query havingLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        BasicStringCondition condition = new BasicStringCondition(
                "like",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    // Overloaded method for default values
    public Query havingLike(String column, Object value) {
        return havingLike(column, value, false, null);
    }

    public Query havingNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().havingLike(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().havingLike(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().havingLike(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Query havingNotLike(String column, Object value) {
        return havingNotLike(column, value, false, null);
    }

    public Query orHavingLike(String column, Object value) {
        return orHavingLike(column, value, false, null);
    }

    public Query orHavingNotLike(String column, Object value) {
        return orHavingNotLike(column, value, false, null);
    }

    public Query havingStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        BasicStringCondition condition = new BasicStringCondition(
                "starts",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    // Overloaded method for default values
    public Query havingStarts(String column, Object value) {
        return havingStarts(column, value, false, null);
    }

    public Query havingNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().havingStarts(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().havingStarts(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().havingStarts(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Query havingNotStarts(String column, Object value) {
        return havingNotStarts(column, value, false, null);
    }

    public Query orHavingStarts(String column, Object value) {
        return orHavingStarts(column, value, false, null);
    }

    public Query orHavingNotStarts(String column, Object value) {
        return orHavingNotStarts(column, value, false, null);
    }

    public Query havingEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        BasicStringCondition condition = new BasicStringCondition(
                "ends",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    // Overloaded method for default values
    public Query havingEnds(String column, Object value) {
        return havingEnds(column, value, false, null);
    }

    public Query havingNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().havingEnds(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().havingEnds(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().havingEnds(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Query havingNotEnds(String column, Object value) {
        return havingNotEnds(column, value, false, null);
    }

    public Query orHavingEnds(String column, Object value) {
        return orHavingEnds(column, value, false, null);
    }

    public Query orHavingNotEnds(String column, Object value) {
        return orHavingNotEnds(column, value, false, null);
    }

    public Query havingContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        BasicStringCondition condition = new BasicStringCondition(
                "contains",
                column,
                value,
                caseSensitive,
                escapeCharacter,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    // Overloaded method for default values
    public Query havingContains(String column, Object value) {
        return havingContains(column, value, false, null);
    }

    public Query havingNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return not().havingContains(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().havingContains(column, value, caseSensitive, escapeCharacter);
    }

    public Query orHavingNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter) {
        return or().not().havingContains(column, value, caseSensitive, escapeCharacter);
    }

    // Overloaded methods for default values
    public Query havingNotContains(String column, Object value) {
        return havingNotContains(column, value, false, null);
    }

    public Query orHavingContains(String column, Object value) {
        return orHavingContains(column, value, false, null);
    }

    public Query orHavingNotContains(String column, Object value) {
        return orHavingNotContains(column, value, false, null);
    }

    public <T> Query havingBetween(String column, T lower, T higher) {
        BetweenCondition<T> condition = new BetweenCondition<>(
                column,
                getOr(),
                getNot(),
                lower,
                higher);
        return addComponent("having", condition);
    }

    public <T> Query orHavingBetween(String column, T lower, T higher) {
        return or().havingBetween(column, lower, higher);
    }

    public <T> Query havingNotBetween(String column, T lower, T higher) {
        return not().havingBetween(column, lower, higher);
    }

    public <T> Query orHavingNotBetween(String column, T lower, T higher) {
        return or().not().havingBetween(column, lower, higher);
    }

    public <T> Query havingIn(String column, List<T> values) {
        InCondition<T> condition = new InCondition<>(
                column,
                getOr(),
                getNot(),
                values);
        return addComponent("having", condition);
    }

    public <T> Query orHavingIn(String column, List<T> values) {
        return or().havingIn(column, values);
    }

    public <T> Query havingNotIn(String column, List<T> values) {
        return not().havingIn(column, values);
    }

    public <T> Query orHavingNotIn(String column, List<T> values) {
        return or().not().havingIn(column, values);
    }

    public Query havingIn(String column, Query query) {
        InQueryCondition condition = new InQueryCondition(
                column,
                getOr(),
                getNot(),
                query);
        return addComponent("having", condition);
    }

    public Query havingIn(String column, Function<Query, Query> callback) {
        Query query = callback.apply(new Query());
        return havingIn(column, query);
    }

    public Query orHavingIn(String column, Query query) {
        return or().havingIn(column, query);
    }

    public Query orHavingIn(String column, Function<Query, Query> callback) {
        return or().havingIn(column, callback);
    }

    public Query havingNotIn(String column, Query query) {
        return not().havingIn(column, query);
    }

    public Query havingNotIn(String column, Function<Query, Query> callback) {
        return not().havingIn(column, callback);
    }

    public Query orHavingNotIn(String column, Query query) {
        return or().not().havingIn(column, query);
    }

    public Query orHavingNotIn(String column, Function<Query, Query> callback) {
        return or().not().havingIn(column, callback);
    }

    public Query having(String column, String op, Function<Query, Query> callback) {
        Query query = callback.apply(newChild());
        return having(column, op, query);
    }

    public Query having(String column, String op, Query query) {
        QueryCondition<Query> condition = new QueryCondition<>(
                column,
                op,
                query,
                getOr(),
                getNot());

        return addComponent("having", condition);
    }

    public Query orHaving(String column, String op, Query query) {
        return or().having(column, op, query);
    }

    public Query orHaving(String column, String op, Function<Query, Query> callback) {
        return or().having(column, op, callback);
    }

    public Query havingExists(Query query) {
        if (!query.hasComponent("from")) {
            throw new IllegalArgumentException(String.format("%s cannot be empty if used inside a %s condition",
                    FromClause.class.getSimpleName(),
                    "HavingExists"));
        }

        // Simplify the query as much as possible
        query = query.copy().clearComponent("select")
                .selectRaw("1")
                .limit(1);

        ExistsCondition condition = new ExistsCondition(
                query,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    public Query havingExists(Function<Query, Query> callback) {
        Query childQuery = new Query().setParent(this);
        return havingExists(callback.apply(childQuery));
    }

    public Query havingNotExists(Query query) {
        return not().havingExists(query);
    }

    public Query havingNotExists(Function<Query, Query> callback) {
        return not().havingExists(callback);
    }

    public Query orHavingExists(Query query) {
        return or().havingExists(query);
    }

    public Query orHavingExists(Function<Query, Query> callback) {
        return or().havingExists(callback);
    }

    public Query orHavingNotExists(Query query) {
        return or().not().havingExists(query);
    }

    public Query orHavingNotExists(Function<Query, Query> callback) {
        return or().not().havingExists(callback);
    }

    public Query havingDatePart(String part, String column, String op, Object value) {
        BasicDateCondition condition = new BasicDateCondition(
                op,
                column,
                value,
                part,
                getOr(),
                getNot());
        return addComponent("having", condition);
    }

    public Query havingNotDatePart(String part, String column, String op, Object value) {
        return not().havingDatePart(part, column, op, value);
    }

    public Query orHavingDatePart(String part, String column, String op, Object value) {
        return or().havingDatePart(part, column, op, value);
    }

    public Query orHavingNotDatePart(String part, String column, String op, Object value) {
        return or().not().havingDatePart(part, column, op, value);
    }

    public Query havingDate(String column, String op, Object value) {
        return havingDatePart("date", column, op, value);
    }

    public Query havingNotDate(String column, String op, Object value) {
        return not().havingDate(column, op, value);
    }

    public Query orHavingDate(String column, String op, Object value) {
        return or().havingDate(column, op, value);
    }

    public Query orHavingNotDate(String column, String op, Object value) {
        return or().not().havingDate(column, op, value);
    }

    public Query havingTime(String column, String op, Object value) {
        return havingDatePart("time", column, op, value);
    }

    public Query havingNotTime(String column, String op, Object value) {
        return not().havingTime(column, op, value);
    }

    public Query orHavingTime(String column, String op, Object value) {
        return or().havingTime(column, op, value);
    }

    public Query orHavingNotTime(String column, String op, Object value) {
        return or().not().havingTime(column, op, value);
    }

    public Query havingDatePart(String part, String column, Object value) {
        return havingDatePart(part, column, "=", value);
    }

    public Query havingNotDatePart(String part, String column, Object value) {
        return havingNotDatePart(part, column, "=", value);
    }

    public Query orHavingDatePart(String part, String column, Object value) {
        return orHavingDatePart(part, column, "=", value);
    }

    public Query orHavingNotDatePart(String part, String column, Object value) {
        return orHavingNotDatePart(part, column, "=", value);
    }

    public Query havingDate(String column, Object value) {
        return havingDate(column, "=", value);
    }

    public Query havingNotDate(String column, Object value) {
        return havingNotDate(column, "=", value);
    }

    public Query orHavingDate(String column, Object value) {
        return orHavingDate(column, "=", value);
    }

    public Query orHavingNotDate(String column, Object value) {
        return orHavingNotDate(column, "=", value);
    }

    public Query havingTime(String column, Object value) {
        return havingTime(column, "=", value);
    }

    public Query havingNotTime(String column, Object value) {
        return havingNotTime(column, "=", value);
    }

    public Query orHavingTime(String column, Object value) {
        return orHavingTime(column, "=", value);
    }

    public Query orHavingNotTime(String column, Object value) {
        return orHavingNotTime(column, "=", value);
    }

    public Query asAggregate(String type, String[] columns) {
        this.method = "aggregate";

        this.clearComponent("aggregate")
                .addComponent("aggregate", new AggregateClause(type, columns != null ? Arrays.asList(columns) : new ArrayList<>()));

        return this;
    }

    public Query asCount(String[] columns) {
        List<String> cols = columns != null ? Arrays.asList(columns) : new ArrayList<>();

        if (cols.isEmpty()) {
            cols.add("*");
        }

        return asAggregate("count", cols.toArray(new String[0]));
    }

    public Query asCount() {
        return asCount(null);
    }

    public Query asAvg(String column) {
        return asAggregate("avg", new String[]{column});
    }

    public Query asAverage(String column) {
        return asAvg(column);
    }

    public Query asSum(String column) {
        return asAggregate("sum", new String[]{column});
    }

    public Query asMax(String column) {
        return asAggregate("max", new String[]{column});
    }

    public Query asMin(String column) {
        return asAggregate("min", new String[]{column});
    }

    public Query combine(String operation, boolean all, Query query) {
        if (!this.method.equals("select") || !query.method.equals("select")) {
            throw new IllegalStateException("Only select queries can be combined.");
        }

        return addComponent("combine", new Combine(query, operation, all));
    }

    public Query combineRaw(String sql, Object... bindings) {
        if (!this.method.equals("select")) {
            throw new IllegalStateException("Only select queries can be combined.");
        }

        return addComponent("combine", new RawCombine(sql, bindings));
    }

    public Query union(Query query, boolean all) {
        return combine("union", all, query);
    }

    public Query unionAll(Query query) {
        return union(query, true);
    }

    public Query union(Function<Query, Query> callback, boolean all) {
        Query query = callback.apply(new Query());
        return union(query, all);
    }

    public Query unionAll(Function<Query, Query> callback) {
        return union(callback, true);
    }

    public Query unionRaw(String sql, Object... bindings) {
        return combineRaw(sql, bindings);
    }

    public Query except(Query query, boolean all) {
        return combine("except", all, query);
    }

    public Query exceptAll(Query query) {
        return except(query, true);
    }

    public Query except(Function<Query, Query> callback, boolean all) {
        Query query = callback.apply(new Query());
        return except(query, all);
    }

    public Query exceptAll(Function<Query, Query> callback) {
        return except(callback, true);
    }

    public Query exceptRaw(String sql, Object... bindings) {
        return combineRaw(sql, bindings);
    }

    public Query intersect(Query query, boolean all) {
        return combine("intersect", all, query);
    }

    public Query intersectAll(Query query) {
        return intersect(query, true);
    }

    public Query intersect(Function<Query, Query> callback, boolean all) {
        Query query = callback.apply(new Query());
        return intersect(query, all);
    }

    public Query intersectAll(Function<Query, Query> callback) {
        return intersect(callback, true);
    }

    public Query intersectRaw(String sql, Object... bindings) {
        return combineRaw(sql, bindings);
    }

    public Query asDelete() {
        this.method = "delete";
        return this;
    }

    public Query asInsert(Object data, boolean returnId) {
        Map<String, Object> propertiesKeyValues = buildKeyValuePairsFromObject(data);
        return asInsert(propertiesKeyValues, returnId);
    }


    public Query asInsert(Map<String, Object> values, boolean returnId) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values argument cannot be null or empty");
        }

        method = "insert";

        clearComponent("insert").addComponent("insert", new InsertClause(
                new ArrayList<>(values.keySet()),
                new ArrayList<>(values.values()),
                returnId
        ));

        return this;
    }

    public Query asInsert(List<String> columns, List<List<Object>> rowsValues) {
        if (columns == null || columns.isEmpty() || rowsValues == null || rowsValues.isEmpty()) {
            throw new IllegalArgumentException("columns and rowsValues cannot be null or empty");
        }

        method = "insert";

        clearComponent("insert");

        for (List<Object> values : rowsValues) {
            if (columns.size() != values.size()) {
                throw new IllegalArgumentException("columns count should be equal to each rowsValues entry count");
            }

            addComponent("insert", new InsertClause(columns, values));
        }

        return this;
    }

    public Query asInsert(List<String> columns, Query query) {
        method = "insert";

        clearComponent("insert").addComponent("insert", new InsertQueryClause(columns, query.copy()));

        return this;
    }

    private Query join(Function<Join, Join> callback) {
        Join join = callback.apply(new Join().asInner());
        return addComponent("join", new BaseJoin(join));
    }

    public Query join(String table, String first, String second, String op, String type) {
        return join(j -> j.joinWith(table).whereColumns(first, op, second).asType(type));
    }

    public Query join(String table, Function<Join, Join> callback, String type) {
        return join(j -> j.joinWith(table).where(callback).asType(type));
    }

    public Query join(Query query, Function<Join, Join> onCallback, String type) {
        return join(j -> j.joinWith(query).where(onCallback).asType(type));
    }

    public Query leftJoin(String table, String first, String second, String op) {
        return join(table, first, second, op, "left join");
    }

    public Query leftJoin(String table, Function<Join, Join> callback) {
        return join(table, callback, "left join");
    }

    public Query leftJoin(Query query, Function<Join, Join> onCallback) {
        return join(query, onCallback, "left join");
    }

    public Query rightJoin(String table, String first, String second, String op) {
        return join(table, first, second, op, "right join");
    }

    public Query rightJoin(String table, Function<Join, Join> callback) {
        return join(table, callback, "right join");
    }

    public Query rightJoin(Query query, Function<Join, Join> onCallback) {
        return join(query, onCallback, "right join");
    }

    public Query crossJoin(String table) {
        return join(j -> j.joinWith(table).asCross());
    }


    public Query select(String... columns) {
        return select(Arrays.asList(columns));
    }

    public Query select(Iterable<String> columns) {
        method = "select";

        List<String> expandedColumns = new ArrayList<>();
        for (String column : columns) {
            expandedColumns.addAll(Helper.expandExpression(column));
        }

        for (String column : expandedColumns) {
            addComponent("select", new Column(column));
        }

        return this;
    }

    public Query selectRaw(String sql, Object... bindings) {
        method = "select";

        addComponent("select", new RawColumn(sql, bindings));

        return this;
    }

    public Query select(Query query, String alias) {
        method = "select";

        Query clonedQuery = query.copy();

        addComponent("select", new QueryColumn(clonedQuery.as(alias)));

        return this;
    }

    public Query select(Function<Query, Query> callback, String alias) {
        return select(callback.apply(new Query().newChild()), alias);
    }

    public Query selectAggregate(String aggregate, String column, Query filter) {
        method = "select";

        addComponent("select", new AggregatedColumn(new Column(column), aggregate, filter));

        return this;
    }


    public Query selectAggregate(String aggregate, String column, Function<Query, Query> filter) {
        if (filter == null) {
            return selectAggregate(aggregate, column, (Query) null);
        }

        return selectAggregate(aggregate, column, filter.apply(new Query().newChild()));
    }

    public Query selectSum(String column, Function<Query, Query> filter) {
        return selectAggregate("sum", column, filter);
    }

    public Query selectCount(String column, Function<Query, Query> filter) {
        return selectAggregate("count", column, filter);
    }

    public Query selectAvg(String column, Function<Query, Query> filter) {
        return selectAggregate("avg", column, filter);
    }

    public Query selectMin(String column, Function<Query, Query> filter) {
        return selectAggregate("min", column, filter);
    }

    public Query selectMax(String column, Function<Query, Query> filter) {
        return selectAggregate("max", column, filter);
    }

    public Query asUpdate(Object data) {
        Map<String, Object> dictionary = buildKeyValuePairsFromObject(data, true);
        return asUpdate(dictionary);
    }

    public Query asUpdate(List<String> columns, List<Object> values) {
        if (columns == null || columns.isEmpty() || values == null || values.isEmpty()) {
            throw new IllegalStateException(columns + " and " + values + " cannot be null or empty");
        }
        if (columns.size() != values.size()) {
            throw new IllegalStateException(columns + " count should be equal to " + values + " count");
        }
        this.method = "update";
        clearComponent("update").addComponent("update", new InsertClause(columns, values));
        return this;
    }

    public Query asUpdate(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException(values + " cannot be null or empty");
        }
        this.method = "update";
        List<String> columnName = new ArrayList<>();
        List<Object> columnValue = new ArrayList<>();
        values.forEach((key, value) -> {
            columnName.add(key);
            columnValue.add(value);
        });
        clearComponent("update").addComponent("update", new InsertClause(
                columnName,
                columnValue));
        return this;
    }

    public Query asIncrement(String column, int value) {
        this.method = "update";
        addOrReplaceComponent("update", new IncrementClause(column, value));
        return this;
    }

    public Query asIncrement(String column) {
        return asIncrement(column, 1);
    }

    public Query asDecrement(String column, int value) {
        return asIncrement(column, -value);
    }

    public Query asDecrement(String column) {
        return asDecrement(column, 1);
    }


    // Main method to build key-value pairs
    private Map<String, Object> buildKeyValuePairsFromObject(Object data, boolean considerKeys) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        Map<String, Object> dictionary = new HashMap<>();
        Field[] fields = cacheDictionaryProperties.computeIfAbsent(data.getClass(), clazz -> clazz.getDeclaredFields());

        for (Field field : fields) {
            // Check if the field should be ignored
            if (field.isAnnotationPresent(IgnoreAttribute.class)) {
                continue;
            }

            // Access the field value
            field.setAccessible(true); // Allow access to private fields
            Object value;
            try {
                value = field.get(data);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field: " + field.getName(), e);
            }

            // Get the column annotation
            ColumnAttribute columnAttr = field.getAnnotation(ColumnAttribute.class);
            String name = (columnAttr != null && !columnAttr.name().isEmpty()) ? columnAttr.name() : field.getName();

            // Add to dictionary
            dictionary.put(name, value);

            // Handle keys if considerKeys is true
            if (considerKeys && columnAttr != null && field.isAnnotationPresent(KeyAttribute.class)) {
                where(name, value);
            }
        }

        return dictionary;
    }


    private Map<String, Object> buildKeyValuePairsFromObject(Object data) {
        return buildKeyValuePairsFromObject(data, false);
    }

}


