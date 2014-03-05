package org.jbpm.task.indexing.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

public interface ExternalIndexService {

	Integer prepare(Collection<Task> updates, Collection<Task> inserts);

	void commit();

	void rollback(Integer ref);

	void syncIndex(Iterator<Task> previousTasks);

	<T> QueryResult<T> find(Class<T> class1, int offset, int count, Comparator<T> comparator, Filter<?, ?>... filters);
}
