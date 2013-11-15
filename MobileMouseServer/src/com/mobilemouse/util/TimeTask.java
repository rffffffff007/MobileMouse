package com.mobilemouse.util;

public class TimeTask implements Runnable {
	private long time;
	private int id;

	public TimeTask(int id) {
		this(id, System.currentTimeMillis());
	}

	public TimeTask(int id, long d) {
		this.id = id;
		time = d;
	}

	@Override
	public void run() {
	}

	public int getId() {
		return id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setDelay(long delay) {
		this.time = System.currentTimeMillis() + delay;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeTask) {
			return ((TimeTask) obj).getId() == getId();
		}
		return false;
	}
}
