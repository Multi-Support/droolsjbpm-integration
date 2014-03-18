/*
 * This software is the property of Multi-Support R&D A/S.
 *
 * © Multi-Support: 2010.
 */
package org.jbpm.task.indexing.api;

/**
 * Groups of filters connected by this provided operator.
 *
 * @param <K> the type of elements being filtered be the filter.
 * @author Hans Lund
 * @version $Id: FilterGroup.java,v 1.1.2.1 2014/03/17 07:31:20 hl Exp $
 */
public class FilterGroup<K> extends ObjectFilter<K, Filter> {

    /**
     * Creates a new Filter.
     *
     * @param occurs  sets if matches should occur or not.
     * @param matches sets what is matched on.
     */
    public FilterGroup(Occurs occurs, Filter... matches) {
        super(occurs, null, Filter.class, matches);
    }

    @Override
    public int hashCode() {
        int ret = super.hashCode();
        for (Filter f : matches) {
            ret = (29 * ret + f.hashCode()) + 3;
        }
        return ret;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterGroup other = (FilterGroup) o;
        if (this.occurs != other.occurs) {
            return false;
        }
        if (matches.size() != other.matches.size()) {
            return false;
        }
        for (int i = 0; i < matches.size(); i++) {
            if (!matches.get(i).equals(other.matches.get(i))) {
                return false;
            }
        }
        return true;
    }


}
