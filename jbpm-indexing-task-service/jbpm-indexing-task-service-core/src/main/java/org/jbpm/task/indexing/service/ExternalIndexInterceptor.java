package org.jbpm.task.indexing.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.impl.AbstractInterceptor;
import org.jbpm.services.task.commands.AddContentCommand;
import org.jbpm.services.task.commands.AddTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.DeleteContentCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.RemoveTaskCommand;
import org.jbpm.services.task.commands.RemoveTasksCommand;
import org.jbpm.services.task.commands.SetTaskPropertyCommand;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.command.Command;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.FaultData;

public class ExternalIndexInterceptor extends AbstractInterceptor implements TaskLifeCycleEventListener, TaskContentReader {

	private ExternalIndexService service;

	private ThreadLocal<Map<Long,Task>> modifiedTasks = new ThreadLocal<Map<Long,Task>>();
	private ThreadLocal<Map<Long,Task>> insertedTasks = new ThreadLocal<Map<Long,Task>>();

	private ThreadLocal<Map<Long,Map<Long, Content>>> contents = new ThreadLocal<Map<Long,Map<Long,Content>>>();

	public ExternalIndexInterceptor(ExternalIndexService service) {
		this.service = service;
	}
	
	public <T> T execute(Command<T> command) {
		//before
		Integer ref = null;
		try {
			modifiedTasks.set(new HashMap<Long, Task>());
			insertedTasks.set(new HashMap<Long, Task>());
			contents.set(new HashMap<Long, Map<Long, Content>>());
			
			T result = executeNext(command);
			populateContentsFromCommand(command, result);
			//update index
			System.out.println("modifiedTasks = " + modifiedTasks);
			System.out.println("modifiedTasks.get = " + modifiedTasks.get());
			System.out.println("insertedTasks = " + insertedTasks);
			System.out.println("insertedTasks.get = " + insertedTasks.get());
			System.out.println("service = " + service);
			
			service.prepare(modifiedTasks.get().values(), insertedTasks.get().values(), this);
			service.commit();
			return result;
		} catch (Exception e) {
			//rollback index
			service.rollback();
			throw new RuntimeException(e);
		} finally {
			modifiedTasks.remove();
			insertedTasks.remove();
			contents.remove();
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

	protected void populateContentsFromCommand(Command<?> command, Object result) {
		if (command instanceof AddContentCommand) {
			AddContentCommand addContentCommand = (AddContentCommand) command;
			Long taskId = addContentCommand.getTaskId();
			Content content = addContentCommand.getContent();
			putInMap(taskId, content);
		} else if (command instanceof AddTaskCommand) {
			AddTaskCommand addTaskCommand = (AddTaskCommand) command;
			Long taskId = (Long) result;
			byte[] content = ContentMarshallerHelper.marshallContent(addTaskCommand.getParams(), null);
			Content contentObject = TaskModelProvider.getFactory().newContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeLong(addTaskCommand.getTask().getTaskData().getDocumentContentId());
				out.writeInt(content.length);
				out.write(content);
				out.flush();
				contentObject.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			putInMap(taskId, contentObject);
		} else if (command instanceof CompleteTaskCommand) {
			CompleteTaskCommand completeTaskCommand = (CompleteTaskCommand) command;
			Long taskId = completeTaskCommand.getTaskId();
			byte[] content = ContentMarshallerHelper.marshallContent(completeTaskCommand.getData(), null);
			Content contentObject = TaskModelProvider.getFactory().newContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeLong(0L); //TODO needs to have the task object to get the output content id, not present in the complete command.
				out.writeInt(content.length);
				out.write(content);
				out.flush();
				contentObject.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			putInMap(taskId, contentObject);
		} else if (command instanceof FailTaskCommand) {
			FailTaskCommand failTaskCommand = (FailTaskCommand) command;
			Long taskId = failTaskCommand.getTaskId();
			byte[] content = ContentMarshallerHelper.marshallContent(failTaskCommand.getData(), null);
			Content contentObject = TaskModelProvider.getFactory().newContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeLong(0L); //TODO needs to have the task object to get the fault content id, not present in the fail command.
				out.writeInt(content.length);
				out.write(content);
				out.flush();
				contentObject.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			putInMap(taskId, contentObject);
		} else if (command instanceof SetTaskPropertyCommand) {
			//new or updated content
			SetTaskPropertyCommand spCommand = (SetTaskPropertyCommand) command;
			switch (spCommand.getProperty()) {
				case SetTaskPropertyCommand.FAULT_PROPERTY:
					FaultData data = (FaultData) spCommand.getValue();
					byte[] content1 = data.getContent();
					Content contentObject1 = TaskModelProvider.getFactory().newContent();
					ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
					try {
						ObjectOutputStream out = new ObjectOutputStream(baos1);
						out.writeLong(0L); //TODO needs to have the task object to get the fault content id, not present in the fail command.
						out.writeInt(content1.length);
						out.write(content1);
						out.flush();
						contentObject1.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos1.toByteArray())));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					putInMap(spCommand.getTaskId(), contentObject1);
					break;
				case SetTaskPropertyCommand.OUTPUT_PROPERTY:
					Content contentObject2 = TaskModelProvider.getFactory().newContent();
					if (spCommand.getValue() instanceof Map) {
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						try {
							ObjectOutputStream out = new ObjectOutputStream(baos2);
							out.writeLong(0L); //TODO needs to have the task object to get the fault content id, not present in the fail command.
							ContentData content2 = ContentMarshallerHelper.marshal(spCommand.getValue(), null);
							out.writeInt(content2.getContent().length);
							out.write(content2.getContent());
							out.flush();
							contentObject2.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos2.toByteArray())));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					} else {
						ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
						ContentData data2 = (ContentData) spCommand.getValue();
						try {
							ObjectOutputStream out = new ObjectOutputStream(baos3);
							out.writeLong(0L); //TODO needs to have the task object to get the fault content id, not present in the fail command.
							out.writeInt(data2.getContent().length);
							out.write(data2.getContent());
							out.flush();
							contentObject2.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos3.toByteArray())));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					putInMap(spCommand.getTaskId(), contentObject2);
					break;
			}
		} else if (command instanceof DeleteContentCommand) {
			DeleteContentCommand dcCommand = (DeleteContentCommand) command;
			Long contentId = dcCommand.getContentId();
			Long taskId = dcCommand.getTaskId();
			removeFromMap(taskId, contentId);
		} else if (command instanceof RemoveTaskCommand) {
			RemoveTaskCommand rmCommand = (RemoveTaskCommand) command;
			removeFromMap(rmCommand.getTaskId(), -1L);
		} else if (command instanceof RemoveTasksCommand) {
			RemoveTasksCommand rmCommand = (RemoveTasksCommand) command;
			for (TaskSummary task : rmCommand.getTasks()) {
				removeFromMap(task.getId(), -1L);
			}
		} else if (command instanceof CompositeCommand) {
			CompositeCommand<?> composite = (CompositeCommand<?>) command;
			for (Command<?> subCommand : composite.getCommands()) {
				populateContentsFromCommand(subCommand, result);
			}
		}
	}
	
	private void removeFromMap(Long taskId, Long contentId) {
		Map<Long, Content> contentMap = contents.get().get(taskId);
		if (contentMap != null) {
			if (contentId != null) {
				if (contentId < 0) {
					contentMap.clear();
				} else {
					contentMap.remove(contentId);
				}
			}
		}
	}

	private void putInMap(Long taskId, Content content) {
		Map<Long, Content> contentMap = contents.get().get(taskId);
		if (contentMap == null) {
			contentMap = new HashMap<Long, Content>();
			contents.get().put(taskId, contentMap);
		}
		contentMap.put(content.getId(), content);
	}
	
	@Override
	public Content getTaskContent(Long taskId, Long contentId) {
		Map<Long, Content> contentMap = contents.get().get(taskId);
		if (contentMap != null) {
			Content content = contentMap.get(contentId);
			return content;
		}
		return null;
	}
}
