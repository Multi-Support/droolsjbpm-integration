package org.jbpm.task.indexing.api;

import java.lang.reflect.Array;

/**
 * A range filter is a special filter that can applied to fields being sortable
 * only.
 * <p/>
 * RangeFilters
 *
 * @param <K> the type being filtered.
 * @param <T> the type being used in match.
 * @author Hans Lund
 * @version $Id: RangeFilter.java,v 1.1.2.1 2014/03/17 07:31:19 hl Exp $
 */
public class RangeFilter<K, T>
    extends Filter<K, RangeFilter.Range> {

    private Class rangeType;

    /**
     * Creates a new empty range filter. Before applying the filter ranges need
     * to be added.
     *
     * @param occurs if matches must occur or not.
     * @param field  the field being filtered upon.
     */
    public RangeFilter(Occurs occurs, String field) {
        super(occurs, field);
        rangeType = this.new Range().getClass();
    }

    @Override
    public Range[] getMatches() {
        return matches
            .toArray((Range[]) Array.newInstance(rangeType, matches.size()));
    }

    public boolean matches(Object value) {
        for (Range r : getMatches()) {
            //lower bound check
            int l = ((Comparable) value).compareTo(r.lower);
            if (l < 0) {
                continue;
            } else if (l == 0 && !r.includeLow) {
                continue;
            }
            //upper bound check
            int h = ((Comparable) value).compareTo(r.upper);
            if (h > 0) {
                continue;
            } else if (h == 0 && !r.includeUp) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a Range including both ends.
     *
     * @param lower lower end of range.
     * @param upper upper end of range.
     * @return true if added, false if duplicate.
     */
    public boolean addRange(T lower, T upper) {
        return this.add(this.new Range(lower, upper, true, true));
    }

    /**
     * Adds a range to the filter.
     *
     * @param lower      lower end of range.
     * @param upper      upper end of range.
     * @param includeLow determine if lower end is inclusive
     * @param includeUp  determine if upper end is inclusive
     * @return true if added, false if duplicate.
     */
    public boolean addRange(T lower, T upper, boolean includeLow,
        boolean includeUp) {
        return this.add(this.new Range(lower, upper, includeLow, includeUp));
    }

    /**
     * A Range is the Object known to a RangeFilter. It holds information about
     * the boundaries of the Range.
     * <p/>
     * Ranges must only be created on the RangeFilter they belong to.
     * <p/>
     * Use the addRange methods on RangeFilter to create Ranges.
     *
     * @author Hans Lund
     * @version $Id: RangeFilter.java,v 1.1.2.4 2010-06-22 09:02:43 MSRD+hl Exp
     *          $
     */
    public class Range {

        /**
         * if true -> include T's that are equal to lower bound.
         */
        boolean includeLow;
        /**
         * if true -> include T's that are equal to upper bound.
         */
        boolean includeUp;
        private T lower;
        private T upper;

        /**
         * Empty constructor needed for runtime class reading.
         */
        private Range() {
        }

        private Range(T lower, T upper, boolean includeLow, boolean includeUp) {
            this.lower = lower;
            this.upper = upper;
            this.includeLow = includeLow;
            this.includeUp = includeUp;
        }

        /**
         * Gets the lower limit for the range.
         *
         * @return lower limit.
         */
        public T getLower() {
            return lower;
        }

        /**
         * Gets upper limit for the range.
         *
         * @return upper limit.
         */
        public T getUpper() {
            return upper;
        }

        /**
         * Limits can be inclusive or exclusive, this tells the state of the
         * lower limit.
         *
         * @return <code>true</code> if the lower limit is part of the filter.
         */
        public boolean isLowIncluded() {
            return includeLow;
        }

        /**
         * Limits can be inclusive or exclusive, this tells the state of the
         * upper limit.
         *
         * @return <code>true</code> if the upper limit is part of the filter.
         */
        public boolean isUpIncluded() {
            return includeUp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Range range = (Range) o;

            if (includeLow != range.includeLow) {
                return false;
            }
            if (includeUp != range.includeUp) {
                return false;
            }
            if (lower != null ? !lower.equals(range.lower)
                : range.lower != null) {
                return false;
            }
            if (upper != null ? !upper.equals(range.upper)
                : range.upper != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = lower != null ? lower.hashCode() : 0;
            result = 31 * result + (upper != null ? upper.hashCode() : 0);
            result = 31 * result + (includeLow ? 1 : 0);
            result = 31 * result + (includeUp ? 1 : 0);
            return result;
        }
    }
}
