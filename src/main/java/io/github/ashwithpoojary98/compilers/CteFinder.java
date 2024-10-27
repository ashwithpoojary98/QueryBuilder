package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.clausses.fromclausses.AbstractFrom;
import io.github.ashwithpoojary98.clausses.fromclausses.QueryFromClause;
import io.github.ashwithpoojary98.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CteFinder {

    private final Query query;
    private final String engineCode;
    private Set<String> namesOfPreviousCtes;
    private List<AbstractFrom> orderedCteList;

    public CteFinder(Query query,String engineCode){
        this.query=query;
        this.engineCode=engineCode;
    }

    public List<AbstractFrom> find() {
        if (orderedCteList != null) {
            return orderedCteList;
        }

        namesOfPreviousCtes = new HashSet<>();

        orderedCteList = findInternal(query);

        namesOfPreviousCtes.clear();
        namesOfPreviousCtes = null;

        return orderedCteList;
    }

    private List<AbstractFrom> findInternal(Query queryToSearch) {
        List<AbstractFrom> cteList = queryToSearch.getComponents("cte", engineCode);

        List<AbstractFrom> resultList = new ArrayList<>();

        for (AbstractFrom cte : cteList) {
            if (namesOfPreviousCtes.contains(cte.getAlias())) {
                continue;
            }

            namesOfPreviousCtes.add(cte.getAlias());
            resultList.add(cte);

            if (cte instanceof QueryFromClause) {
                QueryFromClause queryFromClause = (QueryFromClause) cte;
                resultList.addAll(0, findInternal(queryFromClause.getQuery()));
            }
        }

        return resultList;
    }

}
