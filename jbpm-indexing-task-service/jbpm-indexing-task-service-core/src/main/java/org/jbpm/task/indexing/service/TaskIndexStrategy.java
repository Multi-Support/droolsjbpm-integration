package org.jbpm.task.indexing.service;

import java.util.Map;

import org.kie.api.task.model.Task;

public interface TaskIndexStrategy {

	Map<String, Object> index(Task task);
}
