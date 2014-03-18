package org.jbpm.task.indexing.api;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A filter defines a constrain for a query.
 * <p/>
 *
 */
public abstract class Filter<K, T> implements Serializable {

    public enum Occurs {
        MUST, SHOULD, NOT
    }

    protected List<T> matches;

    protected Occurs occurs;
    protected String field;

    /**
     * Creates a new Filter.
     *
     * @param occurs  sets if matches should occur or not.
     * @param field   sets the field to be matched
     * @param matches sets what is matched on.
     */
    public Filter(Occurs occurs, String field, T... matches) {
        this.occurs = occurs;
        this.field = field;
        if (matches != null && matches.length > 0) {
            this.matches = new ArrayList<T>(Arrays.asList(matches));
        } else {
            this.matches = new ArrayList<T>();
        }
    }

    /**
     * default implementation always return true, overwrite to filter active on
     * objects.
     */
    public boolean isInFilter(K object) {
        return true;
    }

    /**
     * Default implementation returns true if any og the given match objects
     * equals the value.
     */
    public boolean matches(Object value) {
        for (T t : getMatches()) {
            if (t.equals(value)) {
                return true;
            }
        }
        return false;
    }


    public Occurs getOccurs() {
        return occurs;
    }

    public String getField() {
        return field;
    }

    public void setField(String fieldAlias) {
        field = fieldAlias;
    }

    /**
     * Returns true if this is a single term filter.
     */
    public boolean isSingle() {
        return matches.size() == 1;
    }

    public boolean add(T match) {
        return match != null && matches.add(match);
    }

    public abstract T[] getMatches();


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Filter filter = (Filter) o;

        if (field != null ? !field.equals(filter.field)
            : filter.field != null) {
            return false;
        }
        if (matches != null ? matches.size() == filter.matches.size()
            : filter.matches != null) {
            return false;
        }
        return occurs == filter.occurs;

    }

    @Override
    public int hashCode() {
        int result = matches != null ? matches.size() : 0;
        result = 31 * result + (occurs != null ? occurs.hashCode() : 0);
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }
}
