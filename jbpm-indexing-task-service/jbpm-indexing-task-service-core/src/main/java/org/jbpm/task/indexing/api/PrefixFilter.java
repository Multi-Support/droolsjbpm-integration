package org.jbpm.task.indexing.api;


/**
 * A prefix filter is a TermFilter, where the terms are treated as prefixes to
 * the matches.
 */
public class PrefixFilter<K> extends WildCardFilter<K> {

    /**
     * creates a new filter on the field, matching if one of the terms given
     * matches the filter. (using OR operator between terms).
     *
     * @param occurs the filters effect
     * @param field  the filed
     * @param terms  the initial terms.
     */
    public PrefixFilter(Occurs occurs, String field, String... terms) {
        super(occurs, field);
        for (String term : terms) {
            super.add(escape(term) + WildCardFilter.DEFAULT_GENERIC_WILDCARD);
        }
    }
}
