package org.jbpm.task.service.indexing;

import java.util.Comparator;

import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.InternalTaskService;

import com.multisupport.query.Filter;
import com.multisupport.query.QueryResult;

public interface CustomTaskService extends InternalTaskService, EventService<TaskLifeCycleEventListener> {

	QueryResult<Task> findTasks(int offset, int count, Comparator<Task> comparator, Filter<?, ?>... filters);
}
