package org.jbpm.task.indexing.service;

import java.util.List;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

public class ReloadAllTasksCommand extends TaskCommand<Void> {

	private ExternalIndexService service;

	public ReloadAllTasksCommand(ExternalIndexService service) {
		this.service = service;
	}
	
	public Void execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		//TODO add pagination to load a limited number of objects to pass to syncIndex at a time
		List<Task> tasks = persistenceContext.queryInTransaction("GetAllTasks",
				ClassUtil.<List<Task>>castClass(List.class));
		//WARNING: tasks should be an iterator, but JPA doesn't support
		service.syncIndex(tasks.iterator());
		return null;
	}
}
