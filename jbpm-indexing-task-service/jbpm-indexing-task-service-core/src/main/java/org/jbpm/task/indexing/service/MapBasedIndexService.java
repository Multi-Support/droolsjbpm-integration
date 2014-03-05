package org.jbpm.task.indexing.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.jbpm.task.indexing.api.TermFilter;
import org.kie.api.task.model.Task;

public class MapBasedIndexService implements ExternalIndexService {

	private Map<Long, Map<String, Object>> indexes = new HashMap<Long, Map<String, Object>>();
	private Map<Long, Task> tasks = new HashMap<Long, Task>();
	private int counter = 0;
	private TaskIndexStrategy strategy;

	public MapBasedIndexService() {
		this(new SimpleTaskIndexStrategy());
	}
	
	public void setStrategy(TaskIndexStrategy strategy) {
		this.strategy = strategy;
	}
	
	public MapBasedIndexService(TaskIndexStrategy strategy) {
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
		int total = 0;
		Collection<T> result = new HashSet<T>();
		Collection<Long> taskIdClone = new ArrayList<Long>(tasks.keySet());
		for (Filter filter : filters) {
			if (filter instanceof TermFilter) {
				TermFilter tf = (TermFilter) filter;
				for (Long taskId : taskIdClone) {
					String index = toString(indexes.get(taskId));
					if (tf.matches(index)) {
						result.add((T) tasks.get(taskId));
					}
				}
			}
		}
		return new QueryResult<T>(0, result.size(), result);
	}
}
