package org.jbpm.task.indexing.service;

import org.kie.api.task.model.Content;

public interface TaskContentReader {

	Content getTaskContent(Long taskId, Long contentId);
}
