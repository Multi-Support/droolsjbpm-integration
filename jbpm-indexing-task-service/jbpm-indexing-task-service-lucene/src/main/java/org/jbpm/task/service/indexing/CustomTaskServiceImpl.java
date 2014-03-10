package org.jbpm.task.service.indexing;

import java.io.IOException;
import java.util.Comparator;

import org.drools.core.command.CommandService;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.jbpm.task.indexing.service.ExternalIndexService;
import org.kie.api.task.model.Task;


public class CustomTaskServiceImpl extends CommandBasedTaskService
    implements CustomTaskService {

    private CommandService executor;
    private ExternalIndexService<Task> externalIndexService;

    public CustomTaskServiceImpl(CommandService executor,
        TaskEventSupport taskEventSupport,
        ExternalIndexService externalIndexService) {
        super(executor, taskEventSupport);
        this.executor = executor;
        this.externalIndexService = externalIndexService;
    }

    public QueryResult<Task> findTasks(int offset, int count,
        Comparator<Task> comparator, Filter<?, ?>... filters)
        throws IOException {
        return externalIndexService
            .find(offset, count, comparator, filters);
    }
}
