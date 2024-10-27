package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;
import io.github.ashwithpoojary98.clausses.insertclauses.AbstractInsertClause;
import io.github.ashwithpoojary98.Query;

import java.util.List;

public class FirebirdCompiler extends Compiler{

    public FirebirdCompiler()
    {
        this.engineCode=EngineCodes.FIREBIRD.getCode();
        this.singleRowDummyTableName="RDB$DATABASE";
    }

    @Override
    protected SqlResult compileInsertQuery(Query query) {
        SqlResult ctx = super.compileInsertQuery(query);

        List<AbstractInsertClause> inserts = ctx.getQuery().getComponents("insert",engineCode);
        if (!inserts.isEmpty()) {
            ctx.setRawSql(ctx.getRawSql().replaceAll("\\)\\s+VALUES\\s+\\(", ") SELECT "));
            ctx.setRawSql(ctx.getRawSql().replaceAll("\\),\\s*\\(", " FROM RDB$DATABASE UNION ALL SELECT "));
            ctx.setRawSql(ctx.getRawSql().replaceAll("\\)$", " FROM RDB$DATABASE"));
        }
        return ctx;
    }

    @Override
    public String compileLimit(SqlResult ctx) {
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit > 0 && offset > 0) {
            ctx.getBindings().add(offset + 1);
            ctx.getBindings().add(limit + offset);

            return String.format("ROWS %s TO %s", parameterPlaceholder, parameterPlaceholder);
        }

        return null;
    }

    @Override
    protected String compileColumns(SqlResult ctx) {
        String compiled = super.compileColumns(ctx);

        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit > 0 && offset == 0) {
            ctx.getBindings().add(0, limit);
            ctx.getQuery().clearComponent("limit");

            return String.format("SELECT FIRST %s%s", parameterPlaceholder, compiled.substring(6));
        } else if (limit == 0 && offset > 0) {
            ctx.getBindings().add(0, offset);
            ctx.getQuery().clearComponent("offset");

            return String.format("SELECT SKIP %s%s", parameterPlaceholder, compiled.substring(6));
        }

        return compiled;
    }

    @Override
    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition condition) {
        String column = wrap(condition.getColumn());

        String left;

        if ("time".equals(condition.getPart())) {
            left = String.format("CAST(%s AS TIME)", column);
        } else if ("date".equals(condition.getPart())) {
            left = String.format("CAST(%s AS DATE)", column);
        } else {
            left = String.format("EXTRACT(%s FROM %s)", condition.getPart().toUpperCase(), column);
        }

        String sql = String.format("%s %s %s", left, condition.getOperator(), parameter(ctx, condition.getValue()));

        if (condition.isNot()) {
            return String.format("NOT (%s)", sql);
        }

        return sql;
    }

    @Override
    public String wrapValue(String value) {
        return super.wrapValue(value).toUpperCase();
    }

    @Override
    public String compileTrue() {
        return "1";
    }

    @Override
    public String compileFalse() {
        return "0";
    }


}
