package org.jbpm.task.service.indexing;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.kie.api.task.model.Task;

import com.multisupport.query.Filter;
import com.multisupport.query.QueryResult;

public interface ExternalIndexService {

	Integer prepare(Collection<Task> updates, Collection<Task> inserts);

	void commit();

	void rollback(Integer ref);

	void syncIndex(Iterator<Task> previousTasks);

	<T> QueryResult<T> find(Class<T> class1, int offset, int count, Comparator<T> comparator, Filter<?, ?>... filters);
}
