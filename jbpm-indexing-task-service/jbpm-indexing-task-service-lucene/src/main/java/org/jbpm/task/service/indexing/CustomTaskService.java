package org.jbpm.task.service.indexing;

import java.io.IOException;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryComparator;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

/**
 * Interface for task service extension that supports paging, sort and generic
 * filters.
 * 
 * @author Hans Lund
 * @version $Id: CustomTaskService.java,v 1.1.2.2 2014/03/18 13:22:25 sa Exp $
 */
public interface CustomTaskService {

    /**
     * Finds tasks that match the specified parameters.
     * 
     * @param offset
     * @param count
     * @param comparator
     * @param filters
     * @return A <tt>QueryResult</tt> containing tasks that match the query.
     * @throws IOException If the query could not be completed.
     */
    QueryResult<Task> findTasks(int offset, int count,
        QueryComparator<Task> comparator, Filter<?, ?>... filters)
        throws IOException;

}
