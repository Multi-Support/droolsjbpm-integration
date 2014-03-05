package org.jbpm.task.indexing.service;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.task.model.Task;

public class SimpleTaskIndexStrategy implements TaskIndexStrategy {

	public Map<String, Object> index(Task task) {
		Map<String, Object> retval = new HashMap<String, Object>();
		retval.put("taskId", task.getId());
		retval.put("taskType", task.getTaskType());
		retval.put("taskName", task.getNames().iterator().next().getText());
		return retval;
	}

}
