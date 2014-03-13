package org.jbpm.task.indexing.api;


import java.lang.reflect.Field;
import java.util.Comparator;


/**
 * The QueryComparator is used to construct the sorting for a query.
 *
 * Indexing services might rely on the exact implementation or delegate to the
 * under storage, using the implementations as hint.
 *
 * To be used by the underlying storage, the name of the comparator must
 * carefully match the model POJO attribute to be sorted on.
 *
 * @param <T> the model object type.
 */
public class QueryComparator<T> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        //TODO: ugly refection based simple compare - need naming convention to handle object graph walks.
        int nullCompare = objectNullCompare(o1,o2);
        if (nullCompare != 2) return nullCompare;
        Class c = o1.getClass();
        try {
            Field f = c.getField(name);
            boolean assess = f.isAccessible();
            if (!assess) {
                f.setAccessible(true);
            }
            Object oo1 = f.get(o1);
            Object oo2 = f.get(o2);
            if (!assess) {
                f.setAccessible(false);
            }
            return compareComparable((Comparable) oo1, (Comparable) oo2);
        } catch (NoSuchFieldException e) {
            return 0;
        } catch (IllegalAccessException e) {
            return 0;
        }

    }

    public enum Direction {
        ASCENDING,
        DESCENDING
    }

    protected Direction direction;
    protected String name;
    protected Class type;


    protected QueryComparator(Direction direction, String name) {
        this.direction = direction;
        this.name = name;
        this.type = String.class;
    }


    protected QueryComparator(Direction direction, String name, Class type) {
        this.direction = direction;
        this.name = name;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    protected int compareComparable(Comparable o1, Comparable o2) {
        int compare = 0;
        if (o1 == null) {
            if (o2 != null) {
                compare = -1;
            }
        } else {
            compare = o1.compareTo(o2);
        }
        return Direction.ASCENDING.equals(direction) ? compare : -compare;
    }

    protected int objectNullCompare(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return Direction.ASCENDING.equals(direction) ? -1 : 1;
            }
            return Direction.ASCENDING.equals(direction) ? 1 : -1;
        }
        return 2;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public Class getType()  {
        return type;
    }

}
