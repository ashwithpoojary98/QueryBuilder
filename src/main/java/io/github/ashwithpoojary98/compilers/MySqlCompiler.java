package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;

public class MySqlCompiler extends Compiler{

    public MySqlCompiler()
    {
        openingIdentifier = closingIdentifier = "`";
        lastId = "SELECT last_insert_id() as Id";
        engineCode=EngineCodes.MYSQL.getCode();
    }

    @Override
    public String compileLimit(SqlResult ctx) {
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (offset == 0 && limit == 0) {
            return null;
        }

        if (offset == 0) {
            ctx.getBindings().add(limit);
            return String.format("LIMIT %s", parameterPlaceholder);
        }

        if (limit == 0) {
            // MySQL will not accept offset without limit, so we will put a large number
            // to avoid this error.
            ctx.getBindings().add(offset);
            return String.format("LIMIT 18446744073709551615 OFFSET %s", parameterPlaceholder);
        }

        // We have both values
        ctx.getBindings().add(limit);
        ctx.getBindings().add(offset);

        return String.format("LIMIT %s OFFSET %s", parameterPlaceholder, parameterPlaceholder);
    }


}
