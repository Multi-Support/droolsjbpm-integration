package org.jbpm.task.indexing.api;

import java.lang.reflect.Array; 

/**
 * This filter is an exact match filter.
 * <p/>
 * Note that you need to pass the Class of the objects for this filter.
 *
 * @param <K> the type of objects being examined.
 * @param <T> the type of matches in the filter.
 * @author Hans Lund
 * @version $Id: ObjectFilter.java,v 1.1.4.35.2.1 2014/01/24 09:21:01 hl Exp $
 */
public class ObjectFilter<K, T> extends Filter<K, T> {

    private Class<T> type;

    /**
     * @param occurs  the type of filter.
     * @param field   the field holding reference to the object.
     * @param clazz   the type of filter.
     * @param matches the object list to match.
     */
    public ObjectFilter(Occurs occurs, String field, Class<T> clazz, T... matches) {
        super(occurs, field, matches);
        type = clazz;
    }

    @Override
    public T[] getMatches() {
        return matches.toArray((T[]) Array.newInstance(type, matches.size()));
    }
}
