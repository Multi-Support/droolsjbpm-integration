package org.jbpm.task.service.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.jbpm.task.indexing.service.ExternalIndexService;
import org.jbpm.task.indexing.service.SimpleTaskIndexStrategy;
import org.jbpm.task.indexing.service.TaskContentReader;
import org.jbpm.task.indexing.service.TaskIndexStrategy;
import org.kie.api.task.model.Task;

public class DebugExternalIndexService implements ExternalIndexService <Task> {

	private Map<Long, Map<String, Object>> indexes = new HashMap<Long, Map<String, Object>>();
	private Map<Long, Task> tasks = new HashMap<Long, Task>();
	private TaskIndexStrategy strategy;

	public DebugExternalIndexService() {
		this(new SimpleTaskIndexStrategy());
	}
	
	public void setStrategy(TaskIndexStrategy strategy) {
		this.strategy = strategy;
	}
	
	public DebugExternalIndexService(TaskIndexStrategy strategy) {
		this.strategy = strategy;
	}

    @Override
	public void rollback() {
		System.out.println("rollback");
	}

    @Override
	public void syncIndex(Iterator<Task> persistedTasks, TaskContentReader reader) {
		if (persistedTasks != null) {
			while (persistedTasks.hasNext()) {
				putInIndex(persistedTasks.next());
			}
		}
	}
	
	public void prepare(Collection<Task> updates, Collection<Task> inserts, TaskContentReader reader) {
		System.out.println("prepare: " + updates + ", " + inserts);
		if (inserts != null) {
			for (Task task : inserts) {
				putInIndex(task);
			}
		}
		if (updates != null) {
			for (Task task : updates) {
				removeFromIndex(task);
				putInIndex(task);
			}
        }
	}

	private void removeFromIndex(Task task) {
		indexes.remove(task.getId());
		tasks.remove(task.getId());
	}

	private void putInIndex(Task task) {
		indexes.put(task.getId(), strategy.index(task));
		tasks.put(task.getId(), task);
	}

    public void commit() {
		System.out.println("commit");
	}

    public List<Task> get(String q) {
		List<Task> result = new ArrayList<Task>();
		for (Map.Entry<Long, Map<String, Object>> entry : indexes.entrySet()) {
			if (toString(entry.getValue()).contains(q)) {
				result.add(tasks.get(entry.getKey()));
			}
		}
		return result;
	}
	
	private String toString(Map<String, Object> key) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : key.entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append(';');
		}
		return sb.toString();
	}

    @Override
	public QueryResult<Task> find( int offset, int count,
			Comparator<Task> comparator, Filter<?, ?>... filters) {
		int total = 0; //TODO implement search
		Collection<Task> result = new ArrayList<Task>(); //TODO implement search
		
		return new QueryResult<Task>(offset,total,result);
	}
}
