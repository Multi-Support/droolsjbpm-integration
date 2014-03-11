package org.jbpm.task.indexing.service;

import java.io.IOException;
import java.util.Comparator;

import org.drools.core.command.CommandService;
import org.jbpm.services.task.commands.TaskCommandExecutorImpl;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.kie.api.task.model.Task;

public class IndexingTaskServiceImpl extends CommandBasedTaskService implements IndexingTaskService {
 
	private TaskCommandExecutorImpl cmdExecutor;
	private ExternalIndexService externalIndexService;

	public IndexingTaskServiceImpl(CommandService executor, TaskEventSupport taskEventSupport, ExternalIndexService externalIndexService) {
		super(executor, taskEventSupport);
		if (!(executor instanceof TaskCommandExecutorImpl)) {
			throw new IllegalArgumentException("IndexingTaskService needs a TaskCommandExecutorImpl commandService implementation");
		}
		this.cmdExecutor = (TaskCommandExecutorImpl) executor;
		this.externalIndexService = externalIndexService;
		ExternalIndexInterceptor logger = new ExternalIndexInterceptor(externalIndexService);
		cmdExecutor.addInterceptor(logger);
		taskEventSupport.addEventListener(logger);
		cmdExecutor.execute(new ReloadAllTasksCommand(externalIndexService,logger));
	}

    public QueryResult<Task> findTasks(int offset, int count, Comparator<Task> comparator, Filter<?, ?>... filters)
        throws IOException {
    	return externalIndexService.find(offset, count, comparator, filters);
    }
}
