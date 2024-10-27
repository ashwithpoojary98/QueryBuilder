package io.github.ashwithpoojary98.extensions;

import io.github.ashwithpoojary98.compilers.EngineCodes;
import io.github.ashwithpoojary98.Query;

import java.util.function.Function;

public class QueryForExtensions {

    public static Query forFirebird(Query src, Function<Query, Query> fn) {
        return src.forEngine(EngineCodes.FIREBIRD, fn);
    }
    public static Query forMySql(Query src, Function<Query, Query> fn)
    {
        return src.forEngine(EngineCodes.MYSQL, fn);
    }

    public static Query forOracle( Query src, Function<Query, Query> fn)
    {
        return src.forEngine(EngineCodes.ORACLE, fn);
    }

    public static Query forPostgreSql( Query src, Function<Query, Query> fn)
    {
        return src.forEngine(EngineCodes.POSTGRES, fn);
    }

    public static Query forSqlite( Query src, Function<Query, Query> fn)
    {
        return src.forEngine(EngineCodes.SQLITE, fn);
    }

    public static Query forSqlServer( Query src, Function<Query, Query> fn)
    {
        return src.forEngine(EngineCodes.SQL_SERVER, fn);
    }

}
