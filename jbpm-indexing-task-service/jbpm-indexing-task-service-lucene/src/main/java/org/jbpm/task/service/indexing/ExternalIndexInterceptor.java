package org.jbpm.task.service.indexing;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.impl.AbstractInterceptor;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskEvent;

public class ExternalIndexInterceptor extends AbstractInterceptor implements TaskLifeCycleEventListener {

	private ExternalIndexService service;

	private ThreadLocal<Map<Long,Task>> modifiedTasks = new ThreadLocal<Map<Long,Task>>();
	private ThreadLocal<Map<Long,Task>> insertedTasks = new ThreadLocal<Map<Long,Task>>();
	
	public ExternalIndexInterceptor(ExternalIndexService service) {
		this.service = service;
	}
	
	public <T> T execute(Command<T> command) {
		//before
		Integer ref = null;
		try {
			modifiedTasks.set(new HashMap<Long, Task>());
			insertedTasks.set(new HashMap<Long, Task>());
			T result = executeNext(command);
			//update index
			System.out.println("modifiedTasks = " + modifiedTasks);
			System.out.println("modifiedTasks.get = " + modifiedTasks.get());
			System.out.println("insertedTasks = " + insertedTasks);
			System.out.println("insertedTasks.get = " + insertedTasks.get());
			System.out.println("service = " + service);
			ref = service.prepare(modifiedTasks.get().values(), insertedTasks.get().values());
			service.commit();
			return result;
		} catch (Exception e) {
			//rollback index
			service.rollback(ref);
			throw new RuntimeException(e);
		}
	}

	public void beforeTaskActivatedEvent(TaskEvent event) { }
	public void beforeTaskClaimedEvent(TaskEvent event) { }
	public void beforeTaskSkippedEvent(TaskEvent event) { }
	public void beforeTaskStartedEvent(TaskEvent event) { }
	public void beforeTaskStoppedEvent(TaskEvent event) { }
	public void beforeTaskCompletedEvent(TaskEvent event) { }
	public void beforeTaskFailedEvent(TaskEvent event) { }
	public void beforeTaskAddedEvent(TaskEvent event) { }
	public void beforeTaskExitedEvent(TaskEvent event) { }
	public void beforeTaskReleasedEvent(TaskEvent event) { }
	public void beforeTaskResumedEvent(TaskEvent event) { }
	public void beforeTaskSuspendedEvent(TaskEvent event) { }
	public void beforeTaskForwardedEvent(TaskEvent event) { }
	public void beforeTaskDelegatedEvent(TaskEvent event) { }
	
	public void afterTaskActivatedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskClaimedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskSkippedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskStartedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskStoppedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskCompletedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskFailedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskAddedEvent(TaskEvent event) {
		insertedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskExitedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskReleasedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskResumedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskSuspendedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskForwardedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

	public void afterTaskDelegatedEvent(TaskEvent event) {
		modifiedTasks.get().put(event.getTask().getId(), event.getTask());
	}

}
