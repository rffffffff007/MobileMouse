package com.mobilemouse.util;

import java.util.Comparator;

public class TimeTaskQueue extends OrderedList<TimeTask> {

	public TimeTaskQueue() {
		super(new TimeTaskComparetor(), true);
	}

	public TimeTask pop() {
		if (size() == 0)
			return null;
		return remove(0);
	}

	public TimeTask top() {
		if (size() == 0)
			return null;
		return get(0);
	}

	public void push(TimeTask a) {
		if (!contains(a))
			add(a);
	}

	public void removeById(int id) {
		TimeTask task = null;
		for (TimeTask t : this) {
			if (t.getId() == id) {
				task = t;
				break;
			}
		}
		if (task != null)
			remove(task);
	}

	static class TimeTaskComparetor implements Comparator<TimeTask> {
		@Override
		public int compare(TimeTask o1, TimeTask o2) {
			return (int) (o1.getTime() - o2.getTime());
		}
	}

}
