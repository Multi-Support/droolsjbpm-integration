package org.jbpm.task.indexing.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A ChainedComparator simply chain single comparators together, if objects are
 * equal in the first comparator the next comparator is visited.
 */
public class ChainedComparator<T> extends QueryComparator<T> {


    private List<QueryComparator<T>> chain;

    public ChainedComparator(QueryComparator<T>... initialChain) {
        super(null,null);
        this.chain = new ArrayList<QueryComparator<T>>();
        chain.addAll(Arrays.asList(initialChain));
    }

    public void add(QueryComparator<T> comparator) {
        chain.add(comparator);
    }


    public int compare(T o1, T o2) {
        if (chain.size() == 0) {
            throw new IllegalStateException(
                "ChainedComparator can't compare with empty chain");
        }
        for (Comparator<T> c : chain) {
            int i = c.compare(o1, o2);
            if (i != 0) {
                return i;
            }
        }
        return 0;
    }

    public QueryComparator[] getComparators() {
        return chain.toArray(new QueryComparator[chain.size()]);
    }
}

