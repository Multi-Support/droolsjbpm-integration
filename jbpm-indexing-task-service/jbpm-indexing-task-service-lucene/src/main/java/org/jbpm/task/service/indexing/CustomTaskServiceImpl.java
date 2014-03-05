package org.jbpm.task.service.indexing;

import java.util.Comparator;

import org.drools.core.command.CommandService;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.task.model.Task;

import com.multisupport.query.Filter;
import com.multisupport.query.QueryResult;

public class CustomTaskServiceImpl extends CommandBasedTaskService implements CustomTaskService {

	private CommandService executor;
	private ExternalIndexService externalIndexService;

	public CustomTaskServiceImpl(CommandService executor, TaskEventSupport taskEventSupport, ExternalIndexService externalIndexService) {
		super(executor, taskEventSupport);
		this.executor = executor;
		this.externalIndexService = externalIndexService;
	}

    public QueryResult<Task> findTasks(int offset, int count, Comparator<Task> comparator, Filter<?, ?>... filters) {
    	return externalIndexService.find(Task.class, offset, count, comparator, filters);
    }
}
