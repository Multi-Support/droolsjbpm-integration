package org.jbpm.task.indexing.api;

/**
 * Marker filter to treat matches to inputs of a query parser.
 */
public class QueryStringFilter extends TermFilter {

    public QueryStringFilter(Occurs occurs, String field, String... terms) {
        super(occurs, field, terms);
    }
}
