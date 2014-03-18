package org.jbpm.task.indexing.api;

/**
 * TermFilter filters on exact matches on the defined field.
 *
 * @param <K> the type of element we can filter on.
 * @author Hans Lund
 * @version $Id: TermFilter.java,v 1.1.2.2 2014/03/18 13:22:25 sa Exp $
 */
public class TermFilter<K> extends Filter<K, String> {


    /**
     * creates a new filter on the field, matching if one of the terms given
     * matches the filter. (using OR operator between terms).
     *
     * @param occurs the filters effect
     * @param field  the field
     * @param terms  the initial terms.
     */
    public TermFilter(Occurs occurs, String field, String... terms) {
        super(occurs, field, terms);
    }

    @Override
    public String[] getMatches() {
        return matches.toArray(new String[matches.size()]);
    }

}
