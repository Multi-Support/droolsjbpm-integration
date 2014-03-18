package org.jbpm.task.indexing.api;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * The QueryResult is a unmodifiable List, that can be iterated but not altered
 * after creation point.
 * <p/>
 * The QueryResult contains important additional meta-data such as offset,
 * total number of records and which comparator was used in the query.
 */
public class QueryResult<T> extends AbstractList<T> {

    private Comparator<T> comparator;
    private long offset;
    private Object[] objects;
    private long total;

    /**
     * @param offset The offset to start with.
     * @param result List of returned records.
     * @param total  The total number of records that match the query
     *               selection.
     */
    public QueryResult(long offset, long total,
        Collection<T> result) {
        this(offset, total, result, null);
    }


    /**
     * Constructs a new instance of {@link QueryResult} with a {@link
     * Comparator}.
     *
     * @param offset     The offset to start with.
     * @param result     {@link List} of returned records.
     * @param total      The total number of records that match the query
     *                   selection.
     * @param comparator
     */
    public QueryResult(long offset, long total,
        Collection<T> result, Comparator<T> comparator) {
        super();
        this.offset = offset;
        this.objects = result.toArray(new Object[result.size()]);
        this.total = total;
        this.comparator = comparator;
    }

    /**
     * {@inheritDoc}
     */
    public Comparator<T> getComparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    public long getOffset() {
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    public long getTotal() {
        return total;
    }

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) objects[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("QueryResult [comparator=").append(comparator)
            .append(", count=").append(size()).append(", offset=")
            .append(offset).append(", total=").append(total)
            .append(", result=");
        if (objects == null || objects.length == 0) {
            sb.append("null");
        } else {
            for (Object record : objects) {
                sb.append("\n").append(record);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
