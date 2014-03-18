package org.jbpm.task.indexing.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryComparator;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

public interface ExternalIndexService <T> {

	void prepare(Collection<Task> updates, Collection<Task> inserts,
        TaskContentReader contentReader)
        throws IOException;

	void commit() throws IOException;

	void rollback();

	void syncIndex(Iterator<Task> previousTasks,
        TaskContentReader contentReader) throws IOException;

	QueryResult<T> find(int offset, int count, QueryComparator<T> comparator,
        Filter<?, ?>... filters)
        throws IOException;
}
