package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.Helper;
import io.github.ashwithpoojary98.Join;
import io.github.ashwithpoojary98.Query;
import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.UnsafeLiteral;
import io.github.ashwithpoojary98.Variable;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.clausses.AggregateClause;
import io.github.ashwithpoojary98.clausses.columns.AbstractColumn;
import io.github.ashwithpoojary98.clausses.columns.AggregatedColumn;
import io.github.ashwithpoojary98.clausses.columns.Column;
import io.github.ashwithpoojary98.clausses.columns.QueryColumn;
import io.github.ashwithpoojary98.clausses.columns.RawColumn;
import io.github.ashwithpoojary98.clausses.combines.AbstractCombine;
import io.github.ashwithpoojary98.clausses.combines.Combine;
import io.github.ashwithpoojary98.clausses.combines.RawCombine;
import io.github.ashwithpoojary98.clausses.conditions.AbstractCondition;
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
import io.github.ashwithpoojary98.clausses.fromclausses.AbstractFrom;
import io.github.ashwithpoojary98.clausses.fromclausses.AdHocTableFromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.FromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.QueryFromClause;
import io.github.ashwithpoojary98.clausses.fromclausses.RawFromClause;
import io.github.ashwithpoojary98.clausses.incrementclauses.IncrementClause;
import io.github.ashwithpoojary98.clausses.insertclauses.AbstractInsertClause;
import io.github.ashwithpoojary98.clausses.insertclauses.InsertClause;
import io.github.ashwithpoojary98.clausses.insertclauses.InsertQueryClause;
import io.github.ashwithpoojary98.clausses.joins.BaseJoin;
import io.github.ashwithpoojary98.clausses.orderbyclauses.OrderBy;
import io.github.ashwithpoojary98.clausses.orderbyclauses.RawOrderBy;
import io.github.ashwithpoojary98.exceptions.InvalidClauseException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Compiler {

    private final ConditionsCompilerProvider compileConditionMethodsProvider;

    protected String parameterPlaceholder = "?";
    protected String parameterPrefix = "@p";
    protected String openingIdentifier = "\"";
    protected String closingIdentifier = "\"";
    protected String columnAsKeyword = "AS ";
    protected String tableAsKeyword = "AS ";
    protected String lastId = "";
    protected String escapeCharacter = "\\";
    protected String engineCode;
    private boolean supportsFilterClause = false;
    private boolean omitSelectInsideExists = true;
    protected String singleRowDummyTableName;


    protected String singleInsertStartClause = "INSERT INTO";
    protected String multiInsertStartClause = "INSERT INTO";


    protected Compiler() {
        compileConditionMethodsProvider = new ConditionsCompilerProvider(this);
    }

    protected Method findCompilerMethodInfo(Class<?> clauseType, String methodName) {
        return compileConditionMethodsProvider.getMethodInfo(clauseType, methodName);
    }

    public String getEngineCode() {
        return this.engineCode;
    }

    public boolean getSupportsFilterClause() {
        return this.supportsFilterClause;
    }

    public void setSupportsFilterClause(boolean supportsFilterClause) {
        this.supportsFilterClause = supportsFilterClause;
    }

    public boolean getOmitSelectInsideExists() {
        return this.omitSelectInsideExists;
    }

    public void setOmitSelectInsideExists(boolean omitSelectInsideExists) {
        this.omitSelectInsideExists = omitSelectInsideExists;
    }

    protected final HashSet<String> operators = new HashSet<>() {{
        add("=");
        add("<");
        add(">");
        add("<=");
        add(">=");
        add("<>");
        add("!=");
        add("<=>");
        add("like");
        add("not like");
        add("ilike");
        add("not ilike");
        add("like binary");
        add("not like binary");
        add("rlike");
        add("not rlike");
        add("regexp");
        add("not regexp");
        add("similar to");
        add("not similar to");
    }};

    protected HashSet<String> userOperators = new HashSet<>();

    protected Map<String, Object> generateNamedBindings(Object[] bindings) {
        Map<String, Object> namedBindings = new HashMap<>();
        for (int i = 0; i < bindings.length; i++) {
            namedBindings.put(parameterPrefix + i, bindings[i]);
        }
        return namedBindings;
    }

    protected SqlResult prepareResult(SqlResult ctx) {
        ctx.setNamedBindings(generateNamedBindings(ctx.getBindings().toArray()));
        ctx.setSql(Helper.replaceAll(ctx.getRawSql(), parameterPlaceholder, escapeCharacter, i -> parameterPrefix + i));
        return ctx;
    }

    private Query transformAggregateQuery(Query query) {
        AggregateClause clause = query.getOneComponent("aggregate", engineCode);

        // Return early if there is only one column and the query is not distinct
        if (clause.getColumns().size() == 1 && !query.getIsDistinct()) {
            return query;
        }

        if (query.getIsDistinct()) {
            query.clearComponent("aggregate", engineCode);
            query.clearComponent("select", engineCode);
            query.select(clause.getColumns().toArray(new String[0]));
        } else {
            for (String column : clause.getColumns()) {
                query.whereNotNull(column);
            }
        }

        AggregateClause outerClause = new AggregateClause();
        outerClause.setColumns(Collections.singletonList("*"));
        outerClause.setType(clause.getType());

        return new Query()
                .addComponent("aggregate", outerClause)
                .from(query, clause.getType() + "Query");
    }

    public Compiler whitelist(String... operators) {
        Collections.addAll(this.userOperators, operators);
        return this;
    }

    public SqlResult compile(Query query) {
        SqlResult ctx = compileRaw(query);
        ctx = prepareResult(ctx);
        return ctx;
    }

    public SqlResult compile(List<Query> queries) {
        List<SqlResult> compiled = queries.stream()
                .map(this::compileRaw)
                .collect(Collectors.toList());

        List<List<Object>> bindings = compiled.stream()
                .map(SqlResult::getBindings)
                .collect(Collectors.toList());

        int totalBindingsCount = bindings.stream()
                .map(List::size)
                .reduce(0, Integer::sum);

        List<Object> combinedBindings = new ArrayList<>(totalBindingsCount);
        for (List<Object> cb : bindings) {
            combinedBindings.addAll(cb);
        }

        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setRawSql(compiled.stream()
                .map(SqlResult::getRawSql)
                .collect(Collectors.joining(";\n")));
        ctx.setBindings(combinedBindings);

        ctx = prepareResult(ctx);

        return ctx;
    }

    protected SqlResult compileSelectQuery(Query query) {
        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setQuery(query.copy());

        List<String> results = Stream.of(
                        compileColumns(ctx),
                        compileFrom(ctx),
                        compileJoins(ctx),
                        compileWheres(ctx),
                        compileGroups(ctx),
                        compileHaving(ctx),
                        compileOrders(ctx),
                        compileLimit(ctx),
                        compileUnion(ctx)
                )
                .filter(x -> x != null && !x.isEmpty())
                .collect(Collectors.toList());

        String sql = String.join(" ", results);
        ctx.setRawSql(sql);

        return ctx;
    }

    protected SqlResult compileRaw(Query query) {
        SqlResult ctx;

        switch (query.getMethod()) {
            case "insert":
                ctx = compileInsertQuery(query);
                break;
            case "update":
                ctx = compileUpdateQuery(query);
                break;
            case "delete":
                ctx = compileDeleteQuery(query);
                break;
            case "aggregate":
                query.clearComponent("limit", engineCode)
                        .clearComponent("order", engineCode)
                        .clearComponent("group", engineCode);
                query = transformAggregateQuery(query);
                ctx = compileSelectQuery(query);
                break;
            default:
                ctx = compileSelectQuery(query);
                break;
        }

        // Handle CTEs
        if (query.hasComponent("cte", engineCode)) {
            ctx = compileCteQuery(ctx, query);
        }

        ctx.setRawSql(Helper.expandParameters(ctx.getRawSql(), parameterPlaceholder, escapeCharacter, ctx.getBindings().toArray()));

        return ctx;
    }


    protected SqlResult compileInsertQuery(Query query) {
        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setQuery(query);

        if (!ctx.getQuery().hasComponent("from", engineCode)) {
            throw new IllegalStateException("No table set to insert");
        }

        AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", engineCode);
        if (fromClause == null) {
            throw new IllegalStateException("Invalid table expression");
        }

        String table = null;
        if (fromClause instanceof FromClause) {
            FromClause fromClauseCast = (FromClause) fromClause;
            table = wrap(fromClauseCast.getTable());
        } else if (fromClause instanceof RawFromClause) {
            RawFromClause rawFromClause = (RawFromClause) fromClause;
            table = wrapIdentifiers(rawFromClause.getExpression());
            ctx.getBindings().addAll(Arrays.asList(rawFromClause.getBindings()));
        }

        if (table == null) {
            throw new IllegalStateException("Invalid table expression");
        }

        List<AbstractInsertClause> inserts = ctx.getQuery().getComponents("insert", engineCode);
        if (inserts.get(0) instanceof InsertQueryClause) {
            InsertQueryClause insertQueryClause = (InsertQueryClause) inserts.get(0);
            return compileInsertQueryClause(ctx, table, insertQueryClause);
        } else {
            return compileValueInsertClauses(ctx, table, (List<InsertClause>) (List<?>) inserts);
        }
    }

    protected SqlResult compileUpdateQuery(Query query) {
        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setQuery(query);

        if (!ctx.getQuery().hasComponent("from", engineCode)) {
            throw new IllegalStateException("No table set to update");
        }

        AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", engineCode);

        String table = null;

        if (fromClause instanceof FromClause) {
            FromClause fromClauseCast = (FromClause) fromClause;
            table = wrap(fromClauseCast.getTable());
        } else if (fromClause instanceof RawFromClause) {
            RawFromClause rawFromClause = (RawFromClause) fromClause;
            table = wrapIdentifiers(rawFromClause.getExpression());
            ctx.getBindings().addAll(Arrays.asList(rawFromClause.getBindings()));
        }

        if (table == null) {
            throw new IllegalStateException("Invalid table expression");
        }

        // Check for increment statements
        AbstractClause clause = ctx.getQuery().getOneComponent("update", engineCode);

        String wheres;

        if (clause instanceof IncrementClause) {
            IncrementClause increment = (IncrementClause) clause;
            String column = wrap(increment.getColumn());
            String value = parameter(ctx, Math.abs(increment.getValue()));
            String sign = increment.getValue() >= 0 ? "+" : "-";

            wheres = compileWheres(ctx);

            if (!wheres.isEmpty()) {
                wheres = " " + wheres;
            }

            ctx.setRawSql(String.format("UPDATE %s SET %s = %s %s %s%s", table, column, column, sign, value, wheres));

            return ctx;
        }

        // Handle other cases (not shown in the original code)
        // You may need to add more logic here depending on your requirements

        return ctx;
    }

    protected SqlResult compileDeleteQuery(Query query) {
        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setQuery(query);

        if (!ctx.getQuery().hasComponent("from", engineCode)) {
            throw new IllegalStateException("No table set to delete");
        }

        AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", engineCode);

        String table = null;

        if (fromClause instanceof FromClause) {
            FromClause fromClauseCast = (FromClause) fromClause;
            table = wrap(fromClauseCast.getTable());
        } else if (fromClause instanceof RawFromClause) {
            RawFromClause rawFromClause = (RawFromClause) fromClause;
            table = wrapIdentifiers(rawFromClause.getExpression());
            ctx.getBindings().addAll(Arrays.asList(rawFromClause.getBindings()));
        }

        if (table == null) {
            throw new IllegalStateException("Invalid table expression");
        }

        String joins = compileJoins(ctx);
        String where = compileWheres(ctx);

        if (!where.isEmpty()) {
            where = " " + where;
        }

        if (joins.isEmpty()) {
            ctx.setRawSql(String.format("DELETE FROM %s%s", table, where));
        } else {
            // Check if we have an alias
            if (fromClause instanceof FromClause && !fromClause.getAlias().isEmpty()) {
                ctx.setRawSql(String.format("DELETE %s FROM %s %s%s", wrap(fromClause.getAlias()), table, joins, where));
            } else {
                ctx.setRawSql(String.format("DELETE %s FROM %s %s%s", table, table, joins, where));
            }
        }

        return ctx;
    }


    protected SqlResult compileInsertQueryClause(SqlResult ctx, String table, InsertQueryClause clause) {
        String columns = getInsertColumnsList(clause.getColumns());

        SqlResult subCtx = compileSelectQuery(clause.getQuery());
        ctx.getBindings().addAll(subCtx.getBindings());

        ctx.setRawSql(singleInsertStartClause + " " + table + columns + " " + subCtx.getRawSql());

        return ctx;
    }

    protected String getInsertColumnsList(List<String> columnList) {
        String columns = "";
        if (!columnList.isEmpty()) {
            columns = " (" + String.join(", ", wrapArray(columnList)) + ")";
        }
        return columns;
    }

    public List<String> wrapArray(List<String> values) {
        return values.stream()
                .map(this::wrap)
                .collect(Collectors.toList());
    }


    protected SqlResult compileValueInsertClauses(SqlResult ctx, String table, List<InsertClause> insertClauses) {
        boolean isMultiValueInsert = insertClauses.stream()
                .skip(1)
                .findAny()
                .isPresent();

        String insertInto = isMultiValueInsert ? multiInsertStartClause : singleInsertStartClause;

        InsertClause firstInsert = insertClauses.iterator().next();
        String columns = getInsertColumnsList(firstInsert.getColumns());
        String values = String.join(", ", parameterize(ctx, firstInsert.getValues()));

        ctx.setRawSql(insertInto + " " + table + columns + " VALUES (" + values + ")");

        if (isMultiValueInsert) {
            return compileRemainingInsertClauses(ctx, table, insertClauses);
        }

        if (firstInsert.isReturnId() && lastId != null && !lastId.isEmpty()) {
            ctx.setRawSql(ctx.getRawSql() + ";" + lastId);
        }

        return ctx;
    }


    public String wrap(String value) {
        if (value.toLowerCase().contains(" as ")) {
            String[] splitAlias = splitAlias(value);
            String before = splitAlias[0];
            String after = splitAlias[1];

            return wrap(before) + " " + columnAsKeyword + wrapValue(after);
        }

        if (value.contains(".")) {
            String[] parts = value.split("\\.");
            return String.join(".", Arrays.stream(parts)
                    .map(this::wrapValue)
                    .toArray(String[]::new));
        }

        // If we reach here, the value does not contain an "AS" alias nor a dot "." expression, so wrap it as a regular value.
        return wrapValue(value);
    }

    public String wrapValue(String value) {
        if (value.equals("*")) return value;

        String opening = this.openingIdentifier;
        String closing = this.closingIdentifier;

        if (opening.isEmpty() && closing.isEmpty()) return value;

        return opening + value.replace(closing, closing + closing) + closing;
    }

    public Object resolve(SqlResult ctx, Object parameter) {
        // if we face a literal value we have to return it directly
        if (parameter instanceof UnsafeLiteral) {
            UnsafeLiteral literal = (UnsafeLiteral) parameter;
            return literal.getValue();
        }

        // if we face a variable we have to lookup the variable from the predefined variables
        if (parameter instanceof Variable) {
            Variable variable = (Variable) parameter;
            return ctx.getQuery().findVariable(variable.getName());
        }

        return parameter;
    }


    public String[] splitAlias(String value) {
        int index = value.toLowerCase().lastIndexOf(" as ");

        if (index > 0) {
            String before = value.substring(0, index);
            String after = value.substring(index + 4);
            return new String[]{before, after};
        }

        return new String[]{value, null};
    }

    public String wrapIdentifiers(String input) {
        input = Helper.replaceIdentifierUnlessEscaped(this.escapeCharacter, "{", this.openingIdentifier, input);
        input = Helper.replaceIdentifierUnlessEscaped(this.escapeCharacter, "}", this.closingIdentifier, input);
        input = Helper.replaceIdentifierUnlessEscaped(this.escapeCharacter, "[", this.openingIdentifier, input);
        input = Helper.replaceIdentifierUnlessEscaped(this.escapeCharacter, "]", this.closingIdentifier, input);
        return input;
    }

    public <T> String parameterize(SqlResult ctx, Iterable<T> values) {
        List<String> parameterizedValues = new ArrayList<>();
        for (T value : values) {
            parameterizedValues.add(parameter(ctx, value));
        }
        return String.join(", ", parameterizedValues);
    }

    public String parameter(SqlResult ctx, Object parameter) {
        // If we face a literal value, return it directly
        if (parameter instanceof UnsafeLiteral) {
            UnsafeLiteral literal = (UnsafeLiteral) parameter;
            return literal.getValue();
        }

        // If we face a variable, lookup the variable from predefined variables
        if (parameter instanceof Variable) {
            Variable variable = (Variable) parameter;
            Object value = ctx.getQuery().findVariable(variable.getName());
            ctx.getBindings().add(value);
            return parameterPlaceholder;
        }

        ctx.getBindings().add(parameter);
        return parameterPlaceholder;
    }

    protected SqlResult compileRemainingInsertClauses(SqlResult ctx, String table, List<InsertClause> inserts) {
        // Skip the first insert clause and iterate over the rest
        for (InsertClause insert : inserts.stream().skip(1).collect(Collectors.toList())) {
            String values = String.join(", ", parameterize(ctx, insert.getValues()));
            ctx.setRawSql(ctx.getRawSql() + ", (" + values + ")");
        }
        return ctx;
    }


    protected SqlResult compileCteQuery(SqlResult ctx, Query query) {
        CteFinder cteFinder = new CteFinder(query, engineCode);
        List<AbstractFrom> cteSearchResult = cteFinder.find();

        StringBuilder rawSql = new StringBuilder("WITH ");
        List<Object> cteBindings = new ArrayList<>();

        for (AbstractFrom cte : cteSearchResult) {
            SqlResult cteCtx = compileCte(cte);

            cteBindings.addAll(cteCtx.getBindings());
            rawSql.append(cteCtx.getRawSql().trim()).append(",\n");
        }

        // Remove last comma
        if (rawSql.length() > 5) {
            rawSql.setLength(rawSql.length() - 2);
        }

        rawSql.append('\n').append(ctx.getRawSql());

        ctx.getBindings().addAll(0, cteBindings);
        ctx.setRawSql(rawSql.toString());

        return ctx;
    }

    protected SqlResult onBeforeSelect(SqlResult ctx) {
        return ctx;
    }

    public SqlResult compileCte(AbstractFrom cte) {
        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);

        if (cte == null) {
            return ctx;
        }

        if (cte instanceof RawFromClause) {
            RawFromClause raw = (RawFromClause) cte;
            ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
            ctx.setRawSql(String.format("%s AS (%s)", wrapValue(raw.getAlias()), wrapIdentifiers(raw.getExpression())));
        } else if (cte instanceof QueryFromClause) {
            QueryFromClause queryFromClause = (QueryFromClause) cte;
            SqlResult subCtx = compileSelectQuery(queryFromClause.getQuery());
            ctx.getBindings().addAll(subCtx.getBindings());

            ctx.setRawSql(String.format("%s AS (%s)", wrapValue(queryFromClause.getAlias()), subCtx.getRawSql()));
        } else if (cte instanceof AdHocTableFromClause) {
            AdHocTableFromClause adHoc = (AdHocTableFromClause) cte;
            SqlResult subCtx = compileAdHocQuery(adHoc);
            ctx.getBindings().addAll(subCtx.getBindings());

            ctx.setRawSql(String.format("%s AS (%s)", wrapValue(adHoc.getAlias()), subCtx.getRawSql()));
        }

        return ctx;
    }

    protected SqlResult compileAdHocQuery(AdHocTableFromClause adHoc) {
        SqlResult ctx = new SqlResult();

        String row = "SELECT " + String.join(", ", adHoc.getColumns().stream()
                .map(col -> String.format("%s AS %s", parameterPlaceholder, wrap(col)))
                .toArray(String[]::new));

        String fromTable = singleRowDummyTableName;

        if (fromTable != null) {
            row += String.format(" FROM %s", fromTable);
        }

        int numberOfRows = adHoc.getValues().size() / adHoc.getColumns().size();
        String rows = String.join(" UNION ALL ", Collections.nCopies(numberOfRows, row));

        ctx.setRawSql(rows);
        ctx.setBindings(adHoc.getValues());

        return ctx;
    }


    protected String compileColumns(SqlResult ctx) {
        if (ctx.getQuery().hasComponent("aggregate", engineCode)) {
            AggregateClause aggregate = ctx.getQuery().getOneComponent("aggregate", engineCode);

            List<String> aggregateColumns = aggregate.getColumns().stream()
                    .map(x -> compileColumn(ctx, new Column(x)))
                    .collect(Collectors.toList());

            String sql;

            if (aggregateColumns.size() == 1) {
                sql = String.join(", ", aggregateColumns);

                if (ctx.getQuery().getIsDistinct()) {
                    sql = "DISTINCT " + sql;
                }

                return "SELECT " + aggregate.getType().toUpperCase() + "(" + sql + ") " + columnAsKeyword + wrapValue(aggregate.getType());
            }

            return "SELECT 1";
        }

        List<String> columns = ctx.getQuery()
                .getComponents("select", engineCode).stream()
                .map(x -> compileColumn(ctx, (AbstractColumn) x))
                .collect(Collectors.toList());

        String distinct = ctx.getQuery().getIsDistinct() ? "DISTINCT " : "";
        String select = !columns.isEmpty() ? String.join(", ", columns) : "*";

        return String.format("SELECT %s%s", distinct, select);
    }

    protected String compileColumn(SqlResult ctx, AbstractColumn column) {
        if (column instanceof RawColumn) {
            RawColumn raw = (RawColumn) column;
            ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
            return wrapIdentifiers(raw.getExpression());
        }

        if (column instanceof QueryColumn) {
            QueryColumn queryColumn = (QueryColumn) column;
            String alias = "";

            if (!queryColumn.getQuery().getQueryAlias().isEmpty()) {
                alias = " " + columnAsKeyword + wrapValue(queryColumn.getQuery().getQueryAlias());
            }

            SqlResult subCtx = compileSelectQuery(queryColumn.getQuery());
            ctx.getBindings().addAll(subCtx.getBindings());

            return "(" + subCtx.getRawSql() + ")" + alias;
        }

        if (column instanceof AggregatedColumn) {
            AggregatedColumn aggregatedColumn = (AggregatedColumn) column;
            String agg = aggregatedColumn.getAggregate().toUpperCase();

            String[] result = splitAlias(compileColumn(ctx, aggregatedColumn.getColumn()));
            String col = result[0];
            String alias = result[1] != null ? " " + columnAsKeyword + result[1] : "";

            String filterCondition = compileFilterConditions(ctx, aggregatedColumn);

            if (filterCondition.isEmpty()) {
                return agg + "(" + col + ")" + alias;
            }

            if (supportsFilterClause) {
                return agg + "(" + col + ") FILTER (WHERE " + filterCondition + ")" + alias;
            }

            return agg + "(CASE WHEN " + filterCondition + " THEN " + col + " END)" + alias;
        }

        return wrap(((Column) column).getName());
    }

    protected String compileFilterConditions(SqlResult ctx, AggregatedColumn aggregatedColumn) {
        if (aggregatedColumn.getFilter() == null) {
            return null;
        }

        List<AbstractCondition> wheres = aggregatedColumn.getFilter().getComponents("where");

        return compileConditions(ctx, wheres);
    }


    protected String compileConditions(SqlResult ctx, List<AbstractCondition> conditions) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < conditions.size(); i++) {
            String compiled = compileCondition(ctx, conditions.get(i));

            if (compiled == null || compiled.isEmpty()) {
                continue;
            }

            String boolOperator = (i == 0) ? "" : (conditions.get(i).isOr() ? "OR " : "AND ");

            result.add(boolOperator + compiled);
        }

        return String.join(" ", result);
    }

    protected String compileCondition(SqlResult ctx, AbstractCondition clause) {
        Class<?> clauseType = clause.getClass();
        String name = clauseType.getSimpleName();

        // Remove "Condition" suffix
        if (name.endsWith("Condition")) {
            name = name.substring(0, name.length() - "Condition".length());
        }

        String methodName = "compile" + name + "Condition";

        try {
            Method method = findCompilerMethodInfo(clauseType, methodName);
            return (String) method.invoke(this, ctx, clause);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to invoke '" + methodName + "'", ex);
        }
    }

    protected String compileRawCondition(SqlResult ctx, RawCondition x) {
        ctx.getBindings().addAll(Arrays.asList(x.getBindings()));
        return wrapIdentifiers(x.getExpression());
    }

    protected String compileQueryCondition(SqlResult ctx, QueryCondition x) {
        SqlResult subCtx = compileSelectQuery(x.getQuery());

        ctx.getBindings().addAll(subCtx.getBindings());

        return wrap(x.getColumn()) + " " + checkOperator(x.getOperator()) + " (" + subCtx.getRawSql() + ")";
    }

    protected String compileSubQueryCondition(SqlResult ctx, SubQueryCondition x) {
        SqlResult subCtx = compileSelectQuery(x.getQuery());

        ctx.getBindings().addAll(subCtx.getBindings());

        return "(" + subCtx.getRawSql() + ") " + checkOperator(x.getOperator()) + " " + parameter(ctx, x.getValue());
    }

    protected String compileBasicCondition(SqlResult ctx, BasicCondition x) {
        String sql = wrap(x.getColumn()) + " " + checkOperator(x.getOperator()) + " " + parameter(ctx, x.getValue());

        if (x.isNot()) {
            return "NOT (" + sql + ")";
        }

        return sql;
    }

    protected String compileBasicStringCondition(SqlResult ctx, BasicStringCondition x) {
        String column = wrap(x.getColumn());
        String value = (String) resolve(ctx, x.getValue());

        if (value == null) {
            throw new IllegalArgumentException("Expecting a non-nullable string");
        }

        String method = x.getOperator();

        if (Arrays.asList("starts", "ends", "contains", "like").contains(x.getOperator())) {
            method = "LIKE";

            switch (x.getOperator()) {
                case "starts":
                    value = value + "%";
                    break;
                case "ends":
                    value = "%" + value;
                    break;
                case "contains":
                    value = "%" + value + "%";
                    break;
                default:
            }
        }

        String sql;

        if (!x.isCaseSensitive()) {
            column = compileLower(column);
            value = value.toLowerCase();
        }

        if (x.getValue() instanceof UnsafeLiteral) {
            sql = column + " " + checkOperator(method) + " " + value;
        } else {
            sql = column + " " + checkOperator(method) + " " + parameter(ctx, value);
        }
        String currentEscapeCharacter = x.getEscapeCharacter();
        if (currentEscapeCharacter != null && !currentEscapeCharacter.isEmpty()) {
            sql = sql + " ESCAPE '" + currentEscapeCharacter + "'";
        }

        return x.isNot() ? "NOT (" + sql + ")" : sql;
    }

    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition x) {
        String column = wrap(x.getColumn());
        String op = checkOperator(x.getOperator());

        String sql = String.format("%s(%s) %s %s", x.getPart().toUpperCase(), column, op, parameter(ctx, x.getValue()));

        return x.isNot() ? "NOT (" + sql + ")" : sql;
    }

    protected String compileNestedCondition(SqlResult ctx, NestedCondition x) {
        if (!(x.getQuery().hasComponent("where", engineCode) || x.getQuery().hasComponent("having", engineCode))) {
            return null;
        }

        String clause = x.getQuery().hasComponent("where", engineCode) ? "where" : "having";

        List clauses = x.getQuery().getComponents(clause, engineCode);

        String sql = compileConditions(ctx, clauses);

        return x.isNot() ? "NOT (" + sql + ")" : "(" + sql + ")";
    }

    protected String compileTwoColumnsCondition(SqlResult ctx, TwoColumnsCondition clause) {
        String op = clause.isNot() ? "NOT " : "";
        return op + wrap(clause.getFirst()) + " " + checkOperator(clause.getOperator()) + " " + wrap(clause.getSecond());
    }

    protected String compileBetweenCondition(SqlResult ctx, BetweenCondition item) {
        String between = item.isNot() ? "NOT BETWEEN" : "BETWEEN";
        String lower = parameter(ctx, item.getLower());
        String higher = parameter(ctx, item.getHigher());

        return wrap(item.getColumn()) + " " + between + " " + lower + " AND " + higher;
    }

    protected String compileInCondition(SqlResult ctx, InCondition item) {
        String column = wrap(item.getColumn());

        if (item.getValues().isEmpty()) {
            return item.isNot() ? "1 = 1 /* NOT IN [empty list] */" : "1 = 0 /* IN [empty list] */";
        }

        String inOperator = item.isNot() ? "NOT IN" : "IN";
        String values = parameterize(ctx, item.getValues());

        return column + " " + inOperator + " (" + values + ")";
    }

    protected String compileInQueryCondition(SqlResult ctx, InQueryCondition item) {
        SqlResult subCtx = compileSelectQuery(item.getQuery());

        ctx.getBindings().addAll(subCtx.getBindings());

        String inOperator = item.isNot() ? "NOT IN" : "IN";

        return wrap(item.getColumn()) + " " + inOperator + " (" + subCtx.getRawSql() + ")";
    }

    protected String compileNullCondition(SqlResult ctx, NullCondition item) {
        String op = item.isNot() ? "IS NOT NULL" : "IS NULL";
        return wrap(item.getColumn()) + " " + op;
    }

    protected String compileBooleanCondition(SqlResult ctx, BooleanCondition item) {
        String column = wrap(item.getColumn());
        String value = item.isValue() ? compileTrue() : compileFalse();
        String op = item.isNot() ? "!=" : "=";

        return column + " " + op + " " + value;
    }

    protected String compileExistsCondition(SqlResult ctx, ExistsCondition item) {
        String op = item.isNot() ? "NOT EXISTS" : "EXISTS";

        // remove unneeded components
        Query query = item.getQuery().copy();

        if (omitSelectInsideExists) {
            query.clearComponent("select").selectRaw("1");
        }

        SqlResult subCtx = compileSelectQuery(query);

        ctx.getBindings().addAll(subCtx.getBindings());

        return op + " (" + subCtx.getRawSql() + ")";
    }


    protected String compileFrom(SqlResult ctx) {
        if (ctx.getQuery().hasComponent("from", engineCode)) {
            AbstractFrom from = ctx.getQuery().getOneComponent("from", engineCode);
            return "FROM " + compileTableExpression(ctx, from);
        }
        return "";
    }

    public String compileJoins(SqlResult ctx) {
        if (!ctx.getQuery().hasComponent("join", engineCode)) {
            return null;
        }

        List<String> joins = ctx.getQuery()
                .getComponents("join", engineCode)
                .stream()
                .map(join -> compileJoin(ctx, ((BaseJoin) join).getJoin()))
                .collect(Collectors.toList());

        return "\n" + String.join("\n", joins);
    }

    public String compileJoin(SqlResult ctx, Join join, boolean isNested) {
        AbstractFrom from = join.getOneComponent("from", engineCode);
        List<AbstractCondition> conditions = join.getComponents("where", engineCode);

        String joinTable = compileTableExpression(ctx, from);
        String constraints = compileConditions(ctx, conditions);

        String onClause = !conditions.isEmpty() ? " ON " + constraints : "";

        return String.format("%s %s%s", join.getType(), joinTable, onClause);
    }

    public String compileJoin(SqlResult ctx, Join join) {
        return compileJoin(ctx, join, false);
    }

    public String compileWheres(SqlResult ctx) {
        if (!ctx.getQuery().hasComponent("where", engineCode)) {
            return null;
        }

        List<AbstractCondition> conditions = ctx.getQuery().getComponents("where", engineCode);
        String sql = compileConditions(ctx, conditions).trim();

        return sql.isEmpty() ? null : "WHERE " + sql;
    }

    public String compileGroups(SqlResult ctx) {
        if (!ctx.getQuery().hasComponent("group", engineCode)) {
            return null;
        }

        List<AbstractColumn> columns = ctx.getQuery()
                .getComponents("group", engineCode);

        String groupByColumns = columns.stream()
                .map(column -> compileColumn(ctx, column))
                .collect(Collectors.joining(", "));

        return "GROUP BY " + groupByColumns;
    }

    public String compileHaving(SqlResult ctx) {
        if (!ctx.getQuery().hasComponent("having", engineCode)) {
            return null;
        }

        List<String> sql = new ArrayList<>();
        String boolOperator="";

        List<AbstractCondition> having = ctx.getQuery()
                .getComponents("having", engineCode);

        for (int i = 0; i < having.size(); i++) {
            String compiled = compileCondition(ctx, having.get(i));

            if (!compiled.isEmpty()) {
                if (i > 0) {
                    boolOperator = having.get(i).isOr() ? "OR " : "AND ";
                }
                sql.add(boolOperator + compiled);
            }
        }

        return "HAVING " + String.join(" ", sql);
    }

    public String compileRandom() {
        return "RANDOM()";
    }

    public String compileLower(String value) {
        return "LOWER(" + value + ")";
    }

    public String compileUpper(String value) {
        return "UPPER(" + value + ")";
    }

    public String compileTrue() {
        return "true";
    }

    public String compileFalse() {
        return "false";
    }

    protected String checkOperator(String op) {
        op = op.toLowerCase();

        boolean valid = operators.contains(op) || userOperators.contains(op);

        if (!valid) {
            throw new IllegalArgumentException("The operator '" + op + "' cannot be used. Please consider whitelisting it before using it.");
        }

        return op;
    }


    public String compileOrders(SqlResult ctx) {
        if (!ctx.getQuery().hasComponent("order", engineCode)) {
            return null;
        }

        List<String> columns = ctx.getQuery()
                .getComponents("order", engineCode)
                .stream()
                .map(x -> {
                    if (x instanceof RawOrderBy) {
                        RawOrderBy raw = (RawOrderBy) x;
                        ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
                        return wrapIdentifiers(raw.getExpression());
                    }

                    OrderBy orderBy = (OrderBy) x;
                    String direction = orderBy.isAscending() ? "" : " DESC";
                    return wrap(orderBy.getColumn()) + direction;
                })
                .collect(Collectors.toList());

        return "ORDER BY " + String.join(", ", columns);
    }

    public String compileLimit(SqlResult ctx) {
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit == 0 && offset == 0) {
            return null;
        }

        if (offset == 0) {
            ctx.getBindings().add(limit);
            return "LIMIT " + parameterPlaceholder;
        }

        if (limit == 0) {
            ctx.getBindings().add(offset);
            return "OFFSET " + parameterPlaceholder;
        }

        ctx.getBindings().add(limit);
        ctx.getBindings().add(offset);

        return "LIMIT " + parameterPlaceholder + " OFFSET " + parameterPlaceholder;
    }


    public String compileUnion(SqlResult ctx) {
        // Handle UNION, EXCEPT and INTERSECT
        if (!ctx.getQuery().getComponents("combine", engineCode).isEmpty()) {
            List<String> combinedQueries = new ArrayList<>();

            List<AbstractCombine> clauses = ctx.getQuery().getComponents("combine", engineCode);

            for (AbstractCombine clause : clauses) {
                if (clause instanceof Combine) {
                    Combine combineClause = (Combine) clause;
                    String combineOperator = combineClause.getOperation().toUpperCase() + " " + (combineClause.isAll() ? "ALL " : "");

                    SqlResult subCtx = compileSelectQuery(combineClause.getQuery());
                    ctx.getBindings().addAll(subCtx.getBindings());

                    combinedQueries.add(combineOperator + subCtx.getRawSql());
                } else if (clause instanceof RawCombine) {
                    RawCombine combineRawClause = (RawCombine) clause;
                    ctx.getBindings().addAll(Arrays.asList(combineRawClause.getBindings()));
                    combinedQueries.add(wrapIdentifiers(combineRawClause.getExpression()));
                }
            }

            return String.join(" ", combinedQueries);
        }

        return null;
    }


    public String compileTableExpression(SqlResult ctx, AbstractFrom from) {
        if (from instanceof RawFromClause) {
            RawFromClause raw = (RawFromClause) from;
            ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
            return wrapIdentifiers(raw.getExpression());
        }

        if (from instanceof QueryFromClause) {
            QueryFromClause queryFromClause = (QueryFromClause) from;
            Query fromQuery = queryFromClause.getQuery();

            String alias = fromQuery.getQueryAlias().isEmpty()
                    ? ""
                    : " " + tableAsKeyword + wrapValue(fromQuery.getQueryAlias());

            SqlResult subCtx = compileSelectQuery(fromQuery);
            ctx.getBindings().addAll(subCtx.getBindings());

            return "(" + subCtx.getRawSql() + ")" + alias;
        }

        if (from instanceof FromClause) {
            FromClause fromClause = (FromClause) from;
            return wrap(fromClause.getTable());
        }

        throw new InvalidClauseException(String.format("Invalid type \"%s\" provided for the \"TableExpression\" clause", from.getClass().getSimpleName()));
    }


}
