package org.jbpm.task.service.indexing;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kie.api.task.model.Task;

import com.multisupport.query.Filter;
import com.multisupport.query.QueryResult;
import com.multisupport.query.QueryResultImpl;

public class DebugExternalIndexService implements ExternalIndexService {

	private Map<Long, Map<String, Object>> indexes = new HashMap<Long, Map<String, Object>>();
	private Map<Long, Task> tasks = new HashMap<Long, Task>();
	private int counter = 0;
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
	
	public void rollback(Integer ref) {
		System.out.println("rollback: " + ref);
	}
	
	public void syncIndex(Iterator<Task> persistedTasks) {
		if (persistedTasks != null) {
			while (persistedTasks.hasNext()) {
				putInIndex(persistedTasks.next());
			}
		}
	}
	
	public Integer prepare(Collection<Task> updates, Collection<Task> inserts) {
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
		return ++counter;
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

	public <T> QueryResult<T> find(Class<T> class1, int offset, int count,
			Comparator<T> comparator, Filter<?, ?>... filters) {
		int total = 0; //TODO implement search
		Collection<T> result = new ArrayList<T>(); //TODO implement search
		
		return new QueryResultImpl<T>(URI.create("mock"), offset, total, result);
	}
}
