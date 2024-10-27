package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;
import io.github.ashwithpoojary98.clausses.fromclausses.AdHocTableFromClause;
import io.github.ashwithpoojary98.Query;

import java.util.Collections;

public class SqlServerCompiler extends Compiler {

    private boolean isUseLegacyPagination;

    public SqlServerCompiler() {
        openingIdentifier = "[";
        closingIdentifier = "]";
        lastId = "SELECT scope_identity() as Id";
        engineCode = EngineCodes.SQL_SERVER.getCode();
    }

    public boolean getUseLegacyPagination() {
        return this.isUseLegacyPagination;
    }

    public void setUseLegacyPagination(boolean isUseLegacyPagination) {
        this.isUseLegacyPagination = isUseLegacyPagination;
    }

    @Override
    protected SqlResult compileSelectQuery(Query query) {
        if (!getUseLegacyPagination() || !query.hasOffset(engineCode)) {
            return super.compileSelectQuery(query);
        }

        query = query.copy();

        SqlResult ctx = new SqlResult(parameterPlaceholder, escapeCharacter);
        ctx.setQuery(query);

        int limit = query.getLimit(engineCode);
        int offset = query.getOffset(engineCode);

        if (!query.hasComponent("select")) {
            query.select("*");
        }

        String order = compileOrders(ctx);
        if (order == null) {
            order = "ORDER BY (SELECT 0)";
        }

        query.selectRaw(String.format("ROW_NUMBER() OVER (%s) AS row_num", order), ctx.getBindings().toArray());

        query.clearComponent("order");

        SqlResult result = super.compileSelectQuery(query);

        if (limit == 0) {
            result.setRawSql(String.format("SELECT * FROM (%s) AS results_wrapper WHERE row_num >= %s", result.getRawSql(), parameterPlaceholder));
            result.getBindings().add(offset + 1);
        } else {
            result.setRawSql(String.format("SELECT * FROM (%s) AS results_wrapper WHERE row_num BETWEEN %s AND %s", result.getRawSql(), parameterPlaceholder, parameterPlaceholder));
            result.getBindings().add(offset + 1);
            result.getBindings().add(limit + offset);
        }

        return result;
    }

    @Override
    protected String compileColumns(SqlResult ctx) {
        String compiled = super.compileColumns(ctx);

        if (!getUseLegacyPagination()) {
            return compiled;
        }

        // Get the limit and offset from the query context
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        // If there's a limit but no offset, apply the TOP clause
        if (limit > 0 && offset == 0) {
            // Insert top bindings first
            ctx.getBindings().add(0, limit);

            ctx.getQuery().clearComponent("limit");

            // Handle distinct
            if (compiled.startsWith("SELECT DISTINCT")) {
                return String.format("SELECT DISTINCT TOP (%s)%s", parameterPlaceholder, compiled.substring(15));
            }

            return String.format("SELECT TOP (%s)%s", parameterPlaceholder, compiled.substring(6));
        }

        return compiled;
    }


    @Override
    public String compileLimit(SqlResult ctx) {
        if (getUseLegacyPagination()) {
            // In legacy versions of SQL Server, limit is handled by TOP
            // and ROW_NUMBER techniques
            return null;
        }

        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit == 0 && offset == 0) {
            return null;
        }

        String safeOrder = "";
        if (!ctx.getQuery().hasComponent("order")) {
            safeOrder = "ORDER BY (SELECT 0) ";
        }

        if (limit == 0) {
            ctx.getBindings().add(offset);
            return String.format("%sOFFSET %s ROWS", safeOrder, parameterPlaceholder);
        }

        ctx.getBindings().add(offset);
        ctx.getBindings().add(limit);

        return String.format("%sOFFSET %s ROWS FETCH NEXT %s ROWS ONLY", safeOrder, parameterPlaceholder, parameterPlaceholder);
    }


    @Override
    public String compileRandom() {
        return "NEWID()";
    }

    @Override
    public String compileTrue() {
        return "cast(1 as bit)";
    }

    @Override
    public String compileFalse() {
        return "cast(0 as bit)";
    }

    @Override
    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition condition) {
        String column = wrap(condition.getColumn());
        String part = condition.getPart().toUpperCase();

        String left;

        if ("TIME".equals(part) || "DATE".equals(part)) {
            left = String.format("CAST(%s AS %s)", column, part);
        } else {
            left = String.format("DATEPART(%s, %s)", part, column);
        }

        String sql = String.format("%s %s %s", left, condition.getOperator(), parameter(ctx, condition.getValue()));

        if (condition.isNot()) {
            return String.format("NOT (%s)", sql);
        }

        return sql;
    }

    @Override
    protected SqlResult compileAdHocQuery(AdHocTableFromClause adHoc) {
        SqlResult ctx = new SqlResult();

        String colNames = String.join(", ", adHoc.getColumns().stream().map(this::wrap).toArray(String[]::new));

        String valueRow = String.join(", ", Collections.nCopies(adHoc.getColumns().size(), parameterPlaceholder));
        String valueRows = String.join(", ", Collections.nCopies(adHoc.getValues().size() / adHoc.getColumns().size(), String.format("(%s)", valueRow)));
        String sql = String.format("SELECT %s FROM (VALUES %s) AS tbl (%s)", colNames, valueRows, colNames);

        ctx.setRawSql(sql);
        ctx.setBindings(adHoc.getValues());

        return ctx;
    }


}
