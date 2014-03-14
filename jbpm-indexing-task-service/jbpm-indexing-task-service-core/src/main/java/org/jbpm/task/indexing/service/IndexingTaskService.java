package org.jbpm.task.indexing.service;

import java.io.IOException;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryComparator;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;

public interface IndexingTaskService extends InternalTaskService {

	QueryResult<Task> findTasks(int offset, int count, QueryComparator<Task> comparator, Filter<?, ?>... filters)
        throws IOException;

	/*QueryResult<Attachment> findAttachments(int offset, int count, Comparator<Attachment> comparator, Filter<?, ?>... filters);
	
	QueryResult<Attachment> findContents(int offset, int count, Comparator<Content> comparator, Filter<?, ?>... filters);
	
	QueryResult<Attachment> findComments(int offset, int count, Comparator<Comment> comparator, Filter<?, ?>... filters);*/

}
