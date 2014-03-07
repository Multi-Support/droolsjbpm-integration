package org.jbpm.task.indexing.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

public interface ExternalIndexService {

	void prepare(Collection<Task> updates, Collection<Task> inserts)
        throws IOException;

	void commit() throws IOException;

	void rollback();

	void syncIndex(Iterator<Task> previousTasks) throws IOException;

	<T> QueryResult<T> find(Class<T> class1, int offset, int count, Comparator<T> comparator, Filter<?, ?>... filters);
}
