/*
 * This software is the property of Multi-Support R&D A/S.
 *
 * © Multi-Support: 2010.
 */
package org.jbpm.task.indexing.api;

/**
 * Provides exact matching on numeric non decimal values.
 *
 * @version: $Id: NumericFilter.java,v 1.1.2.2 2010-06-22 09:02:43 MSRD+hl Exp
 * $
 * @author: Hans Lund
 */
public class NumericFilter<K> extends Filter<K, Long> {


    /**
     * creates a new filter on the field, matching if one of the terms given
     * matches the filter. (using OR operator between terms).
     *
     * @param occurs the filters effect
     * @param field  the filed
     * @param values the initial values.
     */
    public NumericFilter(Occurs occurs, String field, Long... values) {
        super(occurs, field, values);
    }


    @Override
    public Long[] getMatches() {
        return matches.toArray(new Long[matches.size()]);
    }
}
