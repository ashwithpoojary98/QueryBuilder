package io.github.ashwithpoojary98.clause;

import io.github.ashwithpoojary98.Query;

public interface IJoin {

    Query join(String table, String first, String second,String op);

    Query join(String table, String first, String second);

    Query leftJoin(String table, String first, String second, String op);

    Query leftJoin(String table, String first, String second);

    Query rightJoin(String table, String first, String second, String op);

    Query rightJoin(String table, String first, String second);

    Query crossJoin(String table, String first, String second, String op);

    Query crossJoin(String table, String first, String second);

}
