package io.github.ashwithpoojary98;

import io.github.ashwithpoojary98.compilers.PostgresCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SelectTest {

    @Test
    void selectQueryTest() {

        Query query = new Query("Posts")
                .select("Id", "Title", "CreatedAt as Date");
        PostgresCompiler database = new PostgresCompiler();

        String sqlQuery = database.compile(query).toSql();
        Assertions.assertEquals(sqlQuery, "SELECT \"Id\", \"Title\", \"CreatedAt\" AS \"Date\" FROM \"Posts\"");

        Query countQuery = new Query("Comments")
                .whereColumns("Comments.PostId", "=", "Posts.Id").asCount();
        Query parentQuery = new Query("Posts").
                select("Id").select(countQuery, "CommentsCount");

        Assertions.assertEquals(database.compile(parentQuery).toSql(),
                "SELECT \"Id\", (SELECT COUNT(*) AS \"count\" FROM \"Comments\" WHERE \"Comments\".\"PostId\" = \"Posts\".\"Id\") AS \"CommentsCount\" FROM \"Posts\"");
    }


}
