package org.jbpm.task.service.indexing;

import java.io.IOException;
import java.util.Comparator;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryComparator;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

public interface CustomTaskService  {

	QueryResult<Task> findTasks(int offset, int count, QueryComparator<Task> comparator, Filter<?, ?>... filters)
        throws IOException;

}
